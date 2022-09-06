package com.java.duyiyang.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ArticleDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    Completable insert(Article article);

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    Completable weakInsert(Article article);

    @Delete
    Completable delete(Article article);

    @Query ("SELECT * FROM article_table WHERE idx =:idx")
    Single<Article> selectById(String idx);

    @Query ("SELECT * FROM article_table WHERE idx =  '123'")
    Single<Article> selectTest();

    @Query ("SELECT * FROM article_table ORDER BY rowid DESC")
    Single<Article[]> selectAll();

    @Query ("SELECT * FROM article_table WHERE marked = 1 ORDER BY rowid DESC")
    Single<Article[]> selectAllFavorite();

    @Query ("SELECT * FROM article_table LIMIT (:x)")
    Single<Article[]> selectTop(int x);

    @Query ("SELECT COUNT(*) FROM article_table")
    Single<Integer> getCount();

    @Query ("DELETE FROM article_table")
    Completable clear();
}
