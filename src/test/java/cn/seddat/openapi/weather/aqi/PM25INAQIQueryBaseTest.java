/**
 * 
 */
package cn.seddat.openapi.weather.aqi;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cn.seddat.openapi.BaseTest;

/**
 * @author gengmaozhang01
 * @since 2014-3-16 下午9:42:25
 */
public class PM25INAQIQueryBaseTest extends BaseTest {

	@Autowired
	private PM25INAQIQuery pm25inaqiQuery;

	@Test
	public void test_query() throws Exception {
		String citycode = "101010700";
		Map<String, Object> aqi = pm25inaqiQuery.query(citycode);

		Assert.assertNotNull(aqi);
		this.println(aqi);
	}

}
