package sitesearcher;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;

import javax.naming.InsufficientResourcesException;

import org.junit.Test;

import gao.wework.siteseacher.Constants;
import gao.wework.siteseacher.MyThreadFactory;
import gao.wework.siteseacher.Site;

public class MyThreadFactoryTest {
	Site s=new Site(1,"google.com", 1, 1, 1, 1);
	WebContentCallbackTester cb=new WebContentCallbackTester();
	@Test(expected = InvalidParameterException.class) 
	public void testException1() throws InsufficientResourcesException { 
		MyThreadFactory.createdAThread(null, null);
	}
	
	@Test(expected = InvalidParameterException.class) 
	public void testException2() throws InsufficientResourcesException { 
		MyThreadFactory.createdAThread(s, null);
	}
	
	@Test(expected = InvalidParameterException.class) 
	public void testException3() throws InsufficientResourcesException { 
		MyThreadFactory.createdAThread(null, cb);
	}
	
	@Test(expected = InsufficientResourcesException.class) 
	public void testException4() throws InvalidParameterException, InsufficientResourcesException { 
		for(int i=0; i<=Constants.MAX_THREAD_POOL_SIZE; i++) {
			if(i<Constants.MAX_THREAD_POOL_SIZE) assertTrue(MyThreadFactory.hasAvailableThread());
			else assertFalse(MyThreadFactory.hasAvailableThread());
			MyThreadFactory.createdAThread(s, cb);
		}
	}
	
	@Test
	public void testClearAllThread() {
		int c=MyThreadFactory.getNumOfRunningThread();
		for(int i=0; i<c; i++) {
			assertFalse(MyThreadFactory.allThreadEnds());
			MyThreadFactory.releasedAThread();
			assertEquals(c-i-1, MyThreadFactory.getNumOfRunningThread());
		}
		assertTrue(MyThreadFactory.allThreadEnds());
	}
	
	@Test
	public void testMyThreadFactory() throws InvalidParameterException, InsufficientResourcesException {
		testClearAllThread();
		for(int i=0; i<Constants.MAX_THREAD_POOL_SIZE; i++) {
			assertTrue(MyThreadFactory.hasAvailableThread());
			MyThreadFactory.createdAThread(s, cb);
			assertEquals(i+1, MyThreadFactory.getNumOfRunningThread());
		}
		testClearAllThread();
	}
}
