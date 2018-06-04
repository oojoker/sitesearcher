/**
 * 
 */
package gao.wework.siteseacher;

import java.security.InvalidParameterException;

import javax.naming.InsufficientResourcesException;

/**
 * Thread factory. Potentially could create not only Fetcher thread. 
 * @author Gao
 *
 */
public class MyThreadFactory {
	private static int availableTheads=Constants.MAX_THREAD_POOL_SIZE;
	private static Object lock_availableTheads=new Object(); //Protect lock for availableThreads
	
	public static boolean hasAvailableThread() {
		synchronized (lock_availableTheads) {
			return availableTheads>0;
		}
	}
	
	public static boolean allThreadEnds() {
		synchronized (lock_availableTheads) {
			return availableTheads==Constants.MAX_THREAD_POOL_SIZE;
		}
	}
	/**
	 * Create a thread from the thread pool. Potentially could create a generic thread
	 * @param s Site object
	 * @param callback to handle logic after the content is fetched.
	 * @return a fetcher thread. 
	 * @throws InsufficientResourcesException
	 */
	public static Fetcher createdAThread(Site s, WebContentCallBack callback) throws InvalidParameterException, InsufficientResourcesException {
		if(s==null || callback==null) throw new InvalidParameterException();
		synchronized (lock_availableTheads) {
			if(availableTheads>0) {
				availableTheads--;
				return new Fetcher(s, callback);
			}else
				throw new InsufficientResourcesException();
		}
	}

	public static void releasedAThread() {
		synchronized (lock_availableTheads) {
			if(availableTheads<Constants.MAX_THREAD_POOL_SIZE) //availableThreads should always less than MAX_THREAD_POOL_SIZE in all cases when this method is called.
				availableTheads++;
		}
	}
	
	public static int getNumOfRunningThread() {
		synchronized (lock_availableTheads) {
			return Constants.MAX_THREAD_POOL_SIZE-availableTheads;
		}
	}
}
