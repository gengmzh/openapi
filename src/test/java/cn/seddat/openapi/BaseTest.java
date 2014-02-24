/**
 * 
 */
package cn.seddat.openapi;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author gengmaozhang01
 * @since 2013-5-14 下午11:18:40
 * 
 */
@ContextConfiguration(locations = { "classpath:/spring-mvc.xml", "classpath:/spring-weather.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseTest {

	protected void println(Object... values) {
		for (int i = 0; i < values.length; i++) {
			System.out.println(values[i]);
		}
	}

}
