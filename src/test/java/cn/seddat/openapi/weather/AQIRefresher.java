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
 * AQI信息刷新器
 * 
 * @author gengmaozhang01
 * @since 2014-2-22 下午2:42:39
 */
// @Service
public class AQIRefresher implements Runnable, ApplicationContextAware {

	private static final Log log = LogFactory.getLog(AQIRefresher.class);

	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	private String dbCollNameDaily = "weather.aqidaily", dbCollNameHourly = "weather.aqihourly";

	@Override
	public void run() {
		log.info("AQI refresher starts...");
		List<Config.City> cities = Config.getInstance().getAllAQICities();
		log.info("total " + cities.size() + " AQI cities");
		final ScheduledExecutorService scheduledExecutorService = context.getBean("weatherScheduler",
				ScheduledThreadPoolExecutor.class);
		for (Config.City city : cities) {
			Runner task = new Runner(this, city);
			scheduledExecutorService.submit(task);
		}
		log.info("all AQI refresher startup");
	}

	/**
	 * 更新指定城市的AQI信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-22 下午4:25:12
	 */
	@SuppressWarnings("unchecked")
	public void refresh(Config.City city) throws Exception {
		// query
		AQIClient aqiClient = context.getBean(AQIClient.class);
		Map<String, Object> aqi = aqiClient.queryAirQualityIndex(city.getCode());
		aqi = (Map<String, Object>) aqi.get("weatherinfo");
		String aqiCode = Config.getInstance().getAQICode(city.getCode());
		String utime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		// parse
		MongoService mongoService = context.getBean(MongoService.class);
		// daily
		DBCollection dbColl = mongoService.getDBCollection(dbCollNameDaily);
		dbColl.ensureIndex(BasicDBObjectBuilder.start("city", 1).add("time", 1).get());
		dbColl.ensureIndex(BasicDBObjectBuilder.start("ts", 1).get());
		List<Map<String, String>> daily = (List<Map<String, String>>) aqi.get("daily");
		for (Map<String, String> data : daily) {
			DBObject key = BasicDBObjectBuilder.start("city", city.getCode()).add("time", data.get("time")).get();
			DBObject value = BasicDBObjectBuilder.start("AQI", Integer.valueOf(data.get("AQI")))
					.add("AQICode", aqiCode).add("ts", this.parseTimestamp(data.get("time"))).add("ut", utime).get();
			value.putAll(key);
			dbColl.update(key, value, true, false);
		}
		// hourly
		dbColl = mongoService.getDBCollection(dbCollNameHourly);
		dbColl.ensureIndex(BasicDBObjectBuilder.start("city", 1).add("time", 1).get());
		dbColl.ensureIndex(BasicDBObjectBuilder.start("ts", 1).get());
		List<Map<String, String>> hourly = (List<Map<String, String>>) aqi.get("hourly");
		for (Map<String, String> data : hourly) {
			DBObject key = BasicDBObjectBuilder.start("city", city.getCode()).add("time", data.get("time")).get();
			DBObject value = BasicDBObjectBuilder.start("AQI", Integer.valueOf(data.get("AQI")))
					.add("AQICode", aqiCode).add("ts", this.parseTimestamp(data.get("time"))).add("ut", utime).get();
			value.putAll(key);
			dbColl.update(key, value, true, false);
		}
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

	class Runner implements Runnable {

		private AQIRefresher aqiRefresher;
		private Config.City city;

		public Runner(AQIRefresher aqiRefresher, Config.City city) {
			this.aqiRefresher = aqiRefresher;
			this.city = city;
		}

		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					aqiRefresher.refresh(city);
					break;
				} catch (Exception e) {
					log.error("refresh AQI failed", e);
				}
			}
		}

	}

}
