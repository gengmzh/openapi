/**
 * 
 */
package cn.seddat.openapi.weather;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author gmz
 * @time 2012-5-12
 */
public class Config {

	private static final Log log = LogFactory.getLog(Config.class);

	private static final Config instance = new Config();

	public static Config getInstance() {
		return instance;
	}

	private List<City> provinces = new ArrayList<City>();

	private Config() {
		try {
			this.initCities();
		} catch (Exception e) {
			log.error("init cities failed", e);
		}
	}

	private void initCities() throws Exception {
		BufferedReader reader = null;
		try {
			InputStream ins = Config.class.getClassLoader().getResourceAsStream(
					"cn/seddat/openapi/weather/city.properties");
			reader = new BufferedReader(new InputStreamReader(ins));
			City c1 = null, c2 = null;
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] ls = line.split("\t");
				if (ls.length < 2) {
					continue;
				}
				City city = new City(ls[0], ls[1], (ls.length > 2 ? ls[2] : null));
				// 省份
				if (city.getCode().length() == 5) {
					provinces.add(city);
					c1 = city;
				}
				// 地市
				else if (city.getCode().length() == 7) {
					c1.addChild(city);
					c2 = city;
				}
				// 区县
				else {
					c2.addChild(city);
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * 城市信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 下午3:37:27
	 */
	public class City {
		private String code, name, aqiCode;
		private List<City> children;

		public City(String code, String name, String aqiCode) {
			super();
			this.code = code;
			this.name = name;
			this.aqiCode = aqiCode;
			this.children = new ArrayList<City>();
		}

		/**
		 * 获取城市代码
		 * 
		 * @author gengmaozhang01
		 * @since 2014-2-22 下午3:37:47
		 */
		public String getCode() {
			return code;
		}

		/**
		 * 获取城市名称
		 * 
		 * @author gengmaozhang01
		 * @since 2014-2-22 下午3:38:03
		 */
		public String getName() {
			return name;
		}

		/**
		 * 获取AQI代码
		 * 
		 * @author gengmaozhang01
		 * @since 2014-2-22 下午3:38:17
		 */
		public String getAqiCode() {
			return aqiCode;
		}

		public List<City> getChildren() {
			return children;
		}

		public City addChild(City child) {
			children.add(child);
			return this;
		}

	}

	/**
	 * 获得所有区县
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 下午3:21:43
	 */
	public List<City> getAllDistricts() {
		List<City> districts = new ArrayList<City>();
		for (City prov : provinces) {
			for (City city : prov.getChildren()) {
				for (City distrct : city.getChildren()) {
					districts.add(distrct);
				}
			}
		}
		return districts;
	}

	/**
	 * 获得所有监控AQI信息的城市
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 下午3:23:26
	 */
	public List<City> getAllAQICities() {
		List<City> cities = new ArrayList<City>();
		for (City prov : provinces) {
			if (prov.getAqiCode() != null && !prov.getAqiCode().isEmpty()) {
				cities.add(prov);
			}
			for (City city : prov.getChildren()) {
				if (city.getAqiCode() != null && !city.getAqiCode().isEmpty()) {
					cities.add(city);
				}
				for (City distrct : city.getChildren()) {
					if (distrct.getAqiCode() != null && !distrct.getAqiCode().isEmpty()) {
						cities.add(distrct);
					}
				}
			}
		}
		return cities;
	}

	/**
	 * 获得城市代码对应的AQI代码
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 下午3:26:25
	 */
	public String getAQICode(String citycode) {
		if (citycode == null || citycode.isEmpty()) {
			return null;
		}
		for (City prov : provinces) {
			if (citycode.equals(prov.getCode())) {
				return prov.getAqiCode();
			}
			for (City city : prov.getChildren()) {
				if (citycode.equals(city.getCode())) {
					if (city.getAqiCode() != null && !city.getAqiCode().isEmpty()) {
						return city.getAqiCode();
					} else {
						return prov.getAqiCode();
					}
				}
				for (City distrct : city.getChildren()) {
					if (citycode.equals(distrct.getCode())) {
						if (distrct.getAqiCode() != null && !distrct.getAqiCode().isEmpty()) {
							return distrct.getAqiCode();
						} else if (city.getAqiCode() != null && !city.getAqiCode().isEmpty()) {
							return city.getAqiCode();
						} else {
							return city.getAqiCode();
						}
					}
				}
			}
		}
		return null;
	}

	// private String CITY1_URL = "http://www.weather.com.cn/data/city3jdata/china.html";
	// private String CITY2_URL_PREFIX = "http://www.weather.com.cn/data/city3jdata/provshi/";
	// private String CITY3_URL_PREFIX = "http://www.weather.com.cn/data/city3jdata/station/";
	private String URL_SUFFIX = ".html";

	// public String getCity1Url() {
	// return CITY1_URL;
	// }
	//
	// public String getCity2Url(String city1) {
	// return CITY2_URL_PREFIX + city1 + URL_SUFFIX;
	// }
	//
	// public String getCity3Url(String city2) {
	// return CITY3_URL_PREFIX + city2 + URL_SUFFIX;
	// }

	private String URL_PREFIX_REALTIME = "http://www.weather.com.cn/data/sk/";

	/**
	 * 获取天气实况接口URL，citycode为城市代码
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 上午9:42:11
	 */
	public String getRealtimeUrl(String citycode) {
		return URL_PREFIX_REALTIME + citycode + URL_SUFFIX;
	}

	private String URL_PREFIX_FORECAST = "http://m.weather.com.cn/data/";

	/**
	 * 获取天气预报接口URL，citycode为城市代码
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 上午9:42:54
	 * @deprecated 2014.02.19号后数据更新不及时了
	 */
	public String getForecastUrl(String citycode) {
		return URL_PREFIX_FORECAST + citycode + URL_SUFFIX;
	}

	private String URL_PREFIX_WEATHER_PAGE = "http://www.weather.com.cn/weather/";

	/**
	 * 获取天气预报页面URL，citycode为城市代码
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 下午8:27:51
	 */
	public String getWeatherPageUrl(String citycode) {
		return URL_PREFIX_WEATHER_PAGE + citycode + ".shtml";
	}

	private String URL_PREFIX_PM25 = "http://www.cnpm25.cn/city/";

	/**
	 * 获取AQI页面URL，citycode为城市代码
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-6 下午10:38:22
	 */
	public String getCNPM25AQIUrl(String citycode) {
		String aqiCode = this.getAQICode(citycode);
		if (aqiCode == null || aqiCode.isEmpty()) {
			throw new IllegalArgumentException("citycode " + citycode + " is illegal, can't find AQI code");
		}
		return URL_PREFIX_PM25 + aqiCode + URL_SUFFIX;
	}

	/**
	 * 获取AQI接口URL，citycode为城市代码
	 * 
	 * @author gengmaozhang01
	 * @since 2014-3-15 上午11:32:35
	 */
	public String getCNPM25AQIAPI(String citycode) {
		String aqiCode = this.getAQICode(citycode);
		if (aqiCode == null || aqiCode.isEmpty()) {
			throw new IllegalArgumentException("citycode " + citycode + " is illegal, can't find AQI code");
		}
		return "http://appapi.cnpm25.cn/TopInfoWeb.aspx?u=" + aqiCode;
	}

	/**
	 * 获取pm25.in站点的AQI接口URL，citycode为城市代码
	 * 
	 * @author gengmaozhang01
	 * @since 2014-3-16 下午6:05:08
	 */
	public String getPM25INAQIAPI(String citycode) {
		String aqiCode = this.getAQICode(citycode);
		if (aqiCode == null || aqiCode.isEmpty()) {
			throw new IllegalArgumentException("citycode " + citycode + " is illegal, can't find AQI code");
		}
		return "http://www.pm25.in/api/querys/aqi_details.json?token=AQCbX9VKuX597AbjAtfG&stations=no&city=" + aqiCode;
	}

}
