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
    	"headers": {
    		
    	},
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