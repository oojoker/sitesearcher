package sitesearcher;

import static org.junit.Assert.*;


import gao.wework.siteseacher.Site;
import gao.wework.siteseacher.WebContentCallBack;

public class WebContentCallbackTester implements WebContentCallBack {
	@Override
	public void webContentCallBack(Site site, String content) {
		assertNotNull(content);
	}
}
