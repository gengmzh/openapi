/**
 * 
 */
package cn.seddat.openapi.weather.aqi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.seddat.openapi.HttpRequest;
import cn.seddat.openapi.weather.Config;
import cn.seddat.openapi.weather.EasyCache;

import com.google.common.collect.Lists;

/**
 * @author gengmaozhang01
 * @since 2014-3-16 下午6:06:14
 */
@Service
public class PM25INAQIQuery {

	private static final Log log = LogFactory.getLog(PM25INAQIQuery.class);

	@Autowired
	private HttpRequest httpRequest;
	@Autowired
	private EasyCache easyCache;

	private long cacheSeconds = 50 * 60;

	/**
	 * 抓取pm25.in站点的AQI信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-3-16 下午6:07:28
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> query(String citycode) throws Exception {
		if (citycode == null || citycode.isEmpty()) {
			throw new IllegalArgumentException("citycode is required");
		}
		// cache
		String key = "pm25-" + citycode;
		Map<String, Object> result = easyCache.get(key);
		if (result != null) {
			log.info("cache pm25.in AQI: " + result);
			return result;
		}
		// request
		String url = Config.getInstance().getPM25INAQIAPI(citycode);
		String content = this.request(url);
		if (content == null || content.isEmpty()) {
			throw new Exception("query AQI failed, result is empty");
		}
		// log.info("AQI content: " + content);
		// parse
		Map<String, Object> weatherinfo = new HashMap<String, Object>();
		weatherinfo.put("cityid", citycode);
		weatherinfo.put("city", "");
		ObjectMapper mapper = new ObjectMapper();
		List<Object> list = mapper.readValue(content, List.class);
		Map<String, Object> jsonResult = (Map<String, Object>) list.get(list.size() - 1);
		// city
		Object value = jsonResult.get("area");
		weatherinfo.put("AQI_city", value != null ? value.toString() : "");
		// aqi
		value = jsonResult.get("aqi");
		if (value != null) {
			String aqi = value.toString();
			weatherinfo.put("AQI", aqi);
		}
		// time
		value = jsonResult.get("time_point");
		if (value != null) {
			String time = value.toString().substring(0, 16).replace('-', '.').replace('T', ' ');
			weatherinfo.put("time", time);
		}
		// hourly
		weatherinfo.put("hourly", Lists.newArrayList());
		// daily
		weatherinfo.put("daily", Lists.newArrayList());
		// result
		result = new HashMap<String, Object>();
		result.put("weatherinfo", weatherinfo);
		easyCache.set(key, result, cacheSeconds);
		log.info("AQI: " + result);
		return result;
	}

	private String request(String url) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Host", "www.pm25.in");
		headers.put("Accept-Charset", "UTF-8");
		return httpRequest.request(url, headers);
	}

}
