/**
 * 
 */
package cn.seddat.openapi.weather;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
 * @author gengmaozhang01
 * @since 2014-2-6 下午10:05:06
 */
@Service
public class AQIClientService {

	@Autowired
	private HttpRequest httpRequest;

	private Map<String, String> AQICities = new HashMap<String, String>();

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
			throw new IllegalArgumentException("citycode " + citycode + " is illegal, can't find PM2.5 city name");
		}
		// request
		String url = Config.getInstance().getPM25Url(aqiCity);
		String content = this.request(url);
		if (content == null || content.isEmpty()) {
			throw new Exception("query PM2.5 failed, result is empty");
		}
		// parse
		Map<String, Object> weatherinfo = new HashMap<String, Object>();
		weatherinfo.put("cityid", citycode);
		weatherinfo.put("city", "");
		weatherinfo.put("AQI_city", aqiCity);
		final List<String> lines = Splitter.on('\n').trimResults().omitEmptyStrings().splitToList(content);
		// 当前检测时间
		Date curTime = null;
		SimpleDateFormat hourlyFormat = new SimpleDateFormat("M月d日H点"), dailyFormat = new SimpleDateFormat("M月d日 ");
		for (String line : lines) {
			if (line.contains("更新时间：")) {
				int idx = line.indexOf("更新时间：") + 5;
				final String time = line.substring(idx, idx + 16);
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				curTime = format.parse(time);
				weatherinfo.put("time", hourlyFormat.format(curTime));
				break;
			}
		}
		// 最近24小时AQI、最近14天AQI
		List<Map<String, String>> hourly = Lists.newArrayList(), daily = Lists.newArrayList();
		int count = 0;
		for (String line : lines) {
			if (line.startsWith("flashvalue")) {
				count++;
				Calendar time = Calendar.getInstance();
				time.setTime(curTime);
				if (count <= 24) { // 最近24小时AQI
					time.add(Calendar.HOUR_OF_DAY, count - 24);
					int si = line.indexOf("value='") + 7, ei = line.indexOf("'", si);
					String value = line.substring(si, ei);
					hourly.add(ImmutableMap.of("time", hourlyFormat.format(time.getTime()), "AQI", value));
				} else { // 最近14天AQI
					time.add(Calendar.DAY_OF_MONTH, count - 24 - 14 - 1);
					int si = line.indexOf("value='") + 7, ei = line.indexOf("'", si);
					String value = line.substring(si, ei);
					daily.add(ImmutableMap.of("time", dailyFormat.format(time.getTime()), "AQI", value));
				}
				if (count == 24) { // 当前AQI
					int si = line.indexOf("value='") + 7, ei = line.indexOf("'", si);
					weatherinfo.put("AQI", line.substring(si, ei));
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

}
