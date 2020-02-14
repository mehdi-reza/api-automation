package vend.api.automation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * 
 * @author Mehdi Raza
 *
 */
public class Automate {

	private Logger logger = LoggerFactory.getLogger(Automate.class);

	List<Path> apis = new ArrayList<>();
	List<Definition> definitions = new ArrayList<Definition>();

	String host;
	String basePath;
	String scheme;

	public static void main(String[] args) {

		File swagger = null;
		if (System.getProperties().get("swagger-file") != null) {
			swagger = new File(System.getProperties().get("swagger-file").toString());
		} else if (args.length >= 1) {
			swagger = new File(args[0]);
		}

		if (swagger == null || !swagger.exists())
			throw new RuntimeException("Could not find swagger documentation resource");

		new Automate(swagger);
	}
	
	public Automate(URL url) {
		URLConnection c = null;
		try {
			c = url.openConnection();
			c.connect();
			try (Reader reader = new InputStreamReader(c.getInputStream())) {
				read(reader);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void read(Reader reader) {
		
		JsonObject apiDoc = new GsonBuilder().create().fromJson(reader, JsonObject.class);

		readPaths(apiDoc);
		readDefinitions(apiDoc);
		printSummary();

		this.host = apiDoc.get("host").getAsString();

		// override if defined in system property
		if(System.getProperty("host") != null)
			this.host = System.getProperty("host");
		
		if (apiDoc.get("basePath") != null)
			this.basePath = apiDoc.get("basePath").getAsString();
		
		if (apiDoc.get("schemes") != null)
			this.scheme = apiDoc.get("schemes").getAsJsonArray().get(0).getAsString();
		else
			throw new RuntimeException("No schemes defined in swagger");
	}
	
	public Automate(File swagger) {
		try (FileReader reader = new FileReader(swagger)) {
			read(reader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void readDefinitions(JsonObject apiDoc) {
		JsonObject definitions = apiDoc.get("definitions").getAsJsonObject();

		definitions.entrySet().forEach(entry -> {
			// if a required attribute is defined for this definition
			if (entry.getValue().getAsJsonObject().get("required") != null) {
				this.definitions.add(new Definition(entry.getKey(), entry.getValue().getAsJsonObject()));
			}
		});
	}

	private void readPaths(JsonObject apiDoc) {
		JsonObject apis = apiDoc.get("paths").getAsJsonObject();

		apis.entrySet().forEach(entry -> {
			this.apis.add(new Path(entry.getKey(), entry.getValue().getAsJsonObject()));
		});
	}

	private void printSummary() {

		this.apis.forEach(path -> {
			logger.debug(path.getPath());

			path.getOperations().entrySet().forEach(entry -> {
				logger.debug("\t" + entry.getKey() + "\n");
				logger.debug("\t\tParameters:" + entry.getValue().getParameters());
			});
		});
	}

	public String getHost() {
		return host;
	}

	public String getBasePath() {
		return basePath;
	}
	
	public String getScheme() {
		return scheme;
	}
	
	public Operation findOperation(HTTP_METHOD method, final String path) {
		Optional<Path> api = this.apis.stream().filter(_api -> _api.getPath().equals(path)).findFirst();
		if(!api.isPresent() || api.get().getOperations().get(method) == null)
			throw new RuntimeException(String.format("No such operation \"%s\" found in swagger", (method.toString().toLowerCase()+":"+path)));
		return api.get().getOperations().get(method);
	}
}
