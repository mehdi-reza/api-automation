package vend.api.automation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;

public class Path {

	private String path;
	private List<String> parameters = new ArrayList<>();
	private Map<HTTP_METHOD, Operation> operations = new HashMap<>();

	public Path(String path, JsonObject details) {
		this.path = path;
		parseParameters();
		details.entrySet().forEach(entry -> {
			HTTP_METHOD method = HTTP_METHOD.valueOf(entry.getKey().toUpperCase());
			this.operations.put(method,
					new Operation(method, entry.getValue().getAsJsonObject()));
		});
	}

	private void parseParameters() {
		
		final String regex = "(\\{[a-zA-Z]+\\})";

		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(path);

		boolean matches = matcher.find();
		
		while(matches) {
			this.parameters.add(matcher.group().replace("{", "").replace("}", ""));
			matches = matcher.find();
		}
	}
	
	public List<String> getParameters() {
		return parameters;
	}

	public String getPath() {
		return path;
	}

	public Map<HTTP_METHOD, Operation> getOperations() {
		return operations;
	}
}
