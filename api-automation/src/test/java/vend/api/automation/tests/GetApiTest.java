package vend.api.automation.tests;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vend.api.automation.Parameter;
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
		new ScenarioRunnerExt().invokeCallApi("get:/pet/{petId}");
	}
	
	@Scenario(resource = "get.res")
	public class ScenarioRunnerExt extends ScenarioRunner {
		
		public void invokeCallApi(String apiName, Parameter ... parameters) {
			super.callApi(apiName, parameters);
		}
	}
}
