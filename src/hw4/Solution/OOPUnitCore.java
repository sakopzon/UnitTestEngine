package hw4.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import hw4.Provided.OOPAssertionFailure;
import hw4.Provided.OOPResult;
import hw4.Provided.OOPResult.OOPTestResult;

public class OOPUnitCore {
	
	public static void assertEquals(Object expected, Object actual) {
		if(!expected.equals(actual))
			throw new OOPAssertionFailure(expected, actual);
	}
	
	public static OOPTestSummary runClass(Class<?> testClass) throws IllegalArgumentException {
		if(testClass == null)
			throw new IllegalArgumentException();
		List<Annotation> as = Arrays.asList(testClass.getAnnotations());
		if(!as.stream().anyMatch(a -> a instanceof OOPTestClass))
			throw new IllegalArgumentException();
		
		Map<String,OOPResult> resultMap = new HashMap<>();
		List<Method> allMethods = new ArrayList<>();
		extractAllMethods(testClass,allMethods);
		List<Method> setupMethods = allMethods.stream().filter(m -> isSetup(m)).collect(Collectors.toList());
		List<Method> beforeMethods = allMethods.stream().filter(m -> isBefore(m)).collect(Collectors.toList());
		List<Method> afterMethods = allMethods.stream().filter(m -> isAfter(m)).collect(Collectors.toList());
		Collections.reverse(afterMethods);
		List<Method> testMethods = allMethods.stream().filter(m -> isTest(m)).collect(Collectors.toList());
		
		try {
			Object testClassInst = testClass.newInstance();
			
			runSetupMethods(testClassInst, setupMethods);
			
			testMethods.stream()
						.sorted((m1,m2)->methodOrderComparator(m1,m2))
						.forEachOrdered(m->resultMap.put(m.getName(),runTest(testClassInst,testClass,m,beforeMethods,afterMethods)));
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return new OOPTestSummary(resultMap);
	}
	
	private static void runMethodsWithBackup(Object instance, Class<?> testClass, List<Method> methods, OOPResultImpl $) {
		for(Method m : methods){
			Object backup = backup(instance,testClass);
			try {
				m.invoke(instance);
			} catch (Throwable e) {
				instance = backup;
				$.result = OOPTestResult.ERROR;
				$.message = e.getClass().getName();
			}
		}
	}
	
	private static OOPResult runTest(Object instance, Class<?> testClass, Method m, List<Method> beforeMethods, List<Method> afterMethods){
		OOPResultImpl $ = new OOPResultImpl();
		$.result = OOPTestResult.SUCCESS;
		// find relevant before methods.
		List<Method> applicableBeforeMethods = beforeMethods.stream()
													.filter(beforeMethod->containsBeforeMethod(m,beforeMethod))
														.collect(Collectors.toList());
		// run before methods.
		runMethodsWithBackup(instance, testClass, applicableBeforeMethods, $);

		try {
			m.invoke(instance);
		}
		catch(Throwable t){
			if(!m.getAnnotation(OOPTest.class).testThrows() || !m.getAnnotation(OOPTest.class).exception().isInstance(t.getCause()))
				if(t.getCause() instanceof OOPAssertionFailure && $.result != OOPTestResult.ERROR){
					$.result = OOPTestResult.FAILURE;
					$.message = t.getMessage();
				}
				else{
					$.result = OOPTestResult.ERROR;
					$.message = t.getClass().getName();
				}
		}

		// find afters
		List<Method> applicableAfterMethods = afterMethods.stream()
													.filter(afterMethod->containsAfterMethod(m,afterMethod))
														.collect(Collectors.toList());
		// run afters
		runMethodsWithBackup(instance, testClass, applicableAfterMethods, $);
		
		return $;
	}

	// :(
	private static boolean containsBeforeMethod(Method m, Method beforeMethod) {
		return Arrays.asList(beforeMethod.getAnnotation(OOPBefore.class).value()).contains(m.getName());
	}

	// :(
	private static boolean containsAfterMethod(Method m, Method afterMethod) {
		return Arrays.asList(afterMethod.getAnnotation(OOPAfter.class).value()).contains(m.getName());
	}

	private static int methodOrderComparator(Method m1,Method m2){
		List<Annotation> as1 = Arrays.asList(m1.getAnnotations());
		List<Annotation> as2 = Arrays.asList(m2.getAnnotations());
		
		Optional<Annotation> o1 = as1.stream().filter(a -> a instanceof OOPTest).findFirst();
		Optional<Annotation> o2 = as2.stream().filter(a -> a instanceof OOPTest).findFirst();
		
		if (!o1.isPresent() || !o2.isPresent())
			assert false : "The unoptional optionals in methodOrderComparator were indeed optional.";
		else {
			Annotation a1 = o1.get();
			Annotation a2 = o2.get();
			try {
				return (int) a1.annotationType().getMethod("order").invoke(a1)
						- (int) a2.annotationType().getMethod("order").invoke(a2);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	
	private static Object backup(Object instance, Class<?> testClass) {
		
		assert instance != null : "Something is really fucked up. SOS";
		
		List<Field> fs = Arrays.asList(testClass.getFields());
		
		try {
			Object $ = testClass.newInstance();
			for(Field f : fs){
				try {
					f.set($, f.getType().getMethod("clone").invoke(f.get(instance)));
					continue;
				} catch (NoSuchMethodException e) {
					//Continue to next option.
					//Other catch clauses shouldn't happen.
				} 
				
				try {
					f.set($, f.getType().getConstructor(f.getType()).newInstance(f.get(instance)));
					continue;
				} catch (NoSuchMethodException e) {
					//Continue to next option					
					//Other catch clauses shouldn't happen.
				}
				
				f.set($, f.get(instance));
			}
			
			return $;
		} catch (InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private static void runSetupMethods(Object testClassInst, List<Method> setupMethods) {
		for(Method m : setupMethods)
			try {
				m.invoke(testClassInst);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
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
		
		for(Method m : Arrays.asList(testClass.getDeclaredMethods()))
			if (!ms.stream().anyMatch(method -> method.getName().equals(m.getName()))){
				m.setAccessible(true);
				ms.add(m);
			}	
		extractAllMethods(testClass.getSuperclass(),ms);
	}

	private static boolean isSetup(Method m) {
		return Arrays.asList(m.getAnnotations()).stream().filter(a -> a instanceof OOPSetup).findFirst().isPresent();
	}
	
	private static boolean isBefore(Method m) {
		return Arrays.asList(m.getAnnotations()).stream().filter(a -> a instanceof OOPBefore).findFirst().isPresent();
	}
	
	private static boolean isAfter(Method m) {
		return Arrays.asList(m.getAnnotations()).stream().filter(a -> a instanceof OOPAfter).findFirst().isPresent();
	}
	
	private static boolean isTest(Method m) {
		return Arrays.asList(m.getAnnotations()).stream().filter(a -> a instanceof OOPTest).findFirst().isPresent();
	}
}
