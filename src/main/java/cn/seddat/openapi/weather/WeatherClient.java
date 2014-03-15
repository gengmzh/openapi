/**
 * 
 */
package cn.seddat.openapi.weather;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.seddat.openapi.weather.meteor.ForecastWeatherQuery;
import cn.seddat.openapi.weather.meteor.RealtimeWeatherQuery;

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
	private RealtimeWeatherQuery realtimeWeatherQuery;
	@Autowired
	private ForecastWeatherQuery forecastWeatherQuery;
	@Autowired
	private EasyCache easyCache;

	private long cacheSeconds = 10 * 60;

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
		// cache
		String key = "rtw-" + citycode;
		Map<String, Object> result = easyCache.get(key);
		if (result != null) {
			log.info("cache realtime weather: " + result);
			return result;
		}
		// query
		try {
			result = realtimeWeatherQuery.query(citycode);
		} catch (Exception ex) {
			log.error("query realtime weather failed", ex);
		}
		if (result == null || result.isEmpty()) {
			throw new Exception("realtime weather is empty");
		} else {
			easyCache.set(key, result, cacheSeconds);
		}
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
		// cache
		String key = "fcw-" + citycode;
		Map<String, Object> result = easyCache.get(key);
		if (result != null) {
			log.info("cache forecast weather: " + result);
			return result;
		}
		// query
		try {
			result = realtimeWeatherQuery.query(citycode);
		} catch (Exception ex) {
			log.error("query forecast weather failed", ex);
		}
		if (result == null || result.isEmpty()) {
			throw new Exception("forecast weather is empty");
		} else {
			easyCache.set(key, result, cacheSeconds);
		}
		return result;
	}

}
