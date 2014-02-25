/**
 * 
 */
package cn.seddat.openapi.weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

/**
 * 天气信息获取服务
 * 
 * @author gengmaozhang01
 * @since 2014-1-1 上午8:18:07
 */
@Service
public class WeatherClient {

	private final Log log = LogFactory.getLog(WeatherClient.class);

	@Autowired
	private HttpRequest httpRequest;

	/**
	 * 抓取天气实况信息，包含生活指数
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-23 下午8:12:53
	 */
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
		log.info("realtime weather: " + content);
		// parse
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> result = mapper.readValue(content, Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> weatherinfo = (Map<String, Object>) result.get("weatherinfo");
		String time = (String) weatherinfo.get("time");
		Calendar cal = Calendar.getInstance();
		time = new SimpleDateFormat("yyyy.MM.dd").format(cal.getTime()) + " " + time;
		weatherinfo.put("time", time);
		// 生活指数
		url = Config.getInstance().getWeatherPageUrl(citycode);
		content = this.request(url);
		if (content != null && !content.isEmpty()) {
			List<Map<String, String>> indexes = new ArrayList<Map<String, String>>();
			final List<String> lines = Splitter.on('\n').trimResults().omitEmptyStrings().splitToList(content);
			StringBuffer index = new StringBuffer();
			boolean isSection = false;
			for (String line : lines) {
				if (!isSection) {
					isSection = line.indexOf("<section") > -1 && line.indexOf("class=\"detail") > -1;
				}
				if (isSection) {
					index.append(line);
					if (line.indexOf("</aside>") > -1) {
						String type = null;
						if (index.indexOf("class=\"detail ct\"") > -1) {
							type = "ct"; // 穿衣指数
						} else if (index.indexOf("class=\"detail tr\"") > -1) {
							type = "tr"; // 旅游指数
						} else if (index.indexOf("class=\"detail yd\"") > -1) {
							type = "yd"; // 运动指数
						} else if (index.indexOf("class=\"detail xc\"") > -1) {
							type = "xc"; // 洗车指数
						} else if (index.indexOf("class=\"detail pp\"") > -1) {
							type = "pp"; // 化妆指数
						} else if (index.indexOf("class=\"detail gm\"") > -1) {
							type = "gm"; // 感冒指数
						} else if (index.indexOf("class=\"detail uv\"") > -1) {
							type = "uv"; // 紫外线指数
						} else if (index.indexOf("class=\"detail co\"") > -1) {
							type = "co"; // 舒适度指数
						} else if (index.indexOf("class=\"detail ag\"") > -1) {
							type = "ag"; // 过敏指数
						} else if (index.indexOf("class=\"detail gj\"") > -1) {
							type = "gj"; // 逛街指数
						} else if (index.indexOf("class=\"detail mf\"") > -1) {
							type = "mf"; // 美发指数
						} else if (index.indexOf("class=\"detail ys\"") > -1) {
							type = "ys"; // 雨伞指数
						} else if (index.indexOf("class=\"detail jt\"") > -1) {
							type = "jt"; // 交通指数
						} else if (index.indexOf("class=\"detail lk\"") > -1) {
							type = "lk"; // 路况指数
						} else if (index.indexOf("class=\"detail cl\"") > -1) {
							type = "cl"; // 晨练指数
						} else if (index.indexOf("class=\"detail dy\"") > -1) {
							type = "dy"; // 钓鱼指数
						} else if (index.indexOf("class=\"detail hc\"") > -1) {
							type = "hc"; // 划船指数
						} else if (index.indexOf("class=\"detail yh\"") > -1) {
							type = "yh"; // 约会指数
						} else if (index.indexOf("class=\"detail ls\"") > -1) {
							type = "ls"; // 晾晒指数
						} else if (index.indexOf("class=\"detail fs\"") > -1) {
							type = "fs"; // 防晒指数
						}
						int si = index.indexOf("<b>") + 3, ei = index.indexOf("</b>", si);
						String idx = index.substring(si, ei);
						si = ei + 4;
						ei = index.indexOf("</aside>", si);
						String dtl = index.substring(si, ei);
						if (type != null) {
							indexes.add(ImmutableMap.of("name", type, "value", idx, "desc", dtl));
						}
						isSection = false;
						index = new StringBuffer();
					}
				}
			}
			weatherinfo.put("indexes", indexes);
		}
		log.info("realtime weather: " + result);
		return result;
	}

	/**
	 * 抓取天气预报信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 下午8:51:50
	 */
	public Map<String, Object> queryForecastWeather(String citycode) throws Exception {
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
		StringBuffer title = new StringBuffer();
		boolean isDiv = false, isH1 = false;
		for (String line : lines) {
			if (!isDiv) {
				isDiv = line.indexOf("<div") > -1 && line.indexOf("class=\"weatherYubao\"") > -1;
			}
			if (isDiv) {
				if (!isH1) {
					isH1 = line.indexOf("<h1") > -1 && line.indexOf("class=\"weatheH1\"") > -1;
				}
				if (isH1) {
					title.append(line);
				}
				if (line.indexOf("</h1>") > -1) {
					break;
				}
			}
		}
		log.info("title: " + title);
		int si = title.indexOf("("), ei = title.indexOf(")", si);
		String time = title.substring(si + 1, ei - 2).replace("&nbsp;", " ").replace('-', '.');
		log.info("time: " + time);
		weatherinfo.put("time", time);
		// weatherinfo.put("date_y", time);
		// weatherinfo.put("fchh", time);
		// weatherinfo.put("date", time);
		// weatherinfo.put("week", time);
		// 预报信息
		List<String> tables = new ArrayList<String>();
		StringBuffer buf = new StringBuffer();
		boolean isTable = false;
		for (String line : lines) {
			if (!isTable) {
				isTable = (line.indexOf("<table") > -1 && line.indexOf("class=\"yuBaoTable\"") > -1);
			}
			if (isTable) {
				buf.append(line);
				if (line.indexOf("</table>") > -1) {
					tables.add(buf.toString());
					buf = new StringBuffer();
					isTable = false;
				}
			}
		}
		List<Map<String, String>> forecast = new ArrayList<Map<String, String>>();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		String prevTime = null, prevHalf = null;
		for (String table : tables) {
			List<String> trs = Splitter.on("</tr>").omitEmptyStrings().splitToList(table);
			for (String tr : trs) {
				si = tr.indexOf("<tr");
				if (si == -1) {
					continue;
				}
				tr = tr.substring(si + 3);
				List<String> tds = Splitter.on("</td>").omitEmptyStrings().splitToList(tr);
				// 日期
				if (tds.size() == 7) {
					time = tds.get(0);
					ei = time.indexOf("日星期");
					si = time.lastIndexOf(">", ei) + 1;
					time = time.substring(si, ei);
					Calendar cal = Calendar.getInstance();
					int day = Integer.parseInt(time);
					if (cal.get(Calendar.DAY_OF_MONTH) > day + 7) {
						cal.add(Calendar.MONTH, 1);
					}
					cal.set(Calendar.DAY_OF_MONTH, day);
					time = dateFormat.format(cal.getTime());
				} else {
					time = prevTime;
				}
				// 白天或夜晚
				String half = tds.get(tds.size() == 7 ? 1 : 0);
				half = half.substring(half.indexOf("<td") + 3);
				si = half.indexOf('>') + 1;
				ei = half.indexOf('<', si);
				half = (ei > -1 ? half.substring(si, ei) : half.substring(si));
				// 图片
				String image = tds.get(tds.size() == 7 ? 2 : 1);
				ei = image.indexOf("gif\"") + 3;
				si = image.lastIndexOf("/", ei) + 1;
				image = image.substring(si, ei);
				// 天气
				String weather = tds.get(tds.size() == 7 ? 3 : 2);
				si = weather.indexOf("\"_blank\">") + 9;
				ei = weather.indexOf("<", si);
				weather = weather.substring(si, ei);
				// 温度
				String temp = tds.get(tds.size() == 7 ? 4 : 3);
				si = temp.indexOf("<strong>") + 8;
				ei = temp.indexOf("<", si);
				temp = temp.substring(si, ei);
				// 风向
				String wd = tds.get(tds.size() == 7 ? 5 : 4);
				si = wd.indexOf("\"_blank\">") + 9;
				ei = wd.indexOf("<", si);
				wd = wd.substring(si, ei);
				// 风力
				String ws = tds.get(tds.size() == 7 ? 6 : 5);
				si = ws.indexOf("\"_blank\">") + 9;
				ei = ws.indexOf("<", si);
				ws = ws.substring(si, ei);
				// add
				if (!time.equals(prevTime) || !half.equals(prevHalf)) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("time", time);
					map.put("half", half);
					map.put("image", image);
					map.put("weather", weather);
					map.put("temp", temp);
					map.put("wd", wd);
					map.put("ws", ws);
					forecast.add(map);
				}
				prevTime = time;
				prevHalf = half;
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
