# api-automation

It uses swagger.json to find out all available operations and required parameters.

See sample/src/test/java/tez/TestScenario1.java. The test data is defined in src/test/resources/scenario.1.res and annotated in test class.

A JUnit test is defined as following:

```
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
  
}
```

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
