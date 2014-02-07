/**
 * 
 */
package cn.seddat.openapi.weather;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.seddat.openapi.HttpRequest;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * 空气质量指数服务
 * 
 * @author gengmaozhang01
 * @since 2014-2-6 下午10:05:06
 */
@Service
public class AQIClientService {

	@Autowired
	private HttpRequest httpRequest;

	private final Map<String, String> AQICities = new HashMap<String, String>();

	public AQIClientService() throws Exception {
		BufferedReader reader = null;
		try {
			InputStream ins = Config.class.getClassLoader().getResourceAsStream(
					"cn/seddat/openapi/weather/city.properties");
			reader = new BufferedReader(new InputStreamReader(ins));
			String c1 = null, c2 = null;
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] ls = line.split("\t");
				if (ls.length < 2) {
					continue;
				}
				String id = ls[0], /* name = ls[1], */aqiCity = (ls.length > 2 ? ls[2] : null);
				if (aqiCity == null || aqiCity.isEmpty()) {
					if (c2 != null) {
						aqiCity = AQICities.get(c2);
					}
					if ((aqiCity == null || aqiCity.isEmpty()) && c1 != null) {
						aqiCity = AQICities.get(c1);
					}
				}
				AQICities.put(id, aqiCity);
				if (id.length() == 5) {
					c1 = id;
				} else if (id.length() == 7) {
					c2 = id;
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * 抓取AQI数据
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-7 上午8:32:47
	 */
	public Map<String, Object> queryAirQualityIndex(String citycode) throws Exception {
		if (citycode == null || citycode.isEmpty()) {
			throw new IllegalArgumentException("citycode is required");
		}
		String aqiCity = AQICities.get(citycode);
		if (aqiCity == null || aqiCity.isEmpty()) {
			throw new IllegalArgumentException("citycode " + citycode + " is illegal, can't find AQI city name");
		}
		// request
		String url = Config.getInstance().getAQIUrl(aqiCity);
		String content = this.request(url);
		if (content == null || content.isEmpty()) {
			throw new Exception("query AQI failed, result is empty");
		}
		// parse
		Map<String, Object> weatherinfo = new HashMap<String, Object>();
		weatherinfo.put("cityid", citycode);
		weatherinfo.put("city", "");
		weatherinfo.put("AQI_city", aqiCity);
		final List<String> lines = Splitter.on('\n').trimResults().omitEmptyStrings().splitToList(content);
		// 当前检测时间
		for (String line : lines) {
			if (line.contains("更新时间：")) {
				int idx = line.indexOf("更新时间：") + 5;
				String time = line.substring(idx, idx + 16);
				weatherinfo.put("time", time);
				break;
			}
		}
		// 最近24小时AQI、最近14天AQI
		List<Map<String, String>> hourly = Lists.newArrayList(), daily = Lists.newArrayList();
		int hc = 0/* , dc = 0 */;
		for (String line : lines) {
			if (line.startsWith("flashvalue")) {
				if (line.contains("日") && line.contains("时")) { // 最近24小时AQI
					int si = line.indexOf("name='") + 6, ei = line.indexOf("'", si);
					String time = this.parseHourly(line.substring(si, ei));
					si = line.indexOf("value='") + 7;
					ei = line.indexOf("'", si);
					String value = line.substring(si, ei);
					hourly.add(ImmutableMap.of("time", time, "AQI", value));
					if (++hc == 24) { // 当前AQI
						si = line.indexOf("value='") + 7;
						ei = line.indexOf("'", si);
						weatherinfo.put("AQI", line.substring(si, ei));
					}
				}
				if (line.contains("月") && line.contains("日")) { // 最近14天AQI
					int si = line.indexOf("name='") + 6, ei = line.indexOf("'", si);
					String time = this.parseDaily(line.substring(si, ei));
					si = line.indexOf("value='") + 7;
					ei = line.indexOf("'", si);
					String value = line.substring(si, ei);
					daily.add(ImmutableMap.of("time", time, "AQI", value));
				}
			}
		}
		weatherinfo.put("hourly", hourly);
		weatherinfo.put("daily", daily);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("weatherinfo", weatherinfo);
		return result;
	}

	private String request(String url) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Host", "www.cnpm25.cn");
		headers.put("Referer", "http://www.cnpm25.cn/");
		return httpRequest.request(url, headers);
	}

	private String parseHourly(String date) {
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_MONTH);
		// 06日15时
		int d = Integer.parseInt(date.substring(0, 2));
		if (d > day) { // 上个月
			cal.add(Calendar.MONTH, -1);
		}
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(date.substring(3, 5)));
		return new SimpleDateFormat("yyyy.MM.dd HH:00").format(cal.getTime());
	}

	private String parseDaily(String date) {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		// 01月25日
		int m = Integer.parseInt(date.substring(0, 2)) - 1;
		if (m > month) { // 去年
			cal.add(Calendar.YEAR, -1);
		}
		cal.set(Calendar.MONTH, m);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(3, 5)));
		return new SimpleDateFormat("yyyy.MM.dd").format(cal.getTime());
	}

}
