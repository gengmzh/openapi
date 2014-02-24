package cn.seddat.openapi.weather;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 天气信息获取控制器
 * 
 * @author gengmaozhang01
 * @since 2013-12-30 下午10:14:03
 */
@Controller
@RequestMapping("/api/weather")
public class WeatherController {

	private final Log log = LogFactory.getLog(WeatherController.class);

	@Autowired
	private WeatherClient weatherClientService;
	@Autowired
	private AQIClient aqiClientService;

	@ResponseBody
	@RequestMapping(value = "/realtime/{citycode}", method = RequestMethod.GET)
	public ModelMap getRealtimeWeather(@PathVariable("citycode") String citycode) throws Exception {
		ModelMap model = new ModelMap();
		// args
		if (citycode == null || citycode.isEmpty()) {
			model.addAttribute("code", 1);
			model.addAttribute("message", "city is required");
			return model;
		}
		// request
		try {
			Map<String, Object> weather = weatherClientService.queryRealtimeWeather(citycode);
			model.addAttribute("code", 0);
			model.putAll(weather);
		} catch (Exception ex) {
			model.addAttribute("code", 1);
			model.addAttribute("message", "request realtime weather failed");
			log.error("get realtime weather failed", ex);
		}
		return model;
	}

	@ResponseBody
	@RequestMapping(value = "/forecast/{citycode}", method = RequestMethod.GET)
	public ModelMap getForecastWeather(@PathVariable("citycode") String citycode) throws Exception {
		ModelMap model = new ModelMap();
		// args
		if (citycode == null || citycode.isEmpty()) {
			model.addAttribute("code", 1);
			model.addAttribute("message", "city is required");
			return model;
		}
		// request
		try {
			Map<String, Object> weather = weatherClientService.queryForecastWeather(citycode);
			model.addAttribute("code", 0);
			model.putAll(weather);
		} catch (Exception ex) {
			model.addAttribute("code", 1);
			model.addAttribute("message", "request forecast weather failed");
			log.error("get forecast weather failed", ex);
		}
		return model;
	}

	@ResponseBody
	@RequestMapping(value = "/aqi/{citycode}", method = RequestMethod.GET)
	public ModelMap getAirQualityIndex(@PathVariable("citycode") String citycode) throws Exception {
		ModelMap model = new ModelMap();
		// args
		if (citycode == null || citycode.isEmpty()) {
			model.addAttribute("code", 1);
			model.addAttribute("message", "city is required");
			return model;
		}
		// request
		try {
			Map<String, Object> weather = aqiClientService.queryAirQualityIndex(citycode);
			model.addAttribute("code", 0);
			model.putAll(weather);
		} catch (Exception ex) {
			model.addAttribute("code", 1);
			model.addAttribute("message", "get AQI failed");
			log.error("get AQI failed", ex);
		}
		return model;
	}

}
