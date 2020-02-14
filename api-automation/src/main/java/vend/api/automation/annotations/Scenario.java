package vend.api.automation.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used to specify test data resource file
 * @author Mehdi Raza
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Scenario {
	/**
	 * Provide resource file for test data in src/test/resources
	 * @return
	 */
	String resource();
}
