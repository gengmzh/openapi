/**
 * 
 */
package cn.seddat.openapi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author gengmaozhang01
 * @since 2013-12-31 下午11:15:31
 */
public class LogginInterceptor implements HandlerInterceptor {

	private final Log log = LogFactory.getLog(LogginInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String uri = request.getRequestURI();
		if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
			uri += "?" + request.getQueryString();
		}
		log.info("request [" + uri + "] is coming in, and handler is " + handler.getClass().getName());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {
			String uri = request.getRequestURI();
			if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
				uri += "?" + request.getQueryString();
			}
			log.info("request [" + uri + "] is handled, and result is " + modelAndView.getModelMap());
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if (ex != null) {
			String uri = request.getRequestURI();
			if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
				uri += "?" + request.getQueryString();
			}
			log.error("request [" + uri + "] is crashed", ex);
		}
	}

}
