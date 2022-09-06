package com.java.duyiyang.database;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;

public class FileController {

    static private String suffix = ".jpeg";
    static private Activity activity;

    public static void setActivity(Activity activity) {
        // todo : call this
        FileController.activity = activity;
    }
/*
    private static void PicassoLoadUrl(String url, ImageView imageView, Context context) {

    }
    private static void PicassoLoadFile(File file, ImageView imageView, Context context) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.get().load(file)
                        .resize(0, 800)
                        .resize(1000,0)
                        .onlyScaleDown()
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                // pass
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("FileController", "fetch local failed, trying url"+e.getMessage());
                                try {
                                    Picasso.get().load(url)
                                            .resize(0, 800)
                                            .resize(1000, 0)
                                            .onlyScaleDown()
                                            .into(imageView, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    saveFile(url, filename, context);
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    // pass
                                                }
                                            });
                                } catch (Exception ex) {
                                    Log.e("FileController", "load url failed " + ex.getMessage());
                                }
                            }
                        });
            }
        });
    }
*/
    /**
     * Save image file from internet to local internal memory (as JPEG).
     * @param url resource of internet image
     * @param filename destination of storing image, often named as [article.idx]+[image.idx]
     *                 eg. 202109040119a528fcf48bfc4bc79108d2dc4118d2240
     *                 means article 202109040119a528fcf48bfc4bc79108d2dc4118d224's first(0) image.
     *                 no need for suffix like '.jpeg'.
     * @param context context to access resources
     */
    public static void saveFile(String url, String filename, @NonNull Context context) {

                try {
                    File file = new File(context.getFilesDir(), filename + suffix);
                    Picasso.get().load(url)
                            .resize(0, 800)
                            .resize(1000, 0)
                            .onlyScaleDown()
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    try (FileOutputStream fos = new FileOutputStream(file)) {
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                        fos.flush();
                                        fos.close();
                                        Log.d("onBitmapLoaded","finished");
                                    } catch (Exception e) {
                                        Log.e("FileController", "save file failed " + e.getMessage());
                                    }
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    Log.e("FileController", "save file failed " + e.getMessage());
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                } catch (Exception e) {
                    Log.e("FileController", "save file failed " + e.getMessage());
                }
        //    }
        //}).start();
    }

    /**
     * Load file from local file or internet url.
     * Load local file first, if not available(null string or ""), then load url.
     * If load from internet, try to save to local if possible.
     * @param filename local file name, no .jpg requirement
     * @param url internet resource's url
     * @param imageView target ImageView
     * @param context context to access resources
     */
    public static void loadFile(String filename, String url, @NonNull ImageView imageView, @NonNull Context context) {
        /*
        new Thread(new Runnable() {
            @Override
            public void run() { jp

         */
                if(filename != null && !filename.equals("")) {
                    try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                        File file = new File(context.getFilesDir(), filename+suffix);
                        Log.d("FileController","1");
                        Picasso.get().load(file)
                                .resize(0, 800)
                                .resize(1000,0)
                                .onlyScaleDown()
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        // pass
                                        Log.i("FileController","load local success"+filename);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("FileController", "fetch local failed, trying url"+e.getMessage());
                                        try {
                                            Log.d("onError","URL="+url);
                                            Picasso.get().load(url)
                                                    .resize(0, 800)
                                                    .resize(1000, 0)
                                                    .onlyScaleDown()
                                                    .into(imageView, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                            Log.d("onSuccess","called");
                                                            saveFile(url, filename, context);
                                                        }

                                                        @Override
                                                        public void onError(Exception e) {
                                                            // pass
                                                            Log.d("onError",e.getMessage());
                                                        }
                                                    });
                                        } catch (Exception ex) {
                                            Log.e("FileController", "load url failed " + ex.getMessage());
                                        }
                                    }
                                });

                    } catch (Exception e) {
                        Log.e("FileController", "load local file failed1 " + e.getMessage());
                    }
                } else {
                    try {
                        Picasso.get().load(url)
                                .resize(0, 800)
                                .resize(1000, 0)
                                .onlyScaleDown()
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        saveFile(url, filename, context);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // pass
                                    }
                                });
                    } catch (Exception e) {
                        Log.e("FileController", "load url failed " + e.getMessage());
                    }
                }
        //    }
        //}).start();

    }
    public static void deleteImage(String filename, Context context) {
        try{
            File file = new File(context.getFilesDir(), filename+suffix);
            file.delete();
        } catch (Exception e) {
            Log.e("FileController","deleteImage"+e.getMessage());
        }
    }
}
