package vend.api.automation;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public class Definition {
	
	private String name;
	private String[] required;

	
	public Definition(String name, JsonObject definition) {
		this.name = name;
		List<String> attributes = new ArrayList<>();
		definition.get("required").getAsJsonArray().forEach(element -> attributes.add(element.getAsString()));
		this.required = attributes.toArray(new String[] {});
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getRequired() {
		return required;
	}

	public void setRequired(String[] required) {
		this.required = required;
	}
}
