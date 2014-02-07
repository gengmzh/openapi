/**
 * 
 */
package cn.seddat.openapi.weather;

/**
 * @author gmz
 * @time 2012-5-12
 */
public class Config {

	public static final Config instance = new Config();

	public static Config getInstance() {
		return instance;
	}

	private Config() {
	}

	private String CITY1_URL = "http://www.weather.com.cn/data/city3jdata/china.html";
	private String CITY2_URL_PREFIX = "http://www.weather.com.cn/data/city3jdata/provshi/";
	private String CITY3_URL_PREFIX = "http://www.weather.com.cn/data/city3jdata/station/";
	private String URL_SUFFIX = ".html";

	public String getCity1Url() {
		return CITY1_URL;
	}

	public String getCity2Url(String city1) {
		return CITY2_URL_PREFIX + city1 + URL_SUFFIX;
	}

	public String getCity3Url(String city2) {
		return CITY3_URL_PREFIX + city2 + URL_SUFFIX;
	}

	private String URL_PREFIX_REALTIME = "http://www.weather.com.cn/data/sk/";

	public String getRealtimeUrl(String citycode) {
		return URL_PREFIX_REALTIME + citycode + URL_SUFFIX;
	}

	private String URL_PREFIX_FORECAST = "http://m.weather.com.cn/data/";

	public String getForecastUrl(String citycode) {
		return URL_PREFIX_FORECAST + citycode + URL_SUFFIX;
	}

	private String URL_PREFIX_PM25 = "http://www.cnpm25.cn/city/";

	/**
	 * 获取PM2.5信息页面URL，citycode为城市名称拼音
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-6 下午10:38:22
	 */
	public String getPM25Url(String citycode) {
		return URL_PREFIX_PM25 + citycode + URL_SUFFIX;
	}

}
