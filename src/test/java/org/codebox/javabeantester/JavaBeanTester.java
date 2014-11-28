package org.codebox.javabeantester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.easymock.IMocksControl;
import org.junit.Ignore;


/**
 * This helper class can be used to unit test the get/set methods of JavaBean-style Value Objects.
 * 
 * @author Rob Dawson <rjdawson@gmail.com>, original version released under MIT license, confirmed by email 2014-11-26.
 * 
 * Modifications by Charles Roth, 2014-11-26, also released under MIT license.
 * 1. Remove static, require instantiation of JavaBeanTester.  This allows us to override class for step 6 below.
 * 2. Filter the skipThese member names in a method, rather than a go-to-like break.
 * 3. Add List and Map as recognized constructable elements.
 * 4. Move the no-arg constructor check near the bottom, it works better there (at the top it overrides String and others).
 * 5. Add explicit dependency on EasyMock, use it to try to construct a mock if all else fails.
 * 6. Add projectSpecificClassMaker(), so that subclasses can extend JavaBeanTester, and add their own smarts
 *    about classes that can be created.
 * 7. Simplified if/else-if, since else not needed.
 */

@Ignore  // So that ant/junit doesn't complain that there are no tests in here!

public class JavaBeanTester
{

	/**
	 * Tests the get/set methods of the specified class.
	 * 
	 * @param ctrl EasyMock controller, supplied by the caller (just in case).
	 * @param <T> the type parameter associated with the class under test
	 * @param clazz the Class under test
	 * @param skipThese the names of any properties that should not be tested
	 * @throws IntrospectionException thrown if the Introspector.getBeanInfo() method throws this exception for the
	 *             class under test
	 */
	public <T> void test(IMocksControl ctrl, final Class<T> clazz, final String... skipThese) throws IntrospectionException {
		for (PropertyDescriptor prop : makeFilteredPropertyDescriptors(clazz, skipThese)) {
			findBooleanIsMethods(clazz, prop);
			final Method getter = prop.getReadMethod();
			final Method setter = prop.getWriteMethod();

			if (getter != null && setter != null) {
				// We have both a get and set method for this property
				final Class<?> returnType = getter.getReturnType();
				final Class<?>[] params = setter.getParameterTypes();

				if (params.length == 1 && params[0] == returnType) {
					// The set method has 1 argument, which is of the same type as the return type of the get method, so
					// we can test this property
					try {
						// Build a value of the correct type to be passed to the set method
						Object value = buildValue(ctrl, returnType);

						// Build an instance of the bean that we are testing (each property test gets a new instance)
						T bean = clazz.newInstance();

						// Call the set method, then check the same value comes back out of the get method
						ctrl.replay();
						setter.invoke(bean, value);

						final Object expectedValue = value;
						final Object actualValue = getter.invoke(bean);

						assertEquals(String.format("Failed while testing property %s", prop.getName()), expectedValue, actualValue);
						ctrl.verify();
						ctrl.reset();

					} catch (Exception ex) {
						fail(String.format("An exception was thrown while testing the property %s: %s", prop.getName(), ex.toString()));
					}
				}
			}
		}
	}

	private List<PropertyDescriptor> makeFilteredPropertyDescriptors(Class clazz, String... skipThese) throws IntrospectionException {
		List<PropertyDescriptor> results = new ArrayList<PropertyDescriptor>();
		String commaSepSkipClasses = "," + StringUtils.join(skipThese, ",") + ",";
		for (PropertyDescriptor descriptor : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
			if (!commaSepSkipClasses.contains("," + descriptor.getName() + ",")) {
				results.add(descriptor);
			}
		}
		return results;
	}

	private Object buildValue(IMocksControl ctrl, Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException,
			InvocationTargetException
	{
		if (clazz == String.class)
			return new String("testvalue");

		if (clazz.isArray())
			return Array.newInstance(clazz.getComponentType(), 1);

		if (clazz == boolean.class || clazz == Boolean.class)
			return true;

		if (clazz == int.class || clazz == Integer.class)
			return 1;

		if (clazz == long.class || clazz == Long.class)
			return 1L;

		if (clazz == double.class || clazz == Double.class)
			return 1.0D;

		if (clazz == float.class || clazz == Float.class)
			return 1.0F;

		if (clazz == char.class || clazz == Character.class)
			return 'Y';

		if (clazz.isEnum())
			return clazz.getEnumConstants()[0];
		
		if (clazz == List.class)
			return new ArrayList();
		
		if (clazz == Map.class)
			return new HashMap();
		
		// Handle any classes that have a no-arg constructor.
		final Constructor<?>[] ctrs = clazz.getConstructors();
		for (Constructor<?> ctr : ctrs) {
			if (ctr.getParameterTypes().length == 0) {
				return ctr.newInstance();
			}
		}
		
		Object resultingClass = projectSpecificClassMaker(clazz);
		if (resultingClass != null)
			return resultingClass;

		// Last resort, try mocking the class.  But warn user, in case there's a better way.
		try {
			System.err.println ("JavaBeanTester: making a mock for " + clazz);
			return ctrl.createMock(clazz);
		} catch (Exception e) {
			fail("Unable to build an instance of class " + clazz.getName() + ", please add some code to the " + JavaBeanTester.class.getName() +
					" class to do this.");
		}
		return null;
	}
	
	// Override as needed for subclasses for different projects.
	protected Object projectSpecificClassMaker(Class clazz) {
		return null;
	}

	/**
	 * Hunt down missing Boolean read method if needed as Introspector cannot find 'is' getters for Boolean type
	 * properties.
	 * 
	 * @param clazz the type being introspected
	 * @param descriptor the property descriptor found so far
	 */
	public <T> void findBooleanIsMethods(Class<T> clazz, PropertyDescriptor descriptor) throws IntrospectionException {
		if (needToFindReadMethod(descriptor)) {
			findTheReadMethod(descriptor, clazz);
		}
	}

	private boolean needToFindReadMethod(PropertyDescriptor property) {
		return property.getReadMethod() == null && property.getPropertyType() == Boolean.class;
	}

	private <T> void findTheReadMethod(PropertyDescriptor descriptor, Class<T> clazz) {
		try {
			PropertyDescriptor pd = new PropertyDescriptor(descriptor.getName(), clazz);
			descriptor.setReadMethod(pd.getReadMethod());
		} catch (IntrospectionException e) {}
	}
}
