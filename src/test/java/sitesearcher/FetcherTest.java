/**
 * 
 */
package sitesearcher;

import static org.junit.Assert.*;

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
	Site s=new Site(1,"\"google.com\"", 1, 1, 1, 1);
	WebContentCallbackTester cb=new WebContentCallbackTester();
	/**
	 * Test method for {@link gao.wework.siteseacher.Fetcher#run()}.
	 * @throws InsufficientResourcesException 
	 * @throws InvalidParameterException 
	 */
	@Test
	public void testRun() throws InvalidParameterException, InsufficientResourcesException {
		Fetcher f=MyThreadFactory.createdAThread(s, cb);
		f.run();
	}
}
