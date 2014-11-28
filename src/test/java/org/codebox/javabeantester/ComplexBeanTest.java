package org.codebox.javabeantester;

import static org.junit.Assert.assertEquals;

import java.beans.IntrospectionException;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;


public class ComplexBeanTest
{
	private JavaBeanTester tester = new JavaBeanTester();
	private IMocksControl ctrl = EasyMock.createControl();
	
	@Test
	public void shouldExerciseGettersAndSetters() throws IntrospectionException {
		tester.test(ctrl, ComplexBean.class);
	}
	
	@Test
	public void shouldManuallyTestMethods_thatJavaBeanTesterCannotHandle() {
		ComplexBean bean = new ComplexBean();
		bean.setBizarre("abc", 17);  // 2 arg "setter".  No good for JavaBeanTester.
		assertEquals ("abc17", bean.getBizarre());
	}

}
