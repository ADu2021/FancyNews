package com.java.duyiyang.ui.gallery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.java.duyiyang.CustomAdapter;
import com.java.duyiyang.FragmentWithArticles;
import com.java.duyiyang.R;
import com.java.duyiyang.database.Article;
import com.java.duyiyang.database.ArticleDao;
import com.java.duyiyang.databinding.FragmentGalleryBinding;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;

// History
public class GalleryFragment extends FragmentWithArticles {

    private FragmentGalleryBinding binding;

    @Override protected void afterParseJson() {}

    void setArticles(Article[] articles) {
        datasetArticle = new ArrayList<Article>(Arrays.asList(articles));
    }

    void loadHistory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Single<Article[]> task = database.selectAll();
                task.subscribe(new DisposableSingleObserver<Article[]>() {
                    @Override
                    public void onSuccess(Article @io.reactivex.rxjava3.annotations.NonNull [] articles) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setArticles(articles);
                                mAdapter.setCustomAdapter();
                                Log.d("loadHistory",articles.length+"success"+mAdapter.getItemCount());
                            }
                        });
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.e("GalleryFragment","onError" + e.getMessage());
                    }
                });
            }
        }).start();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        forceSaveAtStart = true;

        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        onCreateView();

        mRecyclerView = binding.historyRecyclerView;
        mAdapter = new CustomAdapter(this);

        loadHistory();

        refreshLayout = binding.historyRefreshLayout;
        setRefreshLayout();
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh();// pass
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                // todo : 分批次加载？
                refreshlayout.finishLoadMore(0/*,false*/);//传入false表示加载失败
            }
        });

        return root;
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