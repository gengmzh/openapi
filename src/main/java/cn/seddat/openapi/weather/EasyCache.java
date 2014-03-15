/**
 * 
 */
package cn.seddat.openapi.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

/**
 * 简易的缓存服务
 * 
 * @author gengmaozhang01
 * @since 2014-3-15 下午1:29:25
 */
@Service
public class EasyCache {

	private static final Log log = LogFactory.getLog(EasyCache.class);
	private static final WeakHashMap<String, CacheItem> CACHE = new WeakHashMap<String, CacheItem>();

	private class CacheItem {

		private Object value;
		private long millis;
		private long cacheTime;

		public CacheItem(Object value, long millis) {
			this.value = value;
			this.millis = millis;
			this.cacheTime = System.currentTimeMillis();
		}

		public Object getValue() {
			return value;
		}

		/**
		 * 是否过期
		 * 
		 * @author gengmaozhang01
		 * @since 2014-3-15 下午1:48:01
		 */
		public boolean isOutOfDate() {
			return millis > 0 && (System.currentTimeMillis() - cacheTime) > millis;
		}

	}

	public EasyCache() {
		Thread cleaner = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					List<String> keys = new ArrayList<String>(CACHE.keySet());
					for (String key : keys) {
						CacheItem item = CACHE.get(key);
						if (item != null && item.isOutOfDate()) {
							CACHE.remove(key);
						}
					}
				} catch (Exception ex) {
					log.error("clean cache failed", ex);
				} finally {
					try {
						Thread.sleep(10 * 60 * 1000);
					} catch (Exception ex) {
						log.error("sleep failed", ex);
					}
				}
			}
		});
		cleaner.start();
	}

	/**
	 * get value from cache
	 * 
	 * @author gengmaozhang01
	 * @since 2014-3-15 下午2:04:41
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		CacheItem item = CACHE.get(key);
		if (item != null && !item.isOutOfDate()) {
			return (T) item.getValue();
		}
		return null;
	}

	/**
	 * set value to cache
	 * 
	 * @author gengmaozhang01
	 * @since 2014-3-15 下午2:04:52
	 */
	public <T> void set(String key, T value, long seconds) {
		CACHE.put(key, new CacheItem(value, seconds * 1000));
	}

}
