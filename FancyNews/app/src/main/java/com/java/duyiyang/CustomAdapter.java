package com.java.duyiyang;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.java.duyiyang.database.Article;
import com.java.duyiyang.database.ArticleDao;
import com.squareup.picasso.Picasso;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;

/**
 * To show a bunch of articles.
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private static final String TAG = "CustomAdapter";

    private Context context;

    private FragmentWithArticles parent;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final ImageView image;
        private final TextView no_image_text;
        private FragmentWithArticles parent;
        private int index;
        private boolean have_image = false;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int Aid = getAbsoluteAdapterPosition();
                    Log.d(TAG, "Element " + Aid + " clicked.");
                    // TODO : (optional) use navigation :
                    // ContentAction action = SlideshowFragmentDirections.contentAction();


                    // this is the trivial(not-that-safe) approach...
                    Bundle bundle = new Bundle();
                    if(parent != null && parent.datasetArticle != null && parent.datasetArticle.size() > index) {
                        parent.datasetArticle.get(index).packBundle(bundle);
                        bundle.putBoolean("forceSave", parent.forceSaveAtStart);
                        bundle.putBoolean("rss", parent.rss);
                        Navigation.findNavController(v).navigate(R.id.articleFragment, bundle);
                    }
                }
            });
            title = (TextView) v.findViewById(R.id.article_bar_title);
            image = (ImageView) v.findViewById(R.id.article_bar_image);
            no_image_text = (TextView) v.findViewById(R.id.article_bar_no_image_text);
        }

        public TextView Title() {
            return title;
        }
        public ImageView Image() { return image; }
        public TextView NoImageText() { return no_image_text; }
        public void setImageRes(@NonNull String source, @NonNull Context context) {
            //image.setImageResource(res_id);
            try {
                Log.d("setImageRes",source);
                Picasso.get().load(source)
                        .resize(0, 800)
                        .resize(1000,0)
                        .onlyScaleDown()
                        .into(image);
                // Glide.with(context).load(source).into(image);
                have_image = true;
                NoImageText().setText("");
            } catch (Exception e) {
                Log.e(TAG, "setImageRes " + e.getMessage());
            }
        }
        public void setIndex(int x) { index = x; }
        public void setParent(FragmentWithArticles parent) {this.parent = parent;}
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * param dataSet String[] containing the data to populate views to be used by RecyclerView.
     * assert : ..Title.length == ..Content.length = _num
     */
    public CustomAdapter(FragmentWithArticles parent) { this.parent = parent; }
    public void setCustomAdapter() {
        num = parent.datasetArticle.size();
        notifyDataSetChanged();
        Log.d("loadHistory",num+"");
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.article_bar, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    void setColor(ViewHolder viewHolder, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Single<Article> task = parent.database.selectById(getArticle(position).idx);
                task.subscribe(new DisposableSingleObserver<Article>() {
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Article article) {
                        viewHolder.Title().setTextColor(parent.getResources().getColor(R.color.gray));
                        viewHolder.NoImageText().setTextColor(parent.getResources().getColor(R.color.gray));
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.e("onError",e.getMessage());
                        viewHolder.Title().setTextColor(parent.getResources().getColor(R.color.black));
                        viewHolder.NoImageText().setTextColor(parent.getResources().getColor(R.color.black));
                    }
                });
            }
        }).start();

    }
    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final int pos = viewHolder.getAbsoluteAdapterPosition(); // actually pos==position
        Log.d(TAG, "Element " + position + " set." + pos);

        if(pos >= parent.datasetArticle.size())
            return;

        setColor(viewHolder, position);

        viewHolder.setParent(parent);
        viewHolder.setIndex(pos);

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.Title().setText(getArticle(pos).title);

        viewHolder.Image().setImageDrawable (null);
        if(getArticle(pos).image.size() == 0 || getArticle(pos).image.get(0).equals("-1")) { // no image
            viewHolder.NoImageText().setText("点进来看看吧~");
        } else if(getArticle(pos).image.size() > 0){ // have image
            viewHolder.setImageRes(getArticle(pos).image.get(0), context);
        }

        //setAnimation(viewHolder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        viewToAnimate.startAnimation(animation);
    }

    // END_INCLUDE(recyclerViewOnBindViewHolder)

    public void hide() {
        Log.d("hide","called");
        num = 0;
        notifyDataSetChanged();
    }
    // Return the size of your dataset (invoked by the layout manager)
    int num = 0;
    @Override
    public int getItemCount() {
        return num;
    }

    private Article getArticle(int index) {
        if(parent != null && parent.datasetArticle != null && parent.datasetArticle.size() > index)
            return parent.datasetArticle.get(index);
        return null;
    }
}
