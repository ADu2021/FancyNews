package com.java.duyiyang;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.java.duyiyang.database.Article;
import com.java.duyiyang.databinding.FragmentGalleryBinding;
import com.java.duyiyang.ui.gallery.GalleryViewModel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;

public class RssFragment extends FragmentWithArticles {

    private FragmentGalleryBinding binding;

    @Override protected void afterParseJson() {}

    void setArticles(Article[] articles) {
        datasetArticle = new ArrayList<Article>(Arrays.asList(articles));
    }



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View ret = inflater.inflate(R.layout.fragment_rss, container, false);
        rss = true;

        onCreateView();

        mRecyclerView = ret.findViewById(R.id.rss_recyclerView);
        mAdapter = new CustomAdapter(this);



        refreshLayout = ret.findViewById(R.id.rss_refreshLayout);
        setRefreshLayout();
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getRssFeed();
                // refreshlayout.finishRefresh();// pass
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(0/*,false*/);//传入false表示加载失败
            }
        });
        refreshLayout.autoRefresh();

        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.setBarTitle("RSS源（无历史记录）");



        return ret;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}