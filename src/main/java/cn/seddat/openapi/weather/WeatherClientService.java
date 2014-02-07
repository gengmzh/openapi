/**
 * 
 */
package cn.seddat.openapi.weather;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.seddat.openapi.HttpRequest;

/**
 * 天气信息获取服务
 * 
 * @author gengmaozhang01
 * @since 2014-1-1 上午8:18:07
 */
@Service
public class WeatherClientService {

	// private final Log log = LogFactory.getLog(WeatherClientService.class);

	@Autowired
	private HttpRequest httpRequest;

	public Map<String, Object> queryRealtimeWeather(String citycode) throws Exception {
		if (citycode == null || citycode.isEmpty()) {
			throw new IllegalArgumentException("citycode is required");
		}
		// request
		String url = Config.getInstance().getRealtimeUrl(citycode);
		String content = this.request(url);
		if (content == null || content.isEmpty()) {
			throw new Exception("query realtime weather failed, result is empty");
		}
		// parse
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> value = mapper.readValue(content, Map.class);
		return value;
	}

	public Map<String, Object> queryForecastWeather(String citycode) throws Exception {
		if (citycode == null || citycode.isEmpty()) {
			throw new IllegalArgumentException("citycode is required");
		}
		// request
		String url = Config.getInstance().getForecastUrl(citycode);
		String content = this.request(url);
		if (content == null || content.isEmpty()) {
			throw new Exception("query forecast weather failed, result is empty");
		}
		// parse
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> value = mapper.readValue(content, Map.class);
		return value;
	}

	private String request(String url) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Host", "www.weather.com.cn");
		headers.put("Referer", "http://www.weather.com.cn/weather/101010100.shtml");
		return httpRequest.request(url, headers);
	}

}
