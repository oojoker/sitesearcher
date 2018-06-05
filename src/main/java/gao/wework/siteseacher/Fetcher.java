
package gao.wework.siteseacher;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
	//TODO: besides 301 and 302 redirect, there are other HTTP cases need attention, 
	// such as SSLHandshakeException for networksolutions.com
	// and java.io.IOException: Premature EOF for ebay.com
	@Override
	public void run() {
		try{
			boolean redirected=false;
			int responseCode=0;
			int tries=0;
			String location=site.getUrl();
			HttpURLConnection urlConnection=null;
			do{
				URL url = new URL(location);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
				urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
				urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
				urlConnection.setRequestMethod("GET");
				urlConnection.setInstanceFollowRedirects(true);
				urlConnection.setUseCaches(false);
				urlConnection.setAllowUserInteraction(false);
				urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				responseCode = urlConnection.getResponseCode();
				
				if(responseCode==HttpURLConnection.HTTP_MOVED_TEMP || responseCode==HttpURLConnection.HTTP_MOVED_PERM){ //handle 302 and 301 redirect. 
					redirected=true;
					location = urlConnection.getHeaderField("Location");
					tries++;
				}else
					redirected=false;
			}while(redirected && tries<3);
			
			if (responseCode == HttpURLConnection.HTTP_OK){
				try (InputStream is = urlConnection.getInputStream();
		                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
		            String content = br.lines().collect(Collectors.joining(System.lineSeparator()));
	            	this.callback.webContentCallBack(site, content);
		        } 
			}else this.callback.webContentCallBack(site, null); //any other case, null is returned as the content. 
		}catch (Exception e) {
			Logger.getLogger(Fetcher.class.getName()).log(Level.WARNING, "Site:"+site.getUrl()+" failed fetching.", e);
			try {
				this.callback.webContentCallBack(site, null); //any exception, null is returned as the content. 
			} catch (Exception e1) {
			} 
		}
	}
}
