package com.java.duyiyang;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Xml;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.java.duyiyang.database.Article;
import com.java.duyiyang.database.ArticleDao;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public abstract class FragmentWithArticles extends Fragment {
    protected ApiAdapter api = new ApiAdapter();

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;

    protected RecyclerView.LayoutManager mLayoutManager;
    /*
    protected String[] mDatasetTitle;
    protected String[] mDatasetImage;
    protected int[] mDataSetIndex;
    public Article[] mDataSetArticle;
    */

    public ArrayList<Article> datasetArticle = new ArrayList<Article>();
    // todo : new this when changing tabs

    protected RefreshLayout refreshLayout;

    public ArticleDao database;

    protected boolean loading = false;
    protected boolean forceSaveAtStart = false;
    protected boolean rss = false;

    protected abstract void afterParseJson(); // actually after parse xml use this too

    private String getImageUrl(String org) {
        Pattern pattern = Pattern.compile("\\[(\\S{6,}?),");
        Matcher matcher = pattern.matcher(org);
        if(matcher.find())
            return new String(matcher.group(1));
        pattern = Pattern.compile("\\[\\s*(\\S{6,})\\s*\\]");
        matcher = pattern.matcher(org);
        if(matcher.find())
            return new String(matcher.group(1));
        return new String("-1");
    }

    /**
     * Callback method for ApiAdapter.getContent()
     * @param inp
     * callback method should use "getActivity().runOnUiThread(new Runnable() ..."
     */
    public void parseJson(String inp) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("parseJson","start");
                int len = 0;
                ArrayList<String> title = new ArrayList<String>();
                ArrayList<String> image = new ArrayList<String>();
                ArrayList<Integer> index = new ArrayList<Integer>();
                try{
                    JSONObject json = new JSONObject(inp);
                    len = json.getInt("total");
                    JSONArray data = json.getJSONArray("data");
                    JSONObject item;

                    Log.d("parseJson","Len:" + String.valueOf(len) + " " + String.valueOf(data.length()));
                    len = data.length();
                    for(int i = 0; i < len; i++) {
                        item = data.getJSONObject(i);

                        datasetArticle.add(new Article(item));
                    }
                } catch (Exception e) {
                    if(e.getMessage() != null) Log.e("parseJson",e.getMessage());
                }
                try{
                    /*
                    mDatasetTitle = (String[])title.toArray(new String[title.size()]);
                    mDatasetImage = (String[])image.toArray(new String[image.size()]);
                    mDataSetIndex = ArrayUtils.toPrimitive((Integer[]) index.toArray(new Integer[index.size()]));
                    */
                    //mAdapter.setCustomAdapter(mDatasetTitle, mDatasetImage, mDataSetIndex, num, getContext());
                    mAdapter.setCustomAdapter();
                    // mRecyclerView.setAdapter(mAdapter);
                } catch (Exception e) {
                    Log.e("parseJson", e.getMessage());
                    refreshLayout.finishRefresh(false);//传入false表示刷新失败
                } finally {
                    loading = false;
                    // tabLayout.getTabAt(0).view.setEnabled(true);
                    refreshLayout.setEnableLoadMore(true);
                    refreshLayout.setEnableRefresh(true);
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                    afterParseJson();
                }

                Log.d("parseJson","finish"+String.valueOf(len));
            }
        });

    }

    /**
     * async function, no callback
     */
    public void getRssFeed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                datasetArticle = new ArrayList<>();
                HttpURLConnection cnn = null;
                try {
                    Log.d("getRssFeed","start");
                    SharedPreferences preference = getActivity().getPreferences(Context.MODE_PRIVATE);
                    String source = preference.getString("preference_rss_source", "https://www.zhihu.com/rss");
                    Log.d("getsource",source);
                    URL url = new URL(source);
                    // todo: change URL
                    cnn = (url.getProtocol().equals("http")) ?
                            (HttpURLConnection) url.openConnection():
                            (HttpsURLConnection) url.openConnection();
                    cnn.setUseCaches(true); // todo delete this
                    cnn.setRequestMethod("GET");
                    cnn.setConnectTimeout(10000);
                    cnn.setReadTimeout(10000);
                    Log.d("getRssFeed","about to get IS"+cnn.getClass().getName());
                    InputStream s = cnn.getInputStream();
                    if(s == null)
                        Log.d("debug", "(s == null)");
                    Log.d("getRssFeed","about to parse xml");
                    parseXML(s);

                } catch(Exception e) {
                    // wrong [ext.setText(e.getMessage());] : not in the same thread. t
                    Log.e("资源获取失败",e.getMessage());
                } finally {
                    if(cnn != null) cnn.disconnect();
                }
            }
        }).start();
    }

    public void afterParseXML() {
        Log.d("afterParseXML","start");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    /*
                    mDatasetTitle = (String[])title.toArray(new String[title.size()]);
                    mDatasetImage = (String[])image.toArray(new String[image.size()]);
                    mDataSetIndex = ArrayUtils.toPrimitive((Integer[]) index.toArray(new Integer[index.size()]));
                    */
                    //mAdapter.setCustomAdapter(mDatasetTitle, mDatasetImage, mDataSetIndex, num, getContext());
                    mAdapter.setCustomAdapter();
                    // mRecyclerView.setAdapter(mAdapter);
                } catch (Exception e) {
                    Log.e("parseXML", e.getMessage());
                    refreshLayout.finishRefresh(false);//传入false表示刷新失败
                } finally {
                    loading = false;
                    // tabLayout.getTabAt(0).view.setEnabled(true);
                    refreshLayout.setEnableLoadMore(true);
                    refreshLayout.setEnableRefresh(true);
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();

                    afterParseJson();
                }
            }
        });
    }
    public void parseXML(InputStream data) {
        try{
            Log.d("parseXML","start");
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(data, "utf-8");
            String nodeName = "";
            String title = "";
            String link = "";
            String description = "";
            String pubDate = "";
            boolean is_first = true;
            int eventType = parser.getEventType();
            Log.d("parseXML","1");

            while(eventType != XmlPullParser.END_DOCUMENT) {
                Log.d("parseXML","2");
                nodeName = parser.getName();
                if(eventType == XmlPullParser.START_TAG) {
                    if(nodeName.equals("item")) {
                        if(is_first)
                            is_first = false;
                        else {
                            Log.d("parseXML","addArticle");
                            datasetArticle.add(new Article(new RssArticle(title, link, description, pubDate)));
                            title = link = description = pubDate = "";
                        }
                    }
                    if(nodeName.equals("title")) {
                        title = parser.nextText();
                        Log.d("found_title", title);
                    }
                    if(nodeName.equals("description")) {
                        description += parser.nextText();
                        Log.d("found_description", description);
                    }
                    if(nodeName.equals("content:encoded")) {
                        description += parser.nextText();
                    }
                    if(nodeName.equals("link")) {
                        link = parser.nextText();
                        Log.d("found_link", link);
                    }
                    if(nodeName.equals("pubDate")) {
                        pubDate = parser.nextText();
                        Log.d("found_pubDate", pubDate);
                    }
                }
                eventType = parser.next();
            }
            if(!title.equals("") || !description.equals("") || !link.equals("") || !pubDate.equals("")) {
                //articles.add(new rssArticle(title, link, description, pubDate));
                datasetArticle.add(new Article(new RssArticle(title, link, description, pubDate)));
            }
        } catch (Exception e) {
            Log.e("parseXML error", e.getMessage());
        }
        afterParseXML();
    }

    protected void setRefreshLayout() {
        refreshLayout.setEnableAutoLoadMore(false);//是否启用列表惯性滑动到底部时自动加载更多
        refreshLayout.setDisableContentWhenRefresh(true);//是否在刷新的时候禁止列表的操作
        refreshLayout.setDisableContentWhenLoading(true);//是否在加载的时候禁止列表的操作
        refreshLayout.setPrimaryColorsId(R.color.purple_200);
        refreshLayout.setRefreshHeader(new BezierRadarHeader(getContext()).setEnableHorizontalDrag(true));
        refreshLayout.setRefreshFooter(new BallPulseFooter(getContext())
                .setSpinnerStyle(SpinnerStyle.FixedBehind)
                .setNormalColor(getResources().getColor(R.color.purple_500))
                .setAnimatingColor(getResources().getColor(R.color.purple_500)));

    }

    protected void onCreateView() {

        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
        setEnterTransition(transitionInflater.inflateTransition(R.transition.explode));

        setDatabase();

    }

    protected void setDatabase() {
        database = ((MainActivity)getActivity()).mArticleDao;
    }
}
