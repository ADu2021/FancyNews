package com.java.duyiyang.database;

// from https://github.com/googlecodelabs/android-room-with-a-view/blob/master/app/src/main/java/com/example/android/roomwordssample/WordRoomDatabase.java

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the backend. The database. This used to be done by the OpenHelper.
 * The fact that this has very few comments emphasizes its coolness.  In a real
 * app, consider exporting the schema to help you with migrations.
 */
@Database(entities = {Article.class}, version = 1, exportSchema = true)
@TypeConverters({Converters.class})
public abstract class ArticleDatabase extends RoomDatabase {
    public abstract ArticleDao articleDao();

    private static volatile ArticleDatabase INSTANCE = null;
    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static ArticleDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {

            synchronized (ArticleDatabase.class) {
                if (INSTANCE == null) {
                    Log.d("database","get");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ArticleDatabase.class, "word_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d("database","create");
            databaseWriteExecutor.execute(() -> {

            });
        }
    };

}
