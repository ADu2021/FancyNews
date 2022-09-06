package com.java.duyiyang;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDate;

import javax.net.ssl.HttpsURLConnection;

public class ApiAdapter {
    //https://api2.newsminer.net/svc/news/queryNewsList?size=15&startDate=2021-08-20&endDate=2021-08-30&words=拜登&categories=科技
    static private String baseUrl = "https://api2.newsminer.net/svc/news/queryNewsList?";
    private String size = "size=15";
    private String startDate = "startDate=";
    private String endDate = "endDate=";
    private String words = "words=";
    private String categories = "categories=";
    private String page = "&page=";
    private String getUrl() {
        return baseUrl+size+"&"+startDate+"&"+endDate+"&"+words+"&"+categories+"&"+page; }
    public int sizeD = 15; // = getSize
    public int pageD = 1;
    public ApiAdapter() {
        setDefault();
    }
    public void setDefault() {
        setDefaultButPage();
        setPage(1);
    }
    public void setDefaultButPage() {
        setSize(15);
        startDate = "startDate="; //+LocalDate.now().plusDays(-365).toString();
        endDate = "endDate="+LocalDate.now().toString();
        words = "words=";
        categories = "categories=";
    }
    public void setSize(int _size) { size = "size="+String.valueOf(_size); sizeD = _size;}
    public void setPage(int _page) { page = "page="+String.valueOf(_page); pageD = _page;}
    public void addPage() { setPage(pageD+1); }
    public void setStartDate() { setStartDate(LocalDate.now().plusDays(-1).toString()); }
    public void setStartDate(String _date) { startDate = "startDate="+_date;}
    public void setEndDate() { setEndDate(LocalDate.now().toString()); }
    public void setEndDate(String _date) { endDate = "endDate="+_date;}
    public void setWords(String _words) { words = "words="+_words;}
    public void setCategories(String _categories) { categories = "categories="+_categories;}

    private StringBuilder sb;
    private String result = "";

    // TODO : ADD cache, so that i haven't to get the same website again.
    //  cache stays for like xHours.

    private void getContentComplete(Object parent, Method callback) {
        Log.d("api","getContentComplete()");
        try {
            callback.invoke(parent, result);
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.e("getContentComplete", e.getMessage());
        }
    }
    public void getContent(Object parent, Method callback) {
        Log.d("getContent","enter");
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpsURLConnection cnn = null;
                try {
                    Log.d("getContent","start");
                    URL url = new URL(getUrl());
                    Log.d("getContent","URL="+getUrl());
                    cnn = (HttpsURLConnection) url.openConnection();
                    cnn.setRequestMethod("GET");
                    cnn.setConnectTimeout(15000);
                    cnn.setReadTimeout(15000);
                    InputStream s = cnn.getInputStream();
                    if(s == null)
                        Log.d("debug", "(s == null)");
                    BufferedReader br = new BufferedReader(new InputStreamReader(s));
                    sb = new StringBuilder();
                    String ln;
                    while((ln = br.readLine()) != null) {
                        sb.append(ln);
                    }
                    result = sb.toString();
                    Log.d("getContent", url+" "+result);
                } catch(Exception e) {
                    // wrong [ext.setText(e.getMessage());] : not in the same thread. t
                    Log.e("资源获取失败",e.getMessage());
                } finally {
                    if(cnn != null) cnn.disconnect();
                    getContentComplete(parent, callback);
                    Log.d("getContent","end");
                }
            }
        }).start();
    }

    public void packBundle(Bundle bundle) {
        bundle.putInt("size",sizeD);
        bundle.putInt("page",pageD);
        bundle.putString("startDate",startDate);
        bundle.putString("endDate",endDate);
        bundle.putString("words",words);
        bundle.putString("categories",categories);
    }
    public ApiAdapter(Bundle bundle) {
        setDefault();
        try {
            setSize(bundle.getInt("size"));
            setPage(bundle.getInt("page"));
            startDate = bundle.getString("startDate");
            endDate = bundle.getString("endDate");
            words = bundle.getString("words");
            categories = bundle.getString("categories");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
