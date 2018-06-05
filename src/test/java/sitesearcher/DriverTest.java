package sitesearcher;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import gao.wework.siteseacher.Constants;
import gao.wework.siteseacher.Driver;
import gao.wework.siteseacher.MyThreadFactory;

/**
 * @author Gao
 *
 */
//TODO: more tests
public class DriverTest {
	Driver d=new Driver();
	/**
	 * Test method for {@link gao.wework.siteseacher.Driver#main(java.lang.String[])}.
	 */
	@Test
	public void testMain() throws Exception{
		String[] args=new String[] {"-s","face", "-f","https://raw.githubusercontent.com/oojoker/sitesearcher/dev/texturls.txt"};
		Driver.main(args);
		while(!MyThreadFactory.allThreadEnds()){
			this.wait(1000);
		}
		File[] f=findFilesForTerm(new File("./"), "face");
		assertTrue(f.length > 0);
		try(BufferedReader br=new BufferedReader(new FileReader(f[0]))){
			assertTrue(br.readLine().contains("facebook.com"));
		}
	}

	/**
	 * Test method for {@link gao.wework.siteseacher.Driver#initialSites(java.lang.String)}.
	 */
	@Test(expected = MalformedURLException.class) 
	public void testInitialSites1() throws Exception{
		d.initialSites("htp://invalidurl");
	}
	
	@Test(expected = UnknownHostException.class) 
	public void testInitialSites2() throws Exception{
		d.initialSites("http://thisisfakeurl.com/fake.txt");
	}
	
	@Test
	public void testInitialSites3() throws Exception{
		assertEquals(500, d.initialSites(Constants.URL_FILE));
		assertEquals(1, d.initialSites("https://raw.githubusercontent.com/oojoker/sitesearcher/dev/texturls.txt"));
	}

	/**
	 * Test method for {@link gao.wework.siteseacher.Driver#searchSitesForTerm(java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testSearchSitesForTermString() throws Exception {
		assertEquals(1, d.initialSites("https://raw.githubusercontent.com/oojoker/sitesearcher/dev/texturls.txt"));
		d.searchSitesForTerm("test11111111111111");
		while(!MyThreadFactory.allThreadEnds()){
			this.wait(1000);
		}
		File[] f=findFilesForTerm(new File("./"), "test11111111111111");
		assertTrue(f.length > 0);
		try(BufferedReader br=new BufferedReader(new FileReader(f[0]))){
			assertNull(br.readLine());
		}
	}
	
	@Test
	public void testSearchSitesForTermString1() throws Exception {
		assertEquals(1, d.initialSites("https://raw.githubusercontent.com/oojoker/sitesearcher/dev/texturls.txt"));
		d.searchSitesForTerm("facebook");
		while(!MyThreadFactory.allThreadEnds()){
			this.wait(1000);
		}
		File[] f=findFilesForTerm(new File("./"), "facebook");
		assertTrue(f.length > 0);
		try(BufferedReader br=new BufferedReader(new FileReader(f[0]))){
			assertTrue(br.readLine().contains("facebook.com"));
		}
	}
	
	/**
	 * Helper method, returns result files in the destination folder.
	 * @param dir 
	 * @param searchTerm 
	 * @return
	 */
	private static File[] findFilesForTerm(File dir, final String searchTerm) {
	    return dir.listFiles(new FileFilter() {
	        public boolean accept(File pathname) {
	        	try {
					if(pathname.getName().startsWith("result_"+URLEncoder.encode(searchTerm,"UTF-8")+"_"+new SimpleDateFormat("yyyy_MM_dd_HH").format(new Date()))
							&& pathname.getName().endsWith(".txt")) return true;
					else return false;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return false;
				}
	        }
	    });
	}
}
