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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * cnpm25.cn站点接口AQI信息查询服务
 * 
 * @author gengmaozhang01
 * @since 2014-3-15 上午11:27:10
 */
@Service
public class CNPM25APIAQIQuery {

	private static final Log log = LogFactory.getLog(CNPM25APIAQIQuery.class);

	@Autowired
	private HttpRequest httpRequest;

	/**
	 * 抓取AQI数据
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-7 上午8:32:47
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> query(String citycode) throws Exception {
		if (citycode == null || citycode.isEmpty()) {
			throw new IllegalArgumentException("citycode is required");
		}
		// request
		String url = Config.getInstance().getCNPM25AQIAPI(citycode);
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
		Map<String, Object> jsonResult = mapper.readValue(content, Map.class);
		// city
		Object value = jsonResult.get("CityName");
		weatherinfo.put("AQI_city", value != null ? value.toString() : "");
		// aqi
		value = jsonResult.get("AQI");
		if (value != null) {
			String aqi = value.toString();
			int si = aqi.indexOf('_');
			if (si > -1) {
				aqi = aqi.substring(0, si);
			}
			weatherinfo.put("AQI", aqi);
		}
		// time
		value = jsonResult.get("UpDateTime");
		if (value != null) {
			String time = value.toString().replace('-', '.');
			weatherinfo.put("time", time);
		}
		// hourly
		Map<String, Object> trend = (Map<String, Object>) jsonResult.get("Tread");
		List<Map<String, String>> hourly = Lists.newArrayList();
		if (trend != null) {
			List<Object> hours = (List<Object>) trend.get("Date24hours"), aqis = (List<Object>) trend.get("AQI24hours");
			int size = Math.min(hours != null ? hours.size() : 0, aqis != null ? aqis.size() : 0);
			for (int i = 0; i < size; i++) {
				value = hours.get(i);
				String time = (value != null ? value.toString() : "").replace('-', '.');
				value = aqis.get(i);
				String aqi = (value != null ? value.toString() : "");
				hourly.add(ImmutableMap.of("time", time, "AQI", aqi));
			}
		}
		weatherinfo.put("hourly", hourly);
		if (!hourly.isEmpty()) { // fix current aqi
			weatherinfo.put("AQI", hourly.get(hourly.size() - 1).get("AQI"));
		}
		// daily
		List<Map<String, String>> daily = Lists.newArrayList();
		if (trend != null) {
			List<Object> days = (List<Object>) trend.get("Date30days"), aqis = (List<Object>) trend.get("AQI30days");
			int size = Math.min(days != null ? days.size() : 0, aqis != null ? aqis.size() : 0);
			for (int i = 0; i < size; i++) {
				value = days.get(i);
				String time = (value != null ? value.toString() : "").replace('-', '.');
				value = aqis.get(i);
				String aqi = (value != null ? value.toString() : "");
				daily.add(ImmutableMap.of("time", time, "AQI", aqi));
			}
		}
		weatherinfo.put("daily", daily);
		// result
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("weatherinfo", weatherinfo);
		log.info("AQI: " + result);
		return result;
	}

	private String request(String url) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Host", "appapi.cnpm25.cn");
		headers.put("Connection", "Keep-Alive");
		headers.put("User-Agent", "Apache-HttpClient/UNAVAILABLE (java 1.4)");
		headers.put("Accept-Charset", "UTF-8");
		return httpRequest.request(url, headers);
	}

}
