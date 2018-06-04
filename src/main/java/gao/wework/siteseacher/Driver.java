package gao.wework.siteseacher;

import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.naming.InsufficientResourcesException;

import org.apache.commons.validator.routines.UrlValidator;


/**
 * @author Gao
 * @version 0.1
 */

/**
 * Coding Challenge for WeWork
 * Given a list of urls in urls.txt: https://s3.amazonaws.com/fieldlens-public/urls.txt, write a program that will fetch each page and determine whether a search term exists on the page (this search can be a really rudimentary regex - this part isn't too important).
 * You can make up the search terms. Ignore the addition information in the urls.txt file.
 * Constraints
 * 		Search is case insensitive
 * 		Should be concurrent.
 * 		But! It shouldn't have more than 20 HTTP requests at any given time.
 * 		The results should be writted out to a file results.txt
 * 		Avoid using thread pooling libraries like Executor, ThreadPoolExecutor, Celluloid, or Parallel streams.
 */
public class Driver implements WebContentCallBack {
	//Global Items
	ArrayList<Site> sites=new ArrayList<Site>();
	Deque<Site> operationQueue=new LinkedList<Site>();
	private int availableTheads=Constants.MAX_THREAD_POOL_SIZE;
	Object lock_availableTheads=new Object();
	Object lock_matchingSites=new Object();
	//Object lock_driver=new Object();
	String searchTerm=null;
	ArrayList<Site> matchingSites=new ArrayList<Site>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Driver d=new Driver();
		try {
			d.searchSitesForTerm(Constants.URL_FILE, "test");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initialSites(String siteURLs) throws MalformedURLException{
		try {
            if (!new UrlValidator().isValid(siteURLs)) {
                throw new MalformedURLException("siteURL is not a valid url");
            }

            URL url = new URL(siteURLs);
            try (InputStream is = url.openStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                br.lines()
                	.skip(1)
                	.forEach(line->{
                		String[] values=line.split(",");
                		Site s=new Site(Integer.parseInt(values[0]), values[1], 
                				Integer.parseInt(values[2]), Integer.parseInt(values[3])
                				, Float.parseFloat(values[4]), Float.parseFloat(values[5]));
                		this.sites.add(s);
                		this.operationQueue.add(s);
                	});
            }
        } catch (IOException ex) {
            Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	public void searchSitesForTerm(String searchTerm){
		if(sites.size()==0) throw new InvalidParameterException("Sites have not been initialized. Please call initialSites(String siteURLs) first or directly call searchSitesForTerm(String siteURLs, String searchTerm) ");
		putSitesInQueue();
		this.searchTerm=searchTerm;
		startFetching();
	}
	
	private void putSitesInQueue() {
		this.operationQueue.addAll(this.sites);
	}

	public void searchSitesForTerm(String siteURLs, String searchTerm) throws MalformedURLException{
		initialSites(siteURLs);
		this.searchTerm=searchTerm;
		startFetching();
	}
	
	public synchronized void startFetching(){
		while(this.operationQueue.size()>0){
			if(hasAvailableThread()){
				Site s=this.operationQueue.pop();
				fetcher f=new fetcher(s, this);
				try {
					createdAThread();
				} catch (InsufficientResourcesException e) {
					Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, "Not Enough Resources", e);
				}
				f.start();
			}else{
				try {
					this.wait(500);
				} catch (InterruptedException e) {
					Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		}
	}
	
	boolean hasAvailableThread() {
		synchronized (lock_availableTheads) {
			return availableTheads>0;
		}
	}
	
	void createdAThread() throws InsufficientResourcesException {
		synchronized (lock_availableTheads) {
			if(availableTheads>0)
				availableTheads--;
		}
	}

	void releasedAThread() {
		synchronized (lock_availableTheads) {
			if(this.availableTheads<Constants.MAX_THREAD_POOL_SIZE)
				this.availableTheads++;
		}
	}

	@Override
	public void webContentCallBack(Site site, String content) {
		if(content!=null && Pattern.compile(this.searchTerm).matcher(content).find()){
			synchronized(lock_matchingSites){
				matchingSites.add(site);
				System.out.println("Site:"+site.getUrl()+" is a match");
			}
		}
		releasedAThread();
		System.out.println("Site:"+site.getUrl()+" is back with availableThread#:"+availableTheads);
	}
}
