/**
 * 
 */
package sitesearcher;

import java.security.InvalidParameterException;

import javax.naming.InsufficientResourcesException;

import org.junit.Test;

import gao.wework.siteseacher.Fetcher;
import gao.wework.siteseacher.MyThreadFactory;
import gao.wework.siteseacher.Site;

/**
 * @author yuegao
 *
 */
public class FetcherTest {
	WebContentCallbackTester success=new WebContentCallbackTester();
	WebContentFailureTester failure=new WebContentFailureTester();
	
	@Test(expected = InvalidParameterException.class) 
	public void testException1() throws InsufficientResourcesException { 
		Site s=new Site(1, null, 1, 1, 1, 1);
		MyThreadFactory.createdAThread(s, success);
	}
	
	@Test(expected = InvalidParameterException.class) 
	public void testException2() throws InsufficientResourcesException { 
		Site s=new Site(1, " ", 1, 1, 1, 1);
		MyThreadFactory.createdAThread(s, success);
	}
	
	/**
	 * Test method for {@link gao.wework.siteseacher.Fetcher#run()}.
	 * @throws InsufficientResourcesException 
	 * @throws InvalidParameterException 
	 */
	@Test
	public void testRunSuccess() throws InvalidParameterException, InsufficientResourcesException {
		Site s=new Site(1,"\"google.com/\"", 1, 1, 1, 1);
		Fetcher f=MyThreadFactory.createdAThread(s, success);
		f.run();
	}
	
	@Test
	public void testRunFailure() throws InvalidParameterException, InsufficientResourcesException {
		Site s=new Site(1,"\"thisisafakeurl.com/\"", 1, 1, 1, 1);
		Fetcher f=MyThreadFactory.createdAThread(s, failure);
		f.run();
	}
	
	//TODO add more test cases for http 403, 500, connect timeout and read timeout etc.
}
