package com.java.duyiyang;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.java.duyiyang.database.Article;
import com.java.duyiyang.ui.home.HomeFragment;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.lang.reflect.Method;
import java.util.ArrayList;

// result (of search)
public class ListFragment extends FragmentWithArticles {

    private TextView infoText;

    public ListFragment() {
        // Required empty public constructor
    }

    protected void afterParseJson() {
        if(datasetArticle.size() == 0) {
            infoText.setText(R.string.no_result);
        } else {
            infoText.setText("找到"+datasetArticle.size()+"条结果，上拉查找更多");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAdapter = new CustomAdapter(this);

        onCreateView();

        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        api = new ApiAdapter(bundle);
        mRecyclerView = view.findViewById(R.id.result_recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.setBarTitle("搜索结果");

        refreshLayout = view.findViewById(R.id.result_refreshLayout);
        setRefreshLayout();
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.setEnableLoadMore(false);
                ListFragment.super.datasetArticle = new ArrayList<>();
                api.setPage(1);
                refresh();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.setEnableRefresh(false);
                api.addPage();
                refresh();
            }
        });
        refreshLayout.autoRefresh();

        infoText = view.findViewById(R.id.list_infoText);
    }

    private void refresh() {
        Log.d("refresh","s");
        try {
            Method callback = this.getClass().getMethod("parseJson", String.class);
            api.getContent(this, callback);
        } catch (Exception e) {
            Log.d("Test Home onVC", e.getMessage());
        }
    }
}