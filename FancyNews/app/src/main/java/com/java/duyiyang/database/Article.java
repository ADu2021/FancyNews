package com.java.duyiyang.database;

import android.os.Bundle;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.java.duyiyang.RssArticle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity(tableName = "article_table")
public class Article {
    @PrimaryKey
    @NonNull
    public String idx;
    public String title = "";
    public String pubDate = "";
    public ArrayList<String> keywords = new ArrayList<String>();
    public ArrayList<String> image = new ArrayList<String>();
    public ArrayList<String> video = new ArrayList<String>();
    public String content = "";
    public String publisher = "";
    public String category = "";
    public boolean marked = false;

    public void packBundle(Bundle bundle) {
        bundle.putString("idx",idx);
        bundle.putString("title",title);
        bundle.putString("pubDate",pubDate);
        bundle.putStringArrayList("keywords",keywords);
        bundle.putStringArrayList("image",image);
        bundle.putStringArrayList("video",video);
        bundle.putString("content",content);
        bundle.putString("publisher",publisher);
        bundle.putString("category",category);
        bundle.putBoolean("marked",marked);
    }
    public Article(Bundle bundle) {
        idx = bundle.getString("idx");
        title = bundle.getString("title");
        pubDate = bundle.getString("pubDate");
        keywords = bundle.getStringArrayList("keywords");
        image = bundle.getStringArrayList("image");
        video = bundle.getStringArrayList("video");
        content = bundle.getString("content");
        publisher = bundle.getString("publisher");
        category = bundle.getString("category");
        marked = bundle.getBoolean("marked");
    }
    public Article(String idx, String title) { this.idx = idx; this.title = title; }
    public Article(JSONObject item) {
        try{
            idx = item.getString("newsID");
            title = item.getString("title");
            pubDate = item.getString("publishTime");
            JSONArray keywords = item.getJSONArray("keywords");
            for(int i = 0, len = keywords.length(); i < len; i++) {
                this.keywords.add(((JSONObject)keywords.get(i)).getString("word"));
            }
            String imageRaw = item.getString("image");
            if(imageRaw.length() > 5) {
                String[] res = imageRaw.substring(1, imageRaw.length() - 1).split(",");
                for(int i = 0, len = res.length; i < len; i++) {
                    Pattern pattern = Pattern.compile("\\s*(\\S+)\\s*");
                    Matcher matcher = pattern.matcher(res[i]);
                    if(matcher.find())
                        image.add(matcher.group(1));
                }
            }
            String video = item.getString("video");
            this.video.add(video);
            content = item.getString("content");
            publisher = item.getString("publisher");
            category = item.getString("category");
        } catch (Exception e) {
            Log.e("Article JSONObject Constructor", e.getMessage());
        }
    }

    public Article(RssArticle rssArticle) {
        title = rssArticle.title;
        publisher = rssArticle.link;
        content = rssArticle.description;
        pubDate = rssArticle.pubDate;
    }

}
