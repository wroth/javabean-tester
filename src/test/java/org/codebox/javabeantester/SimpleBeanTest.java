package org.codebox.javabeantester;

import java.beans.IntrospectionException;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;


public class SimpleBeanTest
{
	private JavaBeanTester tester = new JavaBeanTester();
	private IMocksControl ctrl = EasyMock.createControl();
	
	@Test
	public void shouldExerciseGettersAndSetters() throws IntrospectionException {
		tester.test(ctrl, SimpleBean.class, "truth");  // test all members except 'truth', which acts strangely.
	}

}
