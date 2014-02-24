/**
 * 
 */
package cn.seddat.openapi.weather;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * 天气信息刷新器
 * 
 * @author gengmaozhang01
 * @since 2014-2-22 下午2:42:39
 */
// @Service
public class WeatherRefresher implements Runnable, ApplicationContextAware {

	private static final Log log = LogFactory.getLog(WeatherRefresher.class);

	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	private String dbCollNameRealtime = "weather.realtime", dbCollNameForecast = "weather.forecast";

	@Override
	public void run() {
		log.info("weather refresher starts...");
		List<Config.City> cities = Config.getInstance().getAllDistricts();
		log.info("total " + cities.size() + " weather cities");
		final ScheduledExecutorService scheduledExecutorService = context.getBean("weatherScheduler",
				ScheduledThreadPoolExecutor.class);
		for (Config.City city : cities) {
			Refresher task = new Refresher(this, city);
			scheduledExecutorService.submit(task);
		}
		log.info("all weather refresher startup");
	}

	/**
	 * 更新指定城市的天气实况
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 下午4:25:12
	 */
	@SuppressWarnings("unchecked")
	public void refreshRealtime(Config.City city) throws Exception {
		String utime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		// query
		WeatherClient weatherClient = context.getBean(WeatherClient.class);
		Map<String, Object> realtime = weatherClient.queryRealtimeWeather(city.getCode());
		realtime = (Map<String, Object>) realtime.get("weatherinfo");
		// save
		MongoService mongoService = context.getBean(MongoService.class);
		DBCollection dbColl = mongoService.getDBCollection(dbCollNameRealtime);
		dbColl.ensureIndex(BasicDBObjectBuilder.start("city", 1).add("time", 1).get());
		dbColl.ensureIndex(BasicDBObjectBuilder.start("ts", 1).get());
		final String time = (String) realtime.get("time");
		DBObject key = BasicDBObjectBuilder.start("city", city.getCode()).add("time", time).get();
		DBObject value = BasicDBObjectBuilder.start("temp", realtime.get("temp")).add("WD", realtime.get("WD"))
				.add("WS", realtime.get("WS")).add("WSE", realtime.get("WSE")).add("SD", realtime.get("SD"))
				.add("isRadar", realtime.get("isRadar")).add("Radar", realtime.get("Radar"))
				.add("indexes", realtime.get("indexes")).add("ts", this.parseTimestamp(time)).add("ut", utime).get();
		value.putAll(key);
		dbColl.update(key, value, true, false);
	}

	/**
	 * 刷新指定城市的天气预报
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-23 下午8:23:00
	 */
	@SuppressWarnings("unchecked")
	public void refreshForecast(Config.City city) throws Exception {
		String utime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		// query
		WeatherClient weatherClient = context.getBean(WeatherClient.class);
		Map<String, Object> forecast = weatherClient.queryForecastWeather(city.getCode());
		forecast = (Map<String, Object>) forecast.get("weatherinfo");
		// save
		MongoService mongoService = context.getBean(MongoService.class);
		DBCollection dbColl = mongoService.getDBCollection(dbCollNameForecast);
		dbColl.ensureIndex(BasicDBObjectBuilder.start("city", 1).add("time", 1).get());
		dbColl.ensureIndex(BasicDBObjectBuilder.start("ts", 1).get());
		final String time = (String) forecast.get("time");
		DBObject key = BasicDBObjectBuilder.start("city", city.getCode()).add("time", time).get();
		DBObject value = BasicDBObjectBuilder.start("daily", forecast.get("daily"))
				.add("ts", this.parseTimestamp(time)).add("ut", utime).get();
		value.putAll(key);
		dbColl.update(key, value, true, false);
	}

	private long parseTimestamp(String date) { // 2014.02.21, 2014.02.21 20:00
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7)));
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(8, 10)));
		if (date.length() > 12) {
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(date.substring(11, 13)));
		} else {
			cal.set(Calendar.HOUR_OF_DAY, 0);
		}
		if (date.length() > 15) {
			cal.set(Calendar.MINUTE, Integer.parseInt(date.substring(14, 16)));
		} else {
			cal.set(Calendar.MINUTE, 0);
		}
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis() / 1000;
	}

	class Refresher implements Runnable {

		private WeatherRefresher weatherRefresher;
		private Config.City city;

		public Refresher(WeatherRefresher weatherRefresher, Config.City city) {
			this.weatherRefresher = weatherRefresher;
			this.city = city;
		}

		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					weatherRefresher.refreshRealtime(city);
					break;
				} catch (Exception e) {
					log.error("refresh realtime weather failed", e);
				}
			}
			for (int i = 0; i < 10; i++) {
				try {
					weatherRefresher.refreshForecast(city);
					break;
				} catch (Exception e) {
					log.error("refresh forecast weather failed", e);
				}
			}
		}

	}

}
