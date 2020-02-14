package vend.api.automation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import vend.api.automation.annotations.Scenario;

public abstract class ScenarioRunner {

	private static Automate automate;
	private JsonArray resource;

	private static Map<String, Object> savedData = new HashMap<>();

	private Logger logger = LoggerFactory.getLogger(ScenarioRunner.class);

	protected ScenarioRunner() {

		if (ScenarioRunner.automate == null) {
			if (System.getProperty("swagger-file") != null)
				ScenarioRunner.automate = new Automate(new File(System.getProperty("swagger-file")));
			else if (System.getProperty("swagger-url") != null) {
				try {
					ScenarioRunner.automate = new Automate(new URL(System.getProperty("swagger-url")));
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException("Please specify swagger-file or swagger-url property");
			}
		}

		Scenario scenario = this.getClass().getAnnotation(Scenario.class);
		try {
			this.resource = readResource(scenario.resource());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private JsonArray readResource(String resource) throws IOException {

		try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(resource)) {
			return new GsonBuilder().create().fromJson(new InputStreamReader(stream), JsonArray.class);
		}
	}

	protected Response callApi(String apiName, Parameter... parameters) {

		String[] methodAndPath = apiName.split(":");

		HTTP_METHOD method = HTTP_METHOD.valueOf(methodAndPath[0].toUpperCase());
		String path = methodAndPath[1];

		Operation operation = automate.findOperation(method, path);

		List<Parameter> inPath = operation.getParameters().stream()
				.filter(parameter -> parameter.getIn().equals("path")).collect(Collectors.toList());
		List<Parameter> inHeader = operation.getParameters().stream()
				.filter(parameter -> parameter.getIn().equals("header")).collect(Collectors.toList());
		List<Parameter> query = operation.getParameters().stream()
				.filter(parameter -> parameter.getIn().equals("query")).collect(Collectors.toList());
		List<Parameter> formData = operation.getParameters().stream()
				.filter(parameter -> parameter.getIn().equals("formData")).collect(Collectors.toList());

		boolean inBody = operation.getParameters().stream().anyMatch(parameter -> parameter.getIn().equals("body"));

		BiFunction<JsonArray, String, JsonObject> findParent = (resource, _apiName) -> {
			int index = 0;
			for (Iterator<JsonElement> i = resource.iterator(); i.hasNext();) {
				JsonElement element = i.next();
				if (element.isJsonPrimitive() && element.getAsString().equals(apiName)) {
					break;
				}
				index++;
			}
			if (resource.size() > index+1)
				return resource.get(index + 1).getAsJsonObject();
			else
				return null;
		};
		
		BiFunction<JsonObject, String, JsonObject> findInParent = (parent, collection) -> {
			if(parent!=null && parent.get(collection)!=null)
				return parent.get(collection).getAsJsonObject();
			else return null;
		};

		final JsonObject parent = findParent.apply(resource, apiName);
		final JsonObject data = findInParent.apply(parent, "data");
		final JsonObject headers = findInParent.apply(parent, "headers");

		/** path parameters **/
		if (parameters != null)
			inPath.removeAll(Arrays.asList(parameters));
		// convert to values
		List<Object> pathParameters = inPath.stream().map(parameter -> data.get(parameter.getName()).getAsString())
				.collect(Collectors.toList());
		// add any additional supplied path values
		if (parameters != null)
			pathParameters.addAll(Arrays.stream(parameters).filter(parameter -> parameter.getIn().equals("path"))
					.collect(Collectors.toList()));
		/** path parameters **/

		RequestSpecification given = RestAssured.given();

		String location = automate.scheme + "://" + automate.host + automate.basePath + path;
		logger.debug("Using location: {}", location);

		/** query parameters **/
		if (parameters != null)
			query.removeAll(Arrays.asList(parameters));

		if (query.size() > 0)
			query.forEach(
					queryParam -> given.queryParam(queryParam.getName(), data.get(queryParam.getName()).getAsString()));
		// add any additional supplied parameter
		if (parameters != null)
			Arrays.stream(parameters).filter(parameter -> parameter.getIn().equals("query"))
					.forEach(parameter -> given.queryParam(parameter.getName(), parameter.getValue()));
		/** query parameters **/

		/** header parameters **/
		if (parameters != null)
			inHeader.removeAll(Arrays.asList(parameters));

		if (inHeader.size() > 0)
			inHeader.forEach(headerParam -> given.header(headerParam.getName(),
					headers.get(headerParam.getName()).getAsString()));
		// add any additional supplied parameter
		if (parameters != null)
			Arrays.stream(parameters).filter(parameter -> parameter.getIn().equals("header"))
					.forEach(parameter -> given.header(parameter.getName(), parameter.getValue()));
		/** header parameters **/

		
		/** Content-Type header **/
		if(parent==null || parent.get("consumes")==null)
			if(operation.getConsumes().size() > 0)
				given.contentType(operation.getConsumes().get(0));
			else
				throw new RuntimeException(String.format("No 'consumes:[]' content type header defined for the operation %s", (method.toString().toLowerCase()+":"+path)));
		else if (parent.get("consumes") != null)
			given.contentType(parent.get("consumes").getAsString());

		/** Content-Type header **/
		
		/** Accept header **/
		if(parent==null || parent.get("produces")==null)
			if(operation.getProduces().size() >0 )
				given.accept(operation.getProduces().get(0));
			else
				throw new RuntimeException(String.format("No 'produces:[]' content type header defined for the operation %s", (method.toString().toLowerCase()+":"+path)));
		else if (parent.get("produces") != null)
			given.accept(parent.get("produces").getAsString());
		
		/** Accept header **/
		
		
		if (method == HTTP_METHOD.GET)
			return given.when().get(location, pathParameters.toArray(new Object[] {}));

		else if (method == HTTP_METHOD.DELETE)
			return given.when().delete(location, pathParameters.toArray(new Object[] {}));

		else if (method == HTTP_METHOD.POST || method == HTTP_METHOD.PUT) {

			/** formData parameters **/
			if (parameters != null)
				formData.removeAll(Arrays.asList(parameters));

			if (formData.size() > 0)
				formData.forEach(
						formParam -> given.formParam(formParam.getName(), data.get(formParam.getName()).getAsString()));
			// add any additional supplied parameter
			if (parameters != null)
				Arrays.stream(parameters).filter(parameter -> parameter.getIn().equals("formData"))
						.forEach(parameter -> given.formParam(parameter.getName(), parameter.getValue()));
			/** formData parameters **/

			/** body **/
			Optional<Parameter> bodySupplied = Optional.empty();
			if (parameters != null)
				bodySupplied = Arrays.stream(parameters).filter(parameter -> parameter.getIn().equals("body"))
						.findAny();

			if (bodySupplied.isPresent())
				given.body(bodySupplied.get());
			else if (inBody)
				given.body(data.get("body"));
			/** body **/

			// call with path parameters
			if (method == HTTP_METHOD.POST)
				return given.when().post(location, pathParameters.toArray(new Object[] {}));
			if (method == HTTP_METHOD.PUT)
				return given.when().put(location, pathParameters.toArray(new Object[] {}));

		}
		throw new RuntimeException(String.format("HTTP method %s is not yet implemented", method));
	}

	/**
	 * Pushes a value in internal map for later user.
	 * @param key
	 * @param value
	 */
	protected void push(String key, Object value) {
		ScenarioRunner.savedData.put(key, value);
	}
	
	/**
	 * An alias of {@link vend.api.automation.ScenarioRunner#push(String key, Object value)}.
	 * @param key
	 * @return
	 */
	protected void set(String key, Object value) {
		ScenarioRunner.savedData.put(key, value);
	}

	/**
	 * Returns a keyed value from internal map without removing it.
	 * @param key
	 * @return
	 */
	protected Object peek(String key) {
		return ScenarioRunner.savedData.get(key);
	}

	/**
	 * Returns a keyed value from internal map and also removes it.
	 * @param key
	 * @return
	 */
	protected Object pop(String key) {
		Object value = ScenarioRunner.savedData.get(key);
		if (value != null)
			ScenarioRunner.savedData.remove(key);
		return value;
	}
	
	/**
	 * An alias of {@link vend.api.automation.ScenarioRunner#peek(String key)}.
	 * @param key
	 * @return
	 */
	protected Object get(String key) {
		return peek(key);
	}
}
