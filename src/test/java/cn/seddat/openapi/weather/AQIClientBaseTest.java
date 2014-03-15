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
public class AQIClientBaseTest extends BaseTest {

	@Autowired
	private AQIClient aqiClient;

	@Test
	public void test_queryAirQualityIndex() throws Exception {
		Map<String, Object> aqi = aqiClient.queryAirQualityIndex("101010700");
		aqi = aqiClient.queryAirQualityIndex("101010700");

		Assert.assertNotNull(aqi);
		ObjectMapper mapper = new ObjectMapper();
		this.println(mapper.writeValueAsString(aqi));
	}

}
