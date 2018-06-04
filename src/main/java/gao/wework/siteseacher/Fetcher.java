
package gao.wework.siteseacher;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A fetcher thread is particularly designed to fetch content from a Site.
 * @author Gao
 *
 */
public class Fetcher extends Thread {
	private Site site;
	private WebContentCallBack callback;
	/**
	 * Constructor 
	 * @param site Site that going to fetch
	 * @param callback Callback method will be called with the Site object and the content from it. *Note: Content can be null if any exception happened.  
	 */
	public Fetcher(Site site, WebContentCallBack callback) {
		if(site==null || callback==null) throw new InvalidParameterException("Both parameters are required for the constructor");
		this.site=site;
		this.callback=callback;
	}
	@Override
	public void run() {
		try{
			URL url = new URL(site.getUrl());
			URLConnection urlConnection = url.openConnection();
			urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
			urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
			try (InputStream is = urlConnection.getInputStream();
	                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
	            String content = br.lines().collect(Collectors.joining(System.lineSeparator()));
            	this.callback.webContentCallBack(site, content);
	        } 
		}catch (Exception e) {
			Logger.getLogger(Fetcher.class.getName()).log(Level.WARNING, "Site:"+site.getUrl()+" failed fetching.", e);
			this.callback.webContentCallBack(site, null); //any exception, null is returned as the content. 
		}
	}
}
