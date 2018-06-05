package gao.wework.siteseacher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.naming.InsufficientResourcesException;

import org.apache.commons.validator.routines.UrlValidator;


/**
 * @author Gao
 * @version 0.3
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
	ArrayList<Site> sites=new ArrayList<Site>(); //hold sites read from the file.
	Deque<Site> operationQueue=new LinkedList<Site>(); //hold sites that have not been proceed. 
	
	String searchTerm=null; //Searching term. we need this property for the callback method.
	
	Object lock_matchingSites_bw=new Object(); //Protecting lock for the BufferWriter.
	BufferedWriter bw=null;
	
	/**
	 * @param args 
	 * You can use following configs:<br/>
	 * -s SearchTerm<br/>
	 * -f URL to a sites file.
	 */
	public static void main(String[] args) {
		Driver d=new Driver();
		String searchTerm="test";
		String urlfile=Constants.URL_FILE;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-s") && i+1<args.length) {
				searchTerm=args[i+1];
			}else if(args[i].equalsIgnoreCase("-f") && i+1<args.length) {
				urlfile=args[i+1];
			}
		}
		try{
			d.initialSites(urlfile);
			d.searchSitesForTerm(searchTerm);
		}catch(Exception ex){
			//TODO handle Exception later
		}
	}
	
	/**
	 * Initialize "Sites" by providing a text file contains all URLs.
	 * @param siteURLs an url to a file contains all URLs
	 * @return count of sites
	 */
	public int initialSites(String siteURLs) throws Exception{
		try {
            if (!new UrlValidator().isValid(siteURLs)) {
                throw new MalformedURLException("siteURL is not a valid url");
            }

            URL url = new URL(siteURLs);
            try (InputStream is = url.openStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            	this.sites.clear();
                br.lines()
                	.skip(1)
                	.forEach(line->{
                		String[] values=line.split(",");
                		Site s=new Site(Integer.parseInt(values[0]), values[1], 
                				Integer.parseInt(values[2]), Integer.parseInt(values[3])
                				, Float.parseFloat(values[4]), Float.parseFloat(values[5]));
                		this.sites.add(s);
                	});
            }
            return this.sites.size(); 
        } catch (Exception ex) {
            Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, "Failed in initialization sites.", ex);
            throw ex;
        }
	}
	
	/**
	 * Initialize the BufferedWriter to the result file.
	 * The file will be /results/result_searchTerm_timestamp.txt
	 * @param searchTerm
	 */
	private void initialResultFile(String searchTerm) throws Exception {
		//TODO: we can potentially put all file related operations in one class other than this driver class. 
		this.searchTerm=searchTerm;
		File file;
		try {
			file = new File("result_"+URLEncoder.encode(searchTerm,"UTF-8")+"_"+new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date())+".txt");
	        if (file.exists()) 
	        		file.delete();
	        file.createNewFile();
	        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
	        bw = new BufferedWriter(fw);
		} catch (Exception ex) {
			Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, "Result file is not accessible", ex);
			throw ex;
		}
	}
	
	/**
	 * add all sites into the operationQueue
	 * I put it in a separate method just in case we need to add further logic later. 
	 */
	private void putSitesInQueue() {
		this.operationQueue.addAll(this.sites);
	}
	
	
	/**
	 * The method will fetch each site and determine whether a search term exists on the landing page 
	 * and the result will be saved in /results/result_searchTerm_timestamp.txt
	 * @param searchTerm search term that will be matched upon. 
	 * @throws InvalidParameterException
	 */
	public void searchSitesForTerm(String searchTerm) throws Exception{
		if(sites.size()==0) throw new InvalidParameterException("Sites have not been initialized. Please call initialSites(String siteURLs) first or directly call searchSitesForTerm(String siteURLs, String searchTerm) ");
		initialResultFile(searchTerm);
		putSitesInQueue();
		startFetching();
	}
	
	/**
	 * @deprecated please call initialSites with siteURLs first, and then call searchSitesForTerm(String searchTerm) method. 
	 * We break it down for re-using the sites for multiple search terms.
	 * @param siteURLs an url to a file contains all URLs
	 * @param searchTerm search term that will be matched upon. 
	 */
	public void searchSitesForTerm(String siteURLs, String searchTerm) throws Exception{
		initialSites(siteURLs);
		initialResultFile(searchTerm);
		putSitesInQueue();
		startFetching();
	}
	
	/**
	 * Main dispatcher method for operation queue.
	 */
	private synchronized void startFetching() throws Exception{
		while(this.operationQueue.size()>0){ //more sites to go
			if(MyThreadFactory.hasAvailableThread()){ //have available thread to handle it.
				Site s=this.operationQueue.pop();
				try {
					Fetcher f=MyThreadFactory.createdAThread(s, this);
					f.start();
				} catch (InsufficientResourcesException e) {
					Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, "Not Enough Resources", e);
					throw e;
				}
			}else{ //not enough resources at this moment, wait for a sec.
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, "Main Thread Interrupted.", e);
					throw e;
				}
			}
		}
		while(!MyThreadFactory.allThreadEnds()) { //if still have running child thread, wait for a sec.
			try {
				this.wait(1000);
			} catch (InterruptedException e) {
				Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, "Main Thread Interrupted.", e);
				throw e;
			}
		}
		try {
			bw.close(); //We went through everything, let's close the BufferWriter.
		} catch (IOException e) {
			Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, "Result file is not accessible.", e);
			throw e;
		}
	}
	

	/**
	 * Callback method for fetching the web content
	 * if the returning content has search term somewhere (case sensitive), we will write the site domain into the BufferWriter.
	 */
	@Override
	public void webContentCallBack(Site site, String content) throws Exception {
		if(content!=null && Pattern.compile(this.searchTerm).matcher(content).find()){//Pattern matcher is case sensitive
			synchronized(lock_matchingSites_bw){
				try {
					bw.write(site+"\n");
				} catch (IOException e) {
					Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, "Failed to write to result file for site: "+site.toString(), e);
					throw e;
				}
				System.out.println("Site:"+site.getUrl()+" is a match");
			}
		}
		MyThreadFactory.releasedAThread();
		System.out.println("Site:"+site.getUrl()+" is back. Currently running threads:"+MyThreadFactory.getNumOfRunningThread());
	}
}
