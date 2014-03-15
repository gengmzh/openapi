/**
 * 
 */
package cn.seddat.openapi.weather;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.seddat.openapi.weather.aqi.CNPM25APIAQIQuery;
import cn.seddat.openapi.weather.aqi.CNPM25WebAQIQuery;

/**
 * 空气质量指数服务
 * 
 * @author gengmaozhang01
 * @since 2014-2-6 下午10:05:06
 */
@Service
public class AQIClient {

	private static final Log log = LogFactory.getLog(AQIClient.class);

	@Autowired
	private CNPM25APIAQIQuery cnpm25APIAQIQuery;
	@Autowired
	private CNPM25WebAQIQuery cnpm25WebAQIQuery;
	@Autowired
	private EasyCache easyCache;

	private long cacheSeconds = 30 * 60;

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
		// cache
		String key = "aqi-" + citycode;
		Map<String, Object> result = easyCache.get(key);
		if (result != null) {
			log.info("cache AQI: " + result);
			return result;
		}
		// query
		try {
			result = cnpm25APIAQIQuery.query(citycode);
		} catch (Exception ex) {
			log.error("query AQI by api failed", ex);
		}
		if (result == null || result.isEmpty()) {
			try {
				result = cnpm25WebAQIQuery.query(citycode);
			} catch (Exception ex) {
				log.error("query AQI by web failed", ex);
			}
		}
		if (result == null || result.isEmpty()) {
			throw new Exception("query AQI failed, result is empty");
		} else {
			easyCache.set(key, result, cacheSeconds);
		}
		return result;
	}

}
