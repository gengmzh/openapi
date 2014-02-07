/**
 * 
 */
package cn.seddat.openapi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.seddat.openapi.weather.WeatherClientService;

/**
 * @author gengmaozhang01
 * @since 2014-2-6 下午9:27:06
 */
@Service
public class HttpRequest {

	private final Log log = LogFactory.getLog(WeatherClientService.class);

	private int connectTimeout = 60 * 1000;
	private int readTimeout = 5 * 60 * 1000;
	private int retry = 3;

	/**
	 * 发起HTTP请求
	 */
	public String request(String url, Map<String, String> headers) throws Exception {
		if (url == null || url.isEmpty()) {
			throw new IllegalArgumentException("url is required");
		}
		String content = null;
		Exception exception = null;
		for (int i = 0; i < retry; i++) {
			if (exception != null) {
				log.error("request failed round " + (i + 1), exception);
			}
			try {
				content = this.request0(url, headers);
				if (content != null && !content.isEmpty()) {
					break;
				}
			} catch (Exception ex) {
				exception = ex;
			}
		}
		if (exception != null) {
			throw exception;
		}
		return content;
	}

	private String request0(String url, Map<String, String> headers) throws Exception {
		String content = null;
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
			if (headers != null && !headers.isEmpty()) {
				for (String key : headers.keySet()) {
					conn.setRequestProperty(key, headers.get(key));
				}
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
			content = ous.toString();
		} finally {
			if (ins != null) {
				ins.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return content;
	}

}
