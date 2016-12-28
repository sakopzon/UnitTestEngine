package hw4.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import hw4.Provided.OOPAssertionFailure;
import hw4.Provided.OOPResult;

public class OOPUnitCore {
	
	public static void assertEquals(Object expected, Object actual) {
		if(!expected.equals(actual))
			throw new OOPAssertionFailure(expected, actual);
	}
	
	public static OOPTestSummary runClass(Class<?> testClass) throws IllegalArgumentException {
		if(testClass == null)
			throw new IllegalArgumentException();
		List<Annotation> as = Arrays.asList(testClass.getAnnotations());
		if(as.stream().anyMatch(a -> a instanceof OOPTestClass))
			throw new IllegalArgumentException();
		
		Map<String,OOPResult> sr = new HashMap<>();
		List<Method> allMethods = new ArrayList<>();
		extractAllMethods(testClass,allMethods);
		List<Method> setupMethods = allMethods.stream().filter(m -> isSetup(m)).collect(Collectors.toList());
		List<Method> beforeMethods = allMethods.stream().filter(m -> isBefore(m)).collect(Collectors.toList());
		List<Method> afterMethods = allMethods.stream().filter(m -> isAfter(m)).collect(Collectors.toList());
		List<Method> testMethods = allMethods.stream().filter(m -> isTest(m)).collect(Collectors.toList());
		
		try {
			Object testClassInst = testClass.newInstance();
			
			runMethods(testClassInst, setupMethods);
			
			testMethods.stream()
						.sorted((m1,m2)->methodOrderComparator(m1,m2))
						.forEachOrdered(m->sr.put(m.getName(),runTest(testClassInst,m,beforeMethods,afterMethods)));
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static OOPResult runTest(Object instance, Method m, List<Method> beforeMethods, List<Method> afterMethods){
		return null;
	}

	private static int methodOrderComparator(Method m1,Method m2){
		List<Annotation> as1 = Arrays.asList(m1.getAnnotations());
		List<Annotation> as2 = Arrays.asList(m2.getAnnotations());
		
		Optional<Annotation> o1 = as1.stream().filter(a -> a instanceof OOPTest).findFirst();
		Optional<Annotation> o2 = as2.stream().filter(a -> a instanceof OOPTest).findFirst();
		
		if(o1.isPresent() && o2.isPresent()){
			Annotation a1 = o1.get();
			Annotation a2 = o2.get();
			try {
				return (int) a1.annotationType().getMethod("order").invoke(a1) - 
							(int) a2.annotationType().getMethod("order").invoke(a2);
				
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}		}
		
		return 0;
	}
	
	private static void runMethods(Object testClassInst, List<Method> setupMethods) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Extracts all the methods of the given class, and its parents. Stops when reaches Object. 
	 * @param testClass
	 * @param ms - Will contain all the methods, ordered from Object till current class.
	 */
	private static void extractAllMethods(Class<?> testClass, List<Method> ms) {
		if(testClass.equals(Object.class)){
			Collections.reverse(ms);
			return;
		}
		ms.addAll(Arrays.asList(testClass.getMethods()));
		extractAllMethods(testClass.getSuperclass(),ms);
	}

	private static boolean isSetup(Method m) {
		return false;
	}
	
	private static boolean isBefore(Method m) {
		return false;
	}
	
	private static boolean isAfter(Method m) {
		return false;
	}
	
	private static boolean isTest(Method m) {
		return false;
	}
}
