# api-automation

[![CircleCI](https://circleci.com/gh/mehdi-reza/api-automation.svg?style=shield)](https://circleci.com/gh/mehdi-reza/api-automation) [![Coverage Status](https://coveralls.io/repos/github/mehdi-reza/api-automation/badge.svg)](https://coveralls.io/github/mehdi-reza/api-automation)

It uses swagger.json to find out all available operations and required parameters.

See sample/src/test/java/tez/TestScenario1.java. The test data is defined in src/test/resources/scenario.1.res and annotated in test class.

A JUnit test is defined as following:

```
@Scenario(resource = "scenario.1.res")
@TestMethodOrder(OrderAnnotation.class)
public class TestScenario1 extends ScenarioRunner {

  private Logger logger = LoggerFactory.getLogger(TestScenario1.class);
  
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
  @Order(2)
  public void testFindByStatus() {
    logger.info("testFindByStatus");
		
    logger.info("peek(\"name\") returns {}", peek("name"));
		
    String response = callApi("get:/pet/findByStatus").asString();
    logger.debug(response);
  }
  
}
```
Read further on assertions https://github.com/rest-assured/rest-assured/wiki/usage

## Test data

```
[
    "post:/pet/{petId}/uploadImage",
    {
        "consumes": "application/json",
    	"produces": "application/json",
    	"headers": {},
        "data": {
	  "petId": 1,
	  "name": "Pet name"
        }
    },
    
    "get:/pet/{petId}", 
    {
    	"consumes": "application/json",
    	"produces": "application/json",
	"headers": {},
	"data": {
	  "petId": 509194403
	}
    },
    
    "get:/pet/findByStatus", 
	{
	"consumes": "application/json",
    	"produces": "application/json",
    	"headers": {},
	"data": {
	  "status": "sold"
	}
    },
    
    "post:/pet/{petId}",
    {
    	"consumes": "application/json",
    	"produces": "application/json",
	"headers": {},
	"data": {
	  "petId": 509194403,
	  "name": "Trump",
	  "status":"available"
    	}
    },
    
    "delete:/pet/{petId}",
    {
    	"consumes": "application/json",
    	"produces": "application/json",
    	"headers": {
    	  "api_key":"blah blah"
    	},
    	"data": {
    	  "petId": 1964
    	}
    },
    
    "post:/pet",
    {
    	"consumes": "application/json",
    	"produces": "application/json",
    	"headers": {},
    	"data": {
    	  "body": {
    	    "type": "dog",
    	    "name": "doggie",
    	    "photoUrls": ["http://bit.li/1","http://bit.li/2"],
    	    "status":"sold"
    	  }
    	}
    }
]
```

## Run tests

It reads the host from swagger.json, but you can override it with system property.

### Using swagger file
```mvn clean -Dswagger-file=/Users/vd-mehdi/TEMP/swagger.json -Dhost=https://petstore.swagger.io test```

### Using swagger url
```mvn clean -Dswagger-url=https://petstore.swagger.io/v2/swagger.json -Dhost=https://petstore.swagger.io test```
