/**
 * 
 */
package cn.seddat.openapi.weather.meteor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.google.common.base.Splitter;

/**
 * @author gengmaozhang01
 * @since 2014-3-15 下午2:18:44
 */
@Service
public class ForecastWeatherQuery {

	private static final Log log = LogFactory.getLog(RealtimeWeatherQuery.class);

	@Autowired
	private HttpRequest httpRequest;

	/**
	 * 抓取天气预报信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 下午8:51:50
	 */
	public Map<String, Object> query(String citycode) throws Exception {
		if (citycode == null || citycode.isEmpty()) {
			throw new IllegalArgumentException("citycode is required");
		}
		// 注意：两个接口返回的数据结构不一致
		boolean isApi = false;
		if (isApi) {
			return this.queryForecastWeatherByAPI(citycode);
		}
		Map<String, Object> result = this.queryForecastWeatherByHTML(citycode);
		log.info("forecast weather: " + result);
		return result;
	}

	/**
	 * 通过API获取天气预报信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-23 上午1:45:30
	 * @deprecated 2014.02.19号后数据更新不及时
	 */
	private Map<String, Object> queryForecastWeatherByAPI(String citycode) throws Exception {
		// request
		String url = Config.getInstance().getForecastUrl(citycode);
		String content = this.request(url);
		if (content == null || content.isEmpty()) {
			throw new Exception("query forecast weather failed, result is empty");
		}
		log.info("forecast weather: " + content);
		// parse
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> value = mapper.readValue(content, Map.class);
		return value;
	}

	/**
	 * 通过抓取网页获取天气预报信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-23 上午1:46:21
	 */
	private Map<String, Object> queryForecastWeatherByHTML(String citycode) throws Exception {
		// request
		String url = Config.getInstance().getWeatherPageUrl(citycode);
		String content = this.request(url);
		if (content == null || content.isEmpty()) {
			throw new Exception("query forecast weather failed, result is empty");
		}
		// log.info("forecast weather: " + content);
		// parse
		Map<String, Object> weatherinfo = new HashMap<String, Object>();
		weatherinfo.put("cityid", citycode);
		// weatherinfo.put("city", "");
		// weatherinfo.put("city_en", "");
		final List<String> lines = Splitter.on('\n').trimResults().omitEmptyStrings().splitToList(content);
		// 发布时间
		String time = null;
		for (String line : lines) {
			if (line.indexOf("<p class=\"updataTime\">") > -1) {
				int si = line.indexOf('>') + 1, ei = si + 16;
				time = line.substring(si, ei);
				break;
			}
		}
		if (time == null || time.isEmpty()) {
			final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
			time = format.format(new Date());
		}
		log.info("publish time: " + time);
		weatherinfo.put("time", time);
		// weatherinfo.put("date_y", time);
		// weatherinfo.put("fchh", time);
		// weatherinfo.put("date", time);
		// weatherinfo.put("week", time);
		// 预报信息
		List<Map<String, String>> forecast = new ArrayList<Map<String, String>>();
		Map<String, String> map = new HashMap<String, String>();
		final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		boolean isLi = false, isWin = false;
		for (String line : lines) {
			if (!isLi) {
				isLi = (line.indexOf("<li class='dn") > -1 && line.indexOf("data-dn='7d") > -1);
			}
			if (isLi) {
				// 日期
				int si = line.indexOf("data-dn='7d");
				if (si > -1) {
					si += 11;
					int days = Integer.parseInt(line.substring(si, si + 1)) - 1;
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DAY_OF_MONTH, days);
					map.put("time", format.format(cal.getTime()));
				}
				// 图片
				si = line.indexOf("<big class=\"");
				if (si > -1) {
					si += 12;
					int ei = line.indexOf("\">", si);
					String image = line.substring(si, ei), value = map.get("image");
					if (value != null) {
						if (!value.equals(image)) {
							map.put("image", value + "," + image);
						}
					} else {
						map.put("image", image);
					}
				}
				// 天气
				si = line.indexOf("<p class=\"wea\"");
				if (si > -1) {
					si = line.indexOf("\">") + 2;
					int ei = line.indexOf("</p>", si);
					map.put("weather", line.substring(si, ei));
				}
				// 温度
				si = line.indexOf("<i>°C</i>");
				if (si > -1) {
					si = line.indexOf("<span>") + 6;
					int ei = line.indexOf("</span>", si);
					String temp = line.substring(si, ei) + "℃", value = map.get("temp");
					if (value != null) {
						if (!value.equals(temp)) {
							map.put("temp", value.substring(0, value.length() - 1) + "~" + temp);
						}
					} else {
						map.put("temp", temp);
					}
				}
				// 风向、风力
				if (!isWin) {
					isWin = line.indexOf("<p class=\"win\">") > -1;
				}
				if (isWin) {
					if (line.indexOf("<span") > -1) {
						si = line.indexOf("title=\"") + 7;
						int ei = line.indexOf("\" class=", si);
						String wd = line.substring(si, ei), value = map.get("wd");
						if (value != null) {
							if (!value.equals(wd)) {
								map.put("wd", value + "转" + wd);
							}
						} else {
							map.put("wd", wd);
						}
					} else if (line.indexOf("<i>") > -1) {
						si = line.indexOf("<i>") + 3;
						int ei = line.indexOf("</i>", si);
						map.put("ws", line.substring(si, ei));
					}
					if (line.indexOf("</p>") > -1) {
						isWin = false;
					}
				}
				// 结束
				if (line.indexOf("</li>") > -1) {
					forecast.add(map);
					map = new HashMap<String, String>();
					isLi = false;
					isWin = false;
				}
			}
		}
		weatherinfo.put("forecast", forecast);
		// result
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("weatherinfo", weatherinfo);
		return result;
	}

	private String request(String url) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Host", "www.weather.com.cn");
		headers.put("Referer", "http://www.weather.com.cn/weather/101010100.shtml");
		headers.put("Accept-Charset", "UTF-8");
		return httpRequest.request(url, headers);
	}

}
