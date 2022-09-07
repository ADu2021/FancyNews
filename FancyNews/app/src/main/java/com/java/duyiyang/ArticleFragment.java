package com.java.duyiyang;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.TransitionInflater;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.java.duyiyang.database.Article;
import com.java.duyiyang.database.FileController;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;

public class ArticleFragment extends Fragment {
    private static String TAG = "ArticleFragment";
    static final String img_helper = "<style>img{display: inline; height: auto; max-width: 80%;}</style>";

    private LinearLayout layout;
    private TextView titleText;
    private TextView publishText;
    private ImageView coverImage;
    private TextView contentText;
    private WebView webView;
    private FloatingActionButton markButton;
    private VideoView videoView;

    Article article = null;

    boolean rss = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_article, container, false);

        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_right));

        return ret;
    }

    boolean forceSaveAtStart = false;
    void setForceSaveAtStart(boolean force) {forceSaveAtStart = force;}

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        // todo: get arguments use try-catch
        try {
            article = new Article(bundle);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        if(article == null) return;
        try {
            forceSaveAtStart = bundle.getBoolean("forceSave");
            rss = bundle.getBoolean("rss");
        } catch (Exception e) {}

        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.setBarTitle(article.category);


        layout = view.findViewById(R.id.article_linearLayout);
        titleText = view.findViewById(R.id.article_title);
        publishText = view.findViewById(R.id.article_publish);
        coverImage = view.findViewById(R.id.article_cover_image);
        contentText = view.findViewById(R.id.article_content);
        webView = view.findViewById(R.id.article_webView);
        videoView = view.findViewById(R.id.article_videoView);


        titleText.setText(article.title);
        if(rss)
            publishText.setText(new StringBuilder().append("RSS源（可在右上角设置）\n发表于 ")
                    .append(article.pubDate).toString());
        else
            publishText.setText(new StringBuilder().append(article.publisher).append(" 发表于 ")
                .append(article.pubDate).toString());

        if(rss) {
            webView.loadData(img_helper+article.content, "text/html; charset=utf-8", "UTF-8");
        } else {
            int img_cnt = article.image.size();
            if(img_cnt > 0)
                FileController.loadFile(article.idx+"0",article.image.get(0),coverImage,getContext());
            contentText.setText(article.content);
            for(int i = 1; i < img_cnt; i++) { // other images
                Log.d("load image number", i+article.image.get(i));
                ImageView iv = new ImageView(getContext());
                layout.addView(iv);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0,20,0,20); //(left, top, right, bottom);
                lp.gravity = Gravity.CENTER;
                iv.setLayoutParams(lp);
                FileController.loadFile(article.idx+String.valueOf(i),
                        article.image.get(i),iv,getContext());
            }
            try { // video
                if(article.video.size() > 0 && article.video.get(0).length() > 5){
                    videoView.setMediaController(new MediaController(getContext()));
                    videoView.setVideoURI(Uri.parse(article.video.get(0)));
                    //videoView.setVideoURI(Uri.parse(" https://flv3.people.com.cn/dev1/mvideo/vodfiles/2021/09/07/2eb63e58f8c0b6cee1c2c1dde92940ef_c.mp4"));
                    Log.d("VideoView", article.video.get(0));
                    videoView.requestFocus();
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            videoView.start();
                        }
                    });

                } else {
                    layout.removeView(videoView);
                }
                //webView.loadUrl(article.video.get(0));
            } catch (Exception e) {
                Log.d("webView","loadUrl"+e.getMessage());
            }
        }


        markButton = view.findViewById(R.id.mark_article);
        if(rss)
            markButton.hide();
        markButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(view instanceof FloatingActionButton))
                    return;
                if(article.marked) {
                    article.marked = false;
                    markButton.setBackgroundTintList(
                            ContextCompat.getColorStateList(getContext(), R.color.purple_200));
                    //markButton.setBackgroundResource(R.drawable.empty_star);
                    Snackbar.make(view, "已取消收藏", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    article.marked = true;
                    markButton.setBackgroundTintList(
                            ContextCompat.getColorStateList(getContext(), R.color.gold));
                    //markButton.setBackgroundResource(R.drawable.solid_star); this didn't work..
                    Snackbar.make(view, "已收藏", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        checkMarkStatus();
        storeArticle(forceSaveAtStart);
    }
    private void checkMarkStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Single<Article> task = ((MainActivity)getActivity()).mArticleDao.selectById(article.idx);
                task.subscribe(new DisposableSingleObserver<Article>() {
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Article article_get) {
                        if(article_get.marked) {
                            markButton.setBackgroundTintList(
                                    ContextCompat.getColorStateList(getContext(), R.color.gold));
                            article.marked = true;
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        // pass
                    }
                });
            }
        }).start();
    }

    private void storeArticle(boolean force) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("storeArticle","1"+force);
                // todo retrieve image from the internet and store in local memory
                try{
                    Completable task = force ?
                            ((MainActivity)getActivity()).mArticleDao.insert(article) :
                            ((MainActivity)getActivity()).mArticleDao.weakInsert(article);
                    task.subscribe(new CompletableObserver() { // this is all debug, delete later
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                            //pass
                        }

                        @Override
                        public void onComplete() {
                            Single<Article> got = ((MainActivity)getActivity()).mArticleDao
                                    .selectById("202209010033300ad3879eb149e0b7b35b7cc849d4cd");
                            got.subscribe(new DisposableSingleObserver<Article>() {
                                @Override
                                public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Article article) {
                                    Log.d("successD",article.idx);
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Log.d("errorD", e.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                            Log.d("storeArticle",e.getMessage());
                            if(article.idx != null) Log.d("storeArticle",article.idx);
                            else Log.e("storeArticle","null idx"+article.title);
                        }
                    });
                } catch (Exception e) {
                    Log.e("storeArticle",e.getMessage());
                }
            }
        }).start();
    }

    @MainThread
    @CallSuper
    public void onPause() {
        super.onPause();
        storeArticle(true);
    }
}