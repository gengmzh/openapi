/**
 * 
 */
package cn.seddat.openapi.weather;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cn.seddat.openapi.BaseTest;

/**
 * @author gengmaozhang01
 * @since 2014-2-23 下午10:15:11
 */
public class WeatherRefresherBaseTest extends BaseTest {

	@Autowired
	private WeatherRefresher weatherRefresher;

	@Test
	public void test_refresh() throws Exception {
		weatherRefresher.run();
	}

}
