package com.java.duyiyang;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.java.duyiyang.database.Article;
import com.java.duyiyang.database.FileController;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.setBarTitle("Settings");

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        EditTextPreference textPreference = findPreference("preference_rss_source");
        textPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String value = (String) newValue;
                textPreference.setSummary(value);
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("preference_rss_source", value);
                editor.apply();
                return true;
            }
        });
        textPreference.setSummary(getActivity().getPreferences(Context.MODE_PRIVATE)
                .getString("preference_rss_source", "https://www.zhihu.com/rss"));

        DropDownPreference selectFeed = findPreference("preference_rss_select_source");
        selectFeed.setEntries(getResources().getStringArray(R.array.select_rss_feed_name));
        selectFeed.setEntryValues(getResources().getStringArray(R.array.select_rss_feed));
        selectFeed.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d("onPreferenceChange",)
                String val = (String) newValue;
                textPreference.setSummary(val);
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("preference_rss_source", val);
                editor.apply();
                return true;
            }
        });

        Preference buttonClearImage = findPreference("preference_clear_images");
        buttonClearImage.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                ProgressDialog dialog = ProgressDialog.show(getContext(), "",
                        "Loading. Please wait...", true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Single<Article[]> task = ((MainActivity)getActivity()).mArticleDao.selectAll();
                        task.subscribe(new DisposableSingleObserver<Article[]>() {
                            @Override
                            public void onSuccess(Article @NonNull [] articles) {
                                for(int i = 0; i < articles.length; i++) {
                                    for(int j = 0; j < articles[i].image.size(); j++)
                                        FileController.deleteImage(articles[i].image.get(j),getContext());
                                }
                                dialog.dismiss();
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                if(e.getMessage() != null) Log.e("Clear all images", e.getMessage());
                                dialog.dismiss();
                            }
                        });

                    }
                }).start();

                return true;
            }
        });

        Preference buttonClearArticles = findPreference("preference_clear_all");
        buttonClearArticles.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                ProgressDialog dialog = ProgressDialog.show(getContext(), "",
                        "Loading. Please wait...", true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Single<Article[]> task = ((MainActivity)getActivity()).mArticleDao.selectAll();
                        task.subscribe(new DisposableSingleObserver<Article[]>() {
                            @Override
                            public void onSuccess(Article @NonNull [] articles) {
                                for(int i = 0; i < articles.length; i++) {
                                    for(int j = 0; j < articles[i].image.size(); j++)
                                        FileController.deleteImage(articles[i].image.get(j),getContext());
                                }
                                Completable del = ((MainActivity)getActivity()).mArticleDao.clear();
                                del.subscribe(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {
                                        dialog.dismiss();
                                    }
                                });
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                if(e.getMessage() != null) Log.e("Clear all images", e.getMessage());
                                dialog.dismiss();
                            }
                        });


                    }
                }).start();

                return true;
            }
        });
    }
}