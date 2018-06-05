package gao.wework.siteseacher;

/**
 * Callback method for fetching the web content
 * We need this callback interface to keep Fetcher Thread purely for fetching content and leave the logic and the file writer in the driver class. 
 * @author Gao
 */
public interface WebContentCallBack {
	void webContentCallBack(Site site, String content) throws Exception;
}
