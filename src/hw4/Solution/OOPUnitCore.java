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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
		
		return null;
	}
	
	private static OOPResult runTest(Object instance, Class<?> testClass, Method m, List<Method> beforeMethods, List<Method> afterMethods){
		boolean errorThrown = false;
		
		// find befores
		List<Method> applicableBeforeMethods = beforeMethods.stream()
													.filter(beforeMethod->!containsMethod(m,beforeMethod))
														.collect(Collectors.toList());
		// run befores
		for(Method beforeMethod : applicableBeforeMethods)
			if(runMethodWithBackup(instance,testClass,beforeMethod))
				errorThrown = true;

		//Logic that runs the actual test

		// find afters
		List<Method> applicableAfterMethods = afterMethods.stream()
													.filter(afterMethod->!containsMethod(m,afterMethod))
														.collect(Collectors.toList());
		// run afters
		for(Method afterMethod : applicableAfterMethods)
			if(runMethodWithBackup(instance,testClass,afterMethod))
				errorThrown = true;
		
		return null;
	}

	private static boolean containsMethod(Method m, Method beforeMethod) {
		return Arrays.asList(beforeMethod.getAnnotation(OOPBefore.class).value()).contains(m.getName());
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
	
	/**
	 * @param instance
	 * @param testClass
	 * @param m
	 * @return false <b>iff</b> method has thrown exception;
	 */
	private static boolean runMethodWithBackup(Object instance, Class<?> testClass, Method m){
		Object backup = backup(instance,testClass);
		try {
			m.invoke(instance);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			instance = backup;
			return false;
		}
		return true;
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
		
		for(Method m : Arrays.asList(testClass.getMethods()))
			if (!ms.stream().anyMatch(method -> method.getName().equals(m.getName())))
				ms.add(m);
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
