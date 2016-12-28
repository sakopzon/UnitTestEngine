package hw4.Solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OOPTest {

	boolean testThrows() default false;
	
	Class<?> exception() default Object.class;
	
	int order();
	
}
