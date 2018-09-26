package cns.workspace.lib.androidsdk.httputils.cookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public final class SimpleCookieJar implements CookieJar {

	private final HashMap<HttpUrl,List<Cookie>> map = new HashMap<>();

	@Override
	public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		map.put(url,cookies);
	}

	@Override
	public synchronized List<Cookie> loadForRequest(HttpUrl url) {
		List<Cookie> result = new ArrayList<>();
		List<Cookie> allCookies = map.get(url);
		if (allCookies != null){
			result.addAll(allCookies);
		}
		return result;
	}
}
