package vend.api.automation;

public class Parameter {

	private String name;
	private String in;
	private boolean required = false;
	private String description;
	private String type;
	private String format;
	private String definitionName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIn() {
		return in;
	}

	public void setIn(String in) {
		this.in = in;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}
	
	public String getDefinitionName() {
		return definitionName;
	}
	
	@Override
	public String toString() {
		StringBuilder out =  new StringBuilder((required ? "*":"")+name).append(":").append(getType()==null ? "object":getType());
		
		out.append(":").append(getIn());
		
		if(getIn().equals("body"))
			out.append("#").append(getDefinitionName());
		
		return out.toString();
	}

}
