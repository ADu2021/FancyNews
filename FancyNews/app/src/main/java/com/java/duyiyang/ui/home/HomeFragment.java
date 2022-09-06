package com.java.duyiyang.ui.home;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.java.duyiyang.CustomAdapter;
import com.java.duyiyang.FragmentWithArticles;
import com.java.duyiyang.MainActivity;
import com.java.duyiyang.R;
import com.java.duyiyang.TabAdapter;
import com.java.duyiyang.databinding.FragmentHomeBinding;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.lang.reflect.Method;
import java.util.ArrayList;


public class HomeFragment extends FragmentWithArticles {

    private FragmentHomeBinding binding;
    private TabLayout tabLayout;
    private int saveTab = -1;
    private String lastSelectedTab = "综合";

    protected TabAdapter mTabAdapter;
    protected ConcatAdapter mConcatAdapter;

    boolean first_launch;
    boolean[] tab_show;

    {
        first_launch = true;
        tab_show = new boolean[10];
        for(int i = 0; i < 3; i++)
            tab_show[i] = true;
        for(int i = 3; i < 10; i++)
            tab_show[i] = false;
    }


    protected void afterParseJson() {
        enableAllTab();
        loading = false;
    }

    void newTab(String title) { newTab(title, 0); }
    /**
     * To generate a newTab for TabLayout, and add it's title and behavior
     * @param title title
     * @param type : default 0
     *             0:normalTab--show label the same as title
     *             1:综合--show default
     *             2:今日--show today
     *             -1: do nothing
     * @return the generated tab
     */
    public void newTab(String title, int type) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(title);

        tabLayout.addTab(tab);
        View tabChd = tabLayout.getChildAt(tabLayout.getChildCount()-1);
        if(tabChd instanceof TextView) {
            Log.d("newTab","change text font");
            Typeface typeface = getResources().getFont(R.font.zcool_xiaowei);
            ((TextView) tabChd).setTypeface(typeface);
        }
        if(type == 1) {
            tab.select();
        }
    }

    private void disableAllTab() {
        Log.d("disableAllTab","true");
        for(int i = 0; i < tabLayout.getTabCount(); i++) {
            // tabLayout.getTabAt(i).view.setEnabled(false);
            tabLayout.getTabAt(i).view.setClickable(false);
        }
    }
    private void enableAllTab() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Log.d("enableAllTab","true");
                for(int i = 0; i < tabLayout.getTabCount(); i++) {
                    // tabLayout.getTabAt(i).view.setEnabled(false);
                    tabLayout.getTabAt(i).view.setClickable(true);
                }
            }
        }, 800);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("onCreateView","hf");
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Tab Layout
        tabLayout = binding.tabHome;
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_circle_plus).setId(0));
        newTab("综合",1);
        newTab("今日",2);
        Resources res = getResources();
        for(int i = 0; i < 10; i++) // todo : 10
            if(tab_show[i])
                newTab(res.getStringArray(R.array.categories_array)[i]);

        disableAllTab();

        // concatAdapter
        mAdapter = new CustomAdapter(this);
        mTabAdapter = new TabAdapter();
        mTabAdapter.setTabAdapter(tabLayout, getContext(), tab_show);
        mConcatAdapter = new ConcatAdapter(mAdapter, mTabAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // TODO: show abstract using tab.getText()
                //Log.d("ID",String.valueOf(tab.getId()));
                if(tab.getId() == 0) { // button plus
                    Log.d("getID","0");
                    if(loading)
                        return;
                    mAdapter.hide();
                    mTabAdapter.setTabAdapter(tabLayout, getContext(), tab_show);
                    lastSelectedTab = "+";
                    mTabAdapter.show();
                    refreshLayout.setEnableRefresh(false);
                    refreshLayout.setEnableLoadMore(false);

                    // mRecyclerView.setAdapter(mTabAdapter);
                    return;
                }
                refreshLayout.setEnableRefresh(true);
                refreshLayout.setEnableLoadMore(true);
                String tabName = "";
                try { tabName = tab.getText().toString(); }
                catch(Exception e) { Log.d("TabLayout","Empty Text String"); }
                Log.d("TabName",tabName);
                Log.d("test",tabName+"::"+lastSelectedTab);
                if(tabName != null && tabName.equals(lastSelectedTab))
                    return;
                lastSelectedTab = tabName;
                HomeFragment.super.datasetArticle = new ArrayList<>();
                mTabAdapter.hide(); // todo : fix this
                loading = true;
                // tabLayout.getTabAt(0).view.setEnabled(false);
                tabLayout.setEnabled(false);

                api.setDefault();
                if(tabName.equals("综合")) {
                    // in default order?

                } else if(tabName.equals("今日")) {
                    api.setStartDate();
                } else {
                    Resources res = getResources();
                    String[] categories = res.getStringArray(R.array.categories_array);
                    for(int i = 0; i < 10; i++)
                        if(tabName.equals(categories[i])) {
                            api.setCategories(categories[i]);
                            break;
                        }
                }
                refreshLayout.autoRefresh();
                disableAllTab();

                // refresh();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        refreshLayout = binding.refreshLayout;
        setRefreshLayout();
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.setEnableLoadMore(false);
                disableAllTab();
                HomeFragment.super.datasetArticle = new ArrayList<>();
                api.setPage(1);
                refresh();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                disableAllTab();
                refreshlayout.setEnableRefresh(false);
                String tabn;
                int repeat = 1;
                if(api.pageD == 1 && (tabn = (String)
                        tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText()) != null) {
                    if(tabn.equals("教育"))
                        repeat = 3;
                    else if(tabn.equals("汽车"))
                        repeat = 8;
                }
                api.setPage(api.pageD+repeat);
                refresh();
                // refreshlayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            }
        });

        onCreateView();


        return root;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mConcatAdapter);

        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.setBarTitle("FancyNews");


        // get articles for "综合"(default tab)
        if(first_launch) {
            refreshLayout.autoRefresh();
            first_launch = false;
            Log.d("HomeFragment", "first launch");
        } else {

            Log.d("onViewCreated","not first launch");
            mAdapter.setCustomAdapter();
            //mAdapter.setCustomAdapter(mDatasetTitle, mDatasetImage, mDataSetIndex, num, getContext());
            Log.d("notified","ca");
            enableAllTab();
        }
        // refresh();

    }


    private void refresh() {
        Log.d("refresh","s");
        try {
            Method callback = this.getClass().getMethod("parseJson", String.class);
            api.getContent(this, callback);
        } catch (Exception e) {
            Log.d("Test Home onVC", e.getMessage());
            e.printStackTrace();
        }
    }

    @MainThread
    @CallSuper
    public void onResume() {
        super.onResume();
        Log.d("onSaveInstanceState","r");
        try { if(saveTab >= 0) tabLayout.getTabAt(saveTab).select(); }
        catch (Exception e) { Log.e("onResume", e.getMessage()); }

    }
    @MainThread
    @CallSuper
    public void onPause() {
        // todo : elaborate these in txt
        Log.d("onSaveInstanceState","p");
        saveTab = tabLayout.getSelectedTabPosition();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}