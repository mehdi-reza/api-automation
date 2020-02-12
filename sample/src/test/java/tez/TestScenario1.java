package tez;

import static org.hamcrest.core.Is.is;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		String response = callApi("get:/pet/findByStatus").asString();
		logger.debug(response);
	}

	@Test
	@Order(1)
	public void testGetByPetId() {
		logger.info("testGetByPetId");
		callApi("get:/pet/{petId}").then().body("name", is("Trump"));
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
