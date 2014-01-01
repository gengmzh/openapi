/**
 * 
 */
package cn.seddata.openapi.weather;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * 天气信息获取服务
 * 
 * @author gengmaozhang01
 * @since 2014-1-1 上午8:18:07
 */
@Service
public class WeatherClientService {

	private final Log log = LogFactory.getLog(WeatherClientService.class);

	private int connectTimeout = 60 * 1000;
	private int readTimeout = 5 * 60 * 1000;
	private int retry = 3;

	public Map<String, Object> queryRealtimeWeather(String citycode) throws Exception {
		if (citycode == null || citycode.isEmpty()) {
			throw new IllegalArgumentException("citycode is required");
		}
		// request
		String url = Config.getInstance().getRealtimeUrl(citycode);
		ObjectMapper mapper = new ObjectMapper();
		Exception exception = null;
		for (int i = 0; i < retry; i++) {
			try {
				String json = request(url);
				if (json != null && !json.isEmpty()) {
					@SuppressWarnings("unchecked")
					Map<String, Object> value = mapper.readValue(json, Map.class);
					return value;
				}
			} catch (Exception e) {
				exception = e;
				log.error("round " + (i + 1) + ", get realtime weather failed", e);
			}
		}
		// result
		if (exception != null) {
			throw exception;
		}
		return null;
	}

	public Map<String, Object> queryForecastWeather(String citycode) throws Exception {
		if (citycode == null || citycode.isEmpty()) {
			throw new IllegalArgumentException("citycode is required");
		}
		// request
		String url = Config.getInstance().getForecastUrl(citycode);
		ObjectMapper mapper = new ObjectMapper();
		Exception exception = null;
		for (int i = 0; i < retry; i++) {
			try {
				String json = request(url);
				if (json != null && !json.isEmpty()) {
					@SuppressWarnings("unchecked")
					Map<String, Object> value = mapper.readValue(json, Map.class);
					return value;
				}
			} catch (Exception e) {
				exception = e;
				log.error("round " + (i + 1) + ", get forecast weather failed", e);
			}
		}
		// result
		if (exception != null) {
			throw exception;
		}
		return null;
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
