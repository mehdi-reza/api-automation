package vend.api.automation;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface PreProcessor {
	
	public void process(JsonObject data);
	
	default public PreProcessor andThen(PreProcessor after) {
		return data -> { process(data); after.process(data);};
	}
}
