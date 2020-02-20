package vend.api.automation.tests;

import static org.hamcrest.Matchers.is;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import io.restassured.response.Response;
import vend.api.automation.Parameter;
import vend.api.automation.PreProcessor;
import vend.api.automation.ScenarioRunner;
import vend.api.automation.annotations.Scenario;

public class GetApiTest {

	static Logger logger = LoggerFactory.getLogger(GetApiTest.class);
	
	@BeforeAll
	public static void init() throws URISyntaxException {
		System.setProperty("swagger-file", new File(GetApiTest.class.getResource("/swagger.json").toURI()).getAbsolutePath());
	}
	
	@Test
	public void testSimpleGet() {
		
		String apiName = "get:/pet/{petId}";
		ScenarioRunnerExt ext = new ScenarioRunnerExt();
		Response response = ext.callApi(apiName);
		
		logger.info("Response received: {}", response.asString());
		
		response.then().body("id", is(ext.getData(apiName).get("petId").getAsInt()));	
	}
	
	@Test
	public void testSimpleGetProcessPayload() {
		Response response = new ScenarioRunnerExt().callApi("get:/pet/{petId}", data -> {
			data.addProperty("petId", 99);
		});
		logger.info("Response received: {}", response.asString());
		response.then().body("id", is(99));
	}
	
	@Scenario(resource = "get.res")
	public class ScenarioRunnerExt extends ScenarioRunner {
		
		public Response callApi(String apiName, Parameter ... parameters) {
			return super.callApi(apiName, parameters);
		}
		
		@Override
		public Response callApi(String apiName, PreProcessor preProcessor, Parameter... parameters) {
			return super.callApi(apiName, preProcessor, parameters);
		}
		
		@Override
		public JsonObject getData(String apiName) {
			return super.getData(apiName);
		}
	}
}
