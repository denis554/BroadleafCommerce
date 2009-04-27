package org.broadleafcommerce.profile.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

	public final static String CUSTOMER_COOKIE_NAME = "customerId";

    public static String getCookieValue(HttpServletRequest request, String cookieName) {
    	Cookie[] cookies = request.getCookies();
    	if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookieName.equals(cookie.getName()))
					return (cookie.getValue());
			}
    	}
		return null;
	}

    public static void setCookieValue(HttpServletResponse response, String cookieName, String cookieValue, String path, Integer maxAge) {
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setPath(path);
		if (maxAge != null) {
			cookie.setMaxAge(maxAge);
		}
		response.addCookie(cookie);
    }

    public static void setCookieValue(HttpServletResponse response, String cookieName, String cookieValue) {
    	setCookieValue(response, cookieName, cookieValue, "/", null);
    }

    public static void invalidateCookie(HttpServletResponse response, String cookieName) {
    	setCookieValue(response, cookieName, "", "/", 0);
    }

}
