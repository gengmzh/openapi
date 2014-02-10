/**
 * 
 */
package cn.seddat.openapi.weather;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cn.seddat.openapi.BaseTest;

/**
 * @author gengmaozhang01
 * @since 2014-2-6 下午10:16:42
 */
public class AQIClientServiceBaseTest extends BaseTest {

	@Autowired
	private AQIClientService aqiClientService;

	@Test
	public void test_queryAirQualityIndex() throws Exception {

		Map<String, Object> aqi = aqiClientService.queryAirQualityIndex("101020200");

		Assert.assertNotNull(aqi);
		ObjectMapper mapper = new ObjectMapper();
		this.println(mapper.writeValueAsString(aqi));
	}

}
