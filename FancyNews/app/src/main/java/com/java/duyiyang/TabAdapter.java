package com.java.duyiyang;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;


public class TabAdapter extends RecyclerView.Adapter<TabAdapter.ViewHolder> {
    private static final String TAG = "TabAdapter";
    private Context context;
    Resources res;
    String[] categories;
    boolean[] tab_show;

    private TabLayout tabLayout;



    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final GridLayout gridShow;
        private final GridLayout gridHide;
        public final TextView tab_setting_text_show;
        public final TextView tab_setting_text_hide;
        public ViewHolder(View v) {
            super(v);

            gridShow = (GridLayout) v.findViewById(R.id.gridShow);
            gridHide = (GridLayout) v.findViewById(R.id.gridHide);
            tab_setting_text_show = v.findViewById(R.id.tab_setting_text_show);
            tab_setting_text_hide = v.findViewById(R.id.tab_setting_text_hide);
        }
        public GridLayout getGridShow() { return gridShow; }
        public GridLayout getGridHide() { return gridHide; }

    }

    public TabAdapter() {}
    public void setTabAdapter(TabLayout tabLayout, Context context, boolean[] tab_show) {
        this.tabLayout = tabLayout;
        this.context = context;
        res = context.getResources();
        this.tab_show = tab_show;
        categories = res.getStringArray(R.array.categories_array);
    }
    public void show() {
        if(cnt == 0) {
            cnt = 1;
            Log.d(TAG, "show" + String.valueOf(getItemCount()));
            notifyDataSetChanged();
        }
    }
    public void hide() {
        if(cnt == 1) {
            cnt = 0;
            Log.d(TAG, "hide" + String.valueOf(getItemCount()));
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.tab_setting, viewGroup, false);

        return new ViewHolder(v);
    }

    private boolean haveTab(String name) {
        int len = tabLayout.getTabCount();
        for(int i = 3; i < len; i++)
            if(tabLayout.getTabAt(i).getText().equals(name))
                return true;
        return false;
    }

    private MaterialButton newShowButton(ViewHolder viewHolder, Context context, int i) {
        MaterialButton button = new MaterialButton(context);
        button.setGravity(Gravity.CENTER);
        button.setText(categories[i]);
        button.setTypeface(context.getResources().getFont(R.font.zcool_xiaowei));
        button.setOnClickListener(new View.OnClickListener() {
            MaterialButton self = button;
            @Override
            public void onClick(View view) {
                // hide tab
                int id = i;
                try {
                    TabLayout.Tab tab = null;
                    for (int i = 3, siz = tabLayout.getTabCount(); i < siz; i++)
                        if (tabLayout.getTabAt(i).getText().equals(categories[id])) {
                            tab = tabLayout.getTabAt(i);
                        }
                    if (tab != null)
                        tabLayout.removeTab(tab);
                    else
                        Log.d(TAG, "cannot found remove tab:" + String.valueOf(i));
                    MaterialButton btn;
                    tab_show[i] = false;
                    viewHolder.getGridHide().addView(btn = newHideButton(viewHolder, context, i));
                    setInAnimation(btn);
                    setOutAnimation(self);
                    viewHolder.getGridShow().removeView(self);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        return button;
    }
    private MaterialButton newHideButton(ViewHolder viewHolder, Context context, int i) {
        MaterialButton button = new MaterialButton(context, null, R.attr.materialButtonOutlinedStyle );
        button.setGravity(Gravity.CENTER);
        button.setText(categories[i]);
        button.setTypeface(context.getResources().getFont(R.font.zcool_xiaowei));
        button.setCornerRadius(75);
        int id = i;
        button.setOnClickListener(new View.OnClickListener() {
            MaterialButton self = button;
            @Override
            public void onClick(View view) {
                // show tab

                TabLayout.Tab tab = tabLayout.newTab();
                tab.setText(categories[id]);
                tabLayout.addTab(tab);
                MaterialButton btn;
                tab_show[i] = true;
                viewHolder.getGridShow().addView(btn = newShowButton(viewHolder, context, i));
                setInAnimation(btn);
                setOutAnimation(self);
                viewHolder.getGridHide().removeView(self);
            }
        });
        return button;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");
        //int tab_cnt = viewHolder.getGridShow().getChildCount() + viewHolder.getGridHide().getChildCount();
        //if(tab_cnt > 0)
            //return;
        if(cnt == 0) {
            viewHolder.getGridShow().removeAllViews();
            viewHolder.getGridHide().removeAllViews();
            viewHolder.tab_setting_text_show.setText("");
            viewHolder.tab_setting_text_hide.setText("上拉加载更多");
            return;
        }
        viewHolder.tab_setting_text_show.setText("显示");
        viewHolder.tab_setting_text_hide.setText("隐藏");
        if(viewHolder.getGridHide().getChildCount() == 0) {
            for (int i = 0; i < 10; i++)
                if (haveTab(categories[i])) {
                    viewHolder.getGridShow().addView(newShowButton(viewHolder, context, i));
                } else {
                    viewHolder.getGridHide().addView(newHideButton(viewHolder, context, i));
                }
        }
    }


    private void setInAnimation(View viewToAnimate) {
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        viewToAnimate.startAnimation(animation);
    }
    private void setOutAnimation(View viewToAnimate) {
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
        viewToAnimate.startAnimation(animation);
    }

    int cnt = 0;
    @Override
    public int getItemCount() { return 1; } // 这看上去有点sb...
}
