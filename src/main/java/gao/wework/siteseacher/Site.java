package gao.wework.siteseacher;

import java.security.InvalidParameterException;

/**
 * @author Gao
 */
public class Site {
	//"Rank","URL","Linking Root Domains","External Links","mozRank","mozTrust"
	//example data: 1,"facebook.com/",9616487,1688316928,9.54,9.34
	private int rank;
	private String url;
	private String domain;
	private int linkingRootDomains;
	private int externalLinks;
	private float mozRank;
	private float mozTrust;
	
	public int getRank() {
		return rank;
	}

	public String getUrl() {
		return url;
	}

	public int getLinkingRootDomains() {
		return linkingRootDomains;
	}

	public int getExternalLinks() {
		return externalLinks;
	}

	public float getMozRank() {
		return mozRank;
	}

	public float getMozTrust() {
		return mozTrust;
	}
	
	public Site(int rank, String url, int linkingRootDomains, int externalLinks, float mozRank, float mozTrust){
		this.rank=rank;
		if(url==null || url.trim().length()==0) throw new InvalidParameterException("URL field is required for the constructor");
		this.domain=url;
		this.url="http://www."+url.substring(1, url.length()-2); //We assume all inputs are valid formatted. 
		this.linkingRootDomains=linkingRootDomains;
		this.externalLinks=externalLinks;
		this.mozRank=mozRank;
		this.mozTrust=mozTrust;
	}
	
	@Override
	public String toString() {
		return domain;
	}
}
