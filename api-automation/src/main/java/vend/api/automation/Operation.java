package vend.api.automation;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

/**
 * 
 * @author Mehdi Raza
 *
 */
public class Operation {

	private List<String> consumes = new ArrayList<>();
	private List<String> produces = new ArrayList<>();

	private List<Parameter> parameters = new ArrayList<>();

	public Operation(HTTP_METHOD method, JsonObject details) {
		if (details.get("consumes") != null)
			details.get("consumes").getAsJsonArray().forEach(c -> consumes.add(c.getAsString()));

		if (details.get("produces") != null)
			details.get("produces").getAsJsonArray().forEach(p -> produces.add(p.getAsString()));
		
		setParameters(details);
	
	}

	protected void setParameters(JsonObject details) {
		
		if(details.get("parameters")==null) return;
		
		details.get("parameters").getAsJsonArray().forEach(element -> {
			
			Parameter parameter = new Parameter();
			parameter.setName(element.getAsJsonObject().get("name").getAsString());
			
			if(element.getAsJsonObject().get("type")!=null)
				parameter.setType(element.getAsJsonObject().get("type").getAsString());
			
			parameter.setIn(element.getAsJsonObject().get("in").getAsString());
			
			if(element.getAsJsonObject().get("format")!=null)
				parameter.setFormat(element.getAsJsonObject().get("format").getAsString());
			
			if(element.getAsJsonObject().get("required")!=null)
				parameter.setRequired(element.getAsJsonObject().get("required").getAsBoolean());
			
			if(element.getAsJsonObject().get("description")!=null)
				parameter.setDescription(element.getAsJsonObject().get("description").getAsString());

			if(parameter.getIn().equals("body")) {
				JsonObject schema = element.getAsJsonObject().get("schema").getAsJsonObject();
				
				if(schema.get("$ref") != null) {
					parameter.setDefinitionName(schema.get("$ref").getAsString().replace("#/definitions/", ""));
				} else if(schema.get("items") != null) {
					parameter.setDefinitionName(schema.get("items").getAsJsonObject().get("$ref").getAsString().replace("#/definitions/", ""));
				}
			}
			
			this.parameters.add(parameter);
		});
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}
	
	public List<String> getConsumes() {
		return consumes;
	}
	
	public List<String> getProduces() {
		return produces;
	}
}
