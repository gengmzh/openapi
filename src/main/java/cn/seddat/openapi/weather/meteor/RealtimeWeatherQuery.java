/**
 * 
 */
package cn.seddat.openapi.weather.meteor;

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
import cn.seddat.openapi.weather.Config;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

/**
 * @author gengmaozhang01
 * @since 2014-3-15 下午2:17:28
 */
@Service
public class RealtimeWeatherQuery {

	private static final Log log = LogFactory.getLog(RealtimeWeatherQuery.class);

	@Autowired
	private HttpRequest httpRequest;

	/**
	 * 抓取天气实况信息，包含生活指数
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-23 下午8:12:53
	 */
	public Map<String, Object> query(String citycode) throws Exception {
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

	private String request(String url) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Host", "www.weather.com.cn");
		headers.put("Referer", "http://www.weather.com.cn/weather/101010100.shtml");
		headers.put("Accept-Charset", "UTF-8");
		return httpRequest.request(url, headers);
	}

}
