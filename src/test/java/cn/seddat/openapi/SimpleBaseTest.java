/**
 * 
 */
package cn.seddat.openapi;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author gengmaozhang01
 * @since 2014-2-23 下午8:38:22
 */
public class SimpleBaseTest {

	@Test
	public void test_spring() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-weather.xml");

		Object scheduler = context.getBean("weatherScheduler");
		System.out.println(scheduler.getClass().getName());
	}

}
