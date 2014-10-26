/**
 * 
 */
package cn.seddat.openapi.weather.aqi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * pm25.in重点的AQI接口调用服务
 * 
 * @author gengmaozhang01
 * @since 2014-3-16 下午6:06:14
 */
@Service
public class PM25INAQIQuery {

	private static final Log log = LogFactory.getLog(PM25INAQIQuery.class);

	@Autowired
	private HttpRequest httpRequest;

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
		try {
			List<Map<String, String>> hourly = this.queryHourly(citycode);
			weatherinfo.put("hourly", hourly);
		} catch (Exception ex) {
			weatherinfo.put("hourly", Lists.newArrayList());
			log.warn("query hourly aqi failed", ex);
		}
		// daily
		weatherinfo.put("daily", Lists.newArrayList());
		// result
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("weatherinfo", weatherinfo);
		log.info("AQI: " + result);
		return result;
	}

	private List<Map<String, String>> queryHourly(String citycode) throws Exception {
		// request
		String url = Config.getInstance().getPM25INAQIUrl(citycode);
		String content = this.request(url);
		if (content == null || content.isEmpty()) {
			throw new Exception("query AQI failed, result is empty");
		}
		// parse
		List<Map<String, String>> hourly = Lists.newArrayList();
		List<String> lines = Splitter.on('\n').trimResults().omitEmptyStrings().splitToList(content);
		for (String line : lines) {
			if (line.startsWith("data: [")) {
				log.info("hourly aqi: " + line);
				Calendar cal = Calendar.getInstance();
				DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:00");
				List<String> datas = Splitter.on(',').trimResults(CharMatcher.is('"'))
						.splitToList(line.substring(7, line.length() - 2));
				for (int i = datas.size() - 1; i >= 0; i--) {
					String data = datas.get(i);
					if (!data.isEmpty() && !"null".equalsIgnoreCase(data)) {
						cal.add(Calendar.HOUR_OF_DAY, -1);
						hourly.add(0, ImmutableMap.of("time", format.format(cal.getTime()), "AQI", data));
					}
				}
				break;
			}
		}
		return hourly;
	}

	private String request(String url) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Host", "www.pm25.in");
		headers.put("Accept-Charset", "UTF-8");
		return httpRequest.request(url, headers);
	}

}
