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

public class FavoriteFragment extends FragmentWithArticles {

    private FragmentGalleryBinding binding;

    @Override protected void afterParseJson() {}

    void setArticles(Article[] articles) {
        datasetArticle = new ArrayList<Article>(Arrays.asList(articles));
    }

    void loadFavorite() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Single<Article[]> task = database.selectAllFavorite();
                task.subscribe(new DisposableSingleObserver<Article[]>() {
                    @Override
                    public void onSuccess(Article @io.reactivex.rxjava3.annotations.NonNull [] articles) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setArticles(articles);
                                mAdapter.setCustomAdapter();
                                Log.d("loadFavorite",articles.length+"success"+mAdapter.getItemCount());
                            }
                        });
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.e("loadFavorite","onError" + e.getMessage());
                    }
                });
            }
        }).start();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        forceSaveAtStart = true;

        View ret = inflater.inflate(R.layout.fragment_favorite, container, false);

        onCreateView();

        mRecyclerView = ret.findViewById(R.id.favorite_recyclerView);
        mAdapter = new CustomAdapter(this);

        loadFavorite();

        refreshLayout = ret.findViewById(R.id.favorite_refreshLayout);
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

        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.setBarTitle("我的收藏");

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