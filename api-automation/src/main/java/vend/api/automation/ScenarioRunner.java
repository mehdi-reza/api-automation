package vend.api.automation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	private static Map<String, Object> savedData=new HashMap<>();

	public ScenarioRunner() {
		
		if (ScenarioRunner.automate == null) {
			if(System.getProperties().get("swagger-file")!=null)
				ScenarioRunner.automate = new Automate(new File(System.getProperties().get("swagger-file").toString()));
			else if(System.getProperties().get("swagger-url")!=null) {
				try {
					ScenarioRunner.automate = new Automate(new URL(System.getProperties().get("swagger-url").toString()));
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
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private JsonArray readResource(String resource) throws IOException {
		
		try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(resource)) {
			return new GsonBuilder().create().fromJson(new InputStreamReader(stream), JsonArray.class);
		}
	}

	protected Response callApi(String apiName, Parameter ...parameters) {

		String[] methodAndPath = apiName.split(":");
		
		HTTP_METHOD method = HTTP_METHOD.valueOf(methodAndPath[0].toUpperCase());
		String path = methodAndPath[1];
	
		Operation operation = automate.findOperation(method, path);
		
		List<Parameter> inPath = operation.getParameters().stream().filter(parameter -> parameter.getIn().equals("path")).collect(Collectors.toList());
		List<Parameter> inHeader = operation.getParameters().stream().filter(parameter -> parameter.getIn().equals("header")).collect(Collectors.toList());
		List<Parameter> query = operation.getParameters().stream().filter(parameter -> parameter.getIn().equals("query")).collect(Collectors.toList());
		List<Parameter> formData = operation.getParameters().stream().filter(parameter -> parameter.getIn().equals("formData")).collect(Collectors.toList());
		
		boolean inBody = operation.getParameters().stream().anyMatch(parameter -> parameter.getIn().equals("body"));
				
		int index=0;
		for(Iterator<JsonElement> i = resource.iterator(); i.hasNext();) {
			JsonElement element = i.next();
			if(element.isJsonPrimitive() && element.getAsString().equals(apiName)) {
				break;
			} index++;
		}
		
		final JsonObject parent = resource.get(index+1).getAsJsonObject();
		final JsonObject data = parent.getAsJsonObject("data");
		final JsonObject headers = parent.getAsJsonObject("headers");
		
		// there are some path parameters
		List<Object> pathParameters = inPath.stream().map(parameter -> data.get(parameter.getName()).getAsString()).collect(Collectors.toList());
		
		RequestSpecification given = RestAssured.given();
		
		String location = automate.host+automate.basePath+path;
		
		// set query params
		if(query.size()>0)
			query.forEach(queryParam -> given.queryParam(queryParam.getName(), data.get(queryParam.getName()).getAsString()));
		
		// set headers
		if(inHeader.size()>0)
			inHeader.forEach(headerParam -> given.header(headerParam.getName(), headers.get(headerParam.getName()).getAsString()));
		
		if(parent.get("consumes")!=null)
			given.contentType(parent.get("consumes").getAsString());
		
		if(parent.get("produces")!=null)
			given.accept(parent.get("produces").getAsString());
		
		if(method == HTTP_METHOD.GET)
			return given.when().get(location, pathParameters.toArray(new Object[] {}));
		
		else if (method == HTTP_METHOD.DELETE)
			return given.when().delete(location, pathParameters.toArray(new Object[] {}));
		
		else if(method == HTTP_METHOD.POST) {
						
			// set form data
			if(formData.size()>0)
				formData.forEach(formParam -> given.formParam(formParam.getName(), data.get(formParam.getName()).getAsString()));
			if(inBody)
				given.body(data.get("body"));
			
			// call with path parameters
			return given.when().post(location, pathParameters.toArray(new Object[] {}));
		}
		return null;
	}
	
	protected void push(String key, Object value) {
		ScenarioRunner.savedData.put(key, value);
	}
	
	protected Object peek(String key) {
		return ScenarioRunner.savedData.get(key);
	}
	
	protected Object pop(String key) {
		Object value = ScenarioRunner.savedData.get(key);
		if(value!=null)
			ScenarioRunner.savedData.remove(key);
		return value;
	}
	
}
