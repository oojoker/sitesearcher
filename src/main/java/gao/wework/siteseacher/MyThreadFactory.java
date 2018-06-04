/**
 * 
 */
package gao.wework.siteseacher;

import javax.naming.InsufficientResourcesException;

/**
 * Thread factory. Potentially could create not only Fetcher thread. 
 * @author Gao
 *
 */
public class MyThreadFactory {
	private static int availableTheads=Constants.MAX_THREAD_POOL_SIZE;
	private static Object lock_availableTheads=new Object(); //Protect lock for availableThreads
	
	static boolean hasAvailableThread() {
		synchronized (lock_availableTheads) {
			return availableTheads>0;
		}
	}
	
	static boolean allThreadEnds() {
		synchronized (lock_availableTheads) {
			return availableTheads==Constants.MAX_THREAD_POOL_SIZE;
		}
	}
	
	static Fetcher createdAThread(Site s, WebContentCallBack callback) throws InsufficientResourcesException {
		synchronized (lock_availableTheads) {
			if(availableTheads>0) {
				availableTheads--;
				return new Fetcher(s, callback);
			}
			return null;
		}
	}

	static void releasedAThread() {
		synchronized (lock_availableTheads) {
			if(availableTheads<Constants.MAX_THREAD_POOL_SIZE)
				availableTheads++;
		}
	}
	
	static int getNumOfRunningThread() {
		synchronized (lock_availableTheads) {
			return Constants.MAX_THREAD_POOL_SIZE-availableTheads;
		}
	}
}
