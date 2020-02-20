package vend.api.automation.tests;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import vend.api.automation.Parameter;
import vend.api.automation.ScenarioRunner;
import vend.api.automation.annotations.Scenario;

@TestInstance(Lifecycle.PER_CLASS)
public class GetApiTests {
	
	@BeforeAll
	public void init() throws URISyntaxException {
		System.out.println("init");
		System.setProperty("swagger-file", new File(GetApiTests.class.getResource("/swagger.json").toURI()).getAbsolutePath());
	}
	
	@Test
	public void simpleGetTest() {
		new ScenarioRunnerExt().invokeCallApi("get:/pet/{petId}");
	}
	
	@Scenario(resource = "get.res")
	public class ScenarioRunnerExt extends ScenarioRunner {
		
		public void invokeCallApi(String apiName, Parameter ... parameters) {
			super.callApi(apiName, parameters);
		}
	}
}
