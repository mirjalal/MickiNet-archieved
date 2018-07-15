package com.talmir.mickinet.helpers.room.received;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

/**
 * @author miri
 * @since 7/15/2018
 */
@Database(entities = {Received.class}, version = 1, exportSchema = false)
public abstract class ReceivedRoomDatabase extends RoomDatabase {
    private static ReceivedRoomDatabase INSTANCE;

    public abstract ReceivedDao receivedDao();

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){
                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    static ReceivedRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ReceivedRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, ReceivedRoomDatabase.class, "received")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final ReceivedDao mDao;

        PopulateDbAsync(ReceivedRoomDatabase db) {
            mDao = db.receivedDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDao.deleteAllReceiveds();
            Received word = new Received();
            word.fileName = "ilk test";
            word.dateTime = "15 iyul 2018";
            word.fileType = 1;
            word.operationStatus = 0;
            mDao.insertRecord(word);
            return null;
        }
    }
}
