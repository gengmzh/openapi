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
 * @since 2014-2-22 上午11:30:09
 */
public class WeatherClientBaseTest extends BaseTest {

	@Autowired
	private WeatherClient weatherClientService;

	@Test
	public void test_queryRealtimeWeather() throws Exception {
		String citycode = "101010100";
		Map<String, Object> weather = weatherClientService.queryRealtimeWeather(citycode);

		Assert.assertNotNull(weather);
		ObjectMapper mapper = new ObjectMapper();
		this.println(mapper.writeValueAsString(weather));
	}

	@Test
	public void test_queryForecastWeather() throws Exception {
		String citycode = "101010100";
		Map<String, Object> weather = weatherClientService.queryForecastWeather(citycode);

		Assert.assertNotNull(weather);
		ObjectMapper mapper = new ObjectMapper();
		this.println(mapper.writeValueAsString(weather));
	}

}
