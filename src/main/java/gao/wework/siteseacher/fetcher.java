/**
 * 
 */
package gao.wework.siteseacher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Gao
 *
 */
public class fetcher extends Thread {
	private Site site;
	private WebContentCallBack callback;
	public fetcher(Site site, WebContentCallBack callback) {
		if(site==null || callback==null) throw new InvalidParameterException("Both parameters are required for the constructor");
		this.site=site;
		this.callback=callback;
	}
	@Override
	public void run() {
		URL url;
		try{
			url = new URL(site.getUrl());
			try (InputStream is = url.openStream();
	                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
	            String content = br.lines().collect(Collectors.joining(System.lineSeparator()));
            	this.callback.webContentCallBack(site, content);
	        } 
		}catch (MalformedURLException e) {
			e.printStackTrace();
			Logger.getLogger(fetcher.class.getName()).log(Level.WARNING, "Site:"+site.getUrl()+" failed fetching.", e);
			this.callback.webContentCallBack(site, null);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.getLogger(fetcher.class.getName()).log(Level.WARNING, "Site:"+site.getUrl()+" failed fetching.", e);
			this.callback.webContentCallBack(site, null);
		}
	}
}
