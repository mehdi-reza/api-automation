package tez;

import static org.hamcrest.core.Is.is;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import vend.api.automation.ScenarioRunner;
import vend.api.automation.annotations.Scenario;

@Scenario(resource = "scenario.1.res")
@TestMethodOrder(OrderAnnotation.class)
public class TestScenario1 extends ScenarioRunner {

	private Logger logger = LoggerFactory.getLogger(TestScenario1.class);
	
	@Test
	@Order(2)
	public void testFindByStatus() {
		logger.info("testFindByStatus");
		
		logger.info("peek(\"name\") returns {}", peek("name"));
		
		String response = callApi("get:/pet/findByStatus").asString();
		logger.debug(response);
	}

	@Test
	@Order(1)
	public void testGetByPetId() {
		logger.info("testGetByPetId");
		
		// returns as restassured Response
		Response response = callApi("get:/pet/{petId}");
		
		// get a validatable response and assert
		response.then().body("name", is("Trump"));
		
		// save for later use in other test method
		push("name", response.getBody().as(JsonObject.class, ObjectMapperType.GSON).get("name").getAsString());
		
		logger.debug(response.asString());
	}

	@Test
	@Order(3)
	public void testUpdatetPet() {
		logger.info("testUpdatetPet");
		String response = callApi("post:/pet/{petId}").asString();
		logger.debug(response);
	}

	@Test
	@Order(4)
	public void testDeletePet() {
		logger.info("testDeletePet");
		String response = callApi("delete:/pet/{petId}").asString();
		logger.debug(response);
	}
	
	@Test
	@Order(5)
	public void testCreatePet() {
		logger.info("testCreatePet");
		String response = callApi("post:/pet").asString();
		logger.debug(response);
	}
}
