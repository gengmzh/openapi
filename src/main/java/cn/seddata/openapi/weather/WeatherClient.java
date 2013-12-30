package cn.seddata.openapi.weather;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author gengmaozhang01
 * @since 2013-12-30 下午10:14:03
 */
@Controller
@RequestMapping("/api/weather")
public class WeatherClient {

	private final Log log = LogFactory.getLog(WeatherClient.class);

	private int connectTimeout = 60 * 1000;
	private int readTimeout = 5 * 60 * 1000;
	private int retry = 3;

	@ResponseBody
	@RequestMapping(value = "/realtime", method = RequestMethod.GET)
	public ModelMap getRealtimeWeather(@RequestParam(value = "city", required = false) String citycode)
			throws Exception {
		ModelMap model = new ModelMap();
		// args
		if (citycode == null || citycode.isEmpty()) {
			model.addAttribute("code", 1);
			model.addAttribute("message", "city is required");
			return model;
		}
		// request
		String url = Config.getInstance().getRealtimeUrl(citycode);
		ObjectMapper mapper = new ObjectMapper();
		for (int i = 0; i < retry; i++) {
			try {
				String json = request(url);
				if (json != null && !json.isEmpty()) {
					model.addAttribute("code", 0);
					@SuppressWarnings("unchecked")
					Map<String, Object> value = mapper.readValue(json, Map.class);
					model.putAll(value);
					return model;
				}
			} catch (Exception e) {
				log.error("get realtime weather failed", e);
			}
		}
		// result
		model.addAttribute("code", 1);
		model.addAttribute("message", "request realtime weather failed");
		return model;
	}

	@ResponseBody
	@RequestMapping(value = "/forecast", method = RequestMethod.GET)
	public ModelMap getForecastWeather(@RequestParam(value = "city", required = false) String citycode)
			throws Exception {
		ModelMap model = new ModelMap();
		// args
		if (citycode == null || citycode.isEmpty()) {
			model.addAttribute("code", 1);
			model.addAttribute("message", "city is required");
			return model;
		}
		// request
		String url = Config.getInstance().getForecastUrl(citycode);
		ObjectMapper mapper = new ObjectMapper();
		for (int i = 0; i < retry; i++) {
			try {
				String json = request(url);
				if (json != null && !json.isEmpty()) {
					model.addAttribute("code", 0);
					@SuppressWarnings("unchecked")
					Map<String, Object> value = mapper.readValue(json, Map.class);
					model.putAll(value);
					return model;
				}
			} catch (Exception e) {
				log.error("get forecast weather failed", e);
			}
		}
		// result
		model.addAttribute("code", 1);
		model.addAttribute("message", "request forecast weather failed");
		return model;
	}

	private String request(String url) throws Exception {
		String json = null;
		HttpURLConnection conn = null;
		InputStream ins = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("Accept", "*/*");
			// conn.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
			conn.setRequestProperty("Connection", "keep-alive");
			// conn.setRequestProperty("Cookie",
			// "vjuids=-a91c6f85.13df2da4e82.0.632bf591; vjlast=1365579026.1368776420.11");
			conn.setRequestProperty("Host", "www.weather.com.cn");
			conn.setRequestProperty("Referer", "http://www.weather.com.cn/weather/101010100.shtml");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
			if (connectTimeout > 0) {
				conn.setConnectTimeout(connectTimeout);
			}
			if (readTimeout > 0) {
				conn.setReadTimeout(readTimeout);
			}
			// read
			conn.connect();
			ins = conn.getInputStream();
			ByteArrayOutputStream ous = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = ins.read(b)) > -1) {
				ous.write(b, 0, len);
			}
			json = ous.toString();
		} finally {
			if (ins != null) {
				ins.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return json;
	}

}
