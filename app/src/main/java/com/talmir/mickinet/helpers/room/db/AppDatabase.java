package com.talmir.mickinet.helpers.room.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import com.talmir.mickinet.helpers.room.received.ReceivedFilesDao;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesEntity;
import com.talmir.mickinet.helpers.room.sent.SentFilesDao;
import com.talmir.mickinet.helpers.room.sent.SentFilesEntity;

/**
 * This is the backend. The database. This used to be done by the OpenHelper.
 * The fact that this has very few comments emphasizes its coolness.
 */
@Database(entities = {SentFilesEntity.class, ReceivedFilesEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ReceivedFilesDao mReceivedFilesDao();
    public abstract SentFilesDao mSentFilesDao();

    private static AppDatabase DATABASE_INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (DATABASE_INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (DATABASE_INSTANCE == null) {
                    DATABASE_INSTANCE = Room
                            .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "statistics")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return DATABASE_INSTANCE;
    }

    // Override the onOpen method to populate the database.
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };

    public static void destroyInstance() {
        DATABASE_INSTANCE = null;
    }
}
