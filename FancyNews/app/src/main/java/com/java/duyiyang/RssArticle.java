package com.java.duyiyang;

public class RssArticle {
    public String title;
    public String link;
    public String description;
    public String pubDate;

    public RssArticle(final String ttl, final String lnk, final String des, final String pub) {
        title = ttl;
        link = lnk;
        description = des;
        pubDate = pub;
    }
}