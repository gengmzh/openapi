/**
 * 
 */
package cn.seddat.openapi.weather;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cn.seddat.openapi.BaseTest;

/**
 * @author gengmaozhang01
 * @since 2014-2-23 下午9:33:19
 */
public class AQIRefresherBaseTest extends BaseTest {

	@Autowired
	private AQIRefresher aqiRefresher;

	@Test
	public void test_refresh() throws Exception {
		// List<Config.City> aqiCities = Config.getInstance().getAllAQICities();
		aqiRefresher.run();
	}

}
