package com.talmir.mickinet.helpers.room.received;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

/**
 * @author miri
 * @since 7/15/2018
 *
 * <p>Why use a Repository?</p>
 * A Repository manages query threads and
 * allows you to use multiple backends. In
 * the most common example, the Repository
 * implements the logic for deciding whether
 * to fetch data from a network or use
 * results cached in a local database.
 */
public class ReceivedRepository {

    private ReceivedDao rDao;
    private List<Received> allReceiveds;

    ReceivedRepository(Application application) {
        ReceivedRoomDatabase db = ReceivedRoomDatabase.getDatabase(application);
        rDao = db.receivedDao();
        allReceiveds = rDao.getAllReceiveds();
    }

    List<Received> getAllReceiveds() {
        return allReceiveds;
    }

    public void insert(Received received) {
        new insertAsyncTask(rDao).execute(received);
    }

    private static class insertAsyncTask extends AsyncTask<Received, Void, Void> {
        private ReceivedDao mAsyncTaskDao;

        insertAsyncTask(ReceivedDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Received... params) {
            mAsyncTaskDao.insertRecord(params[0]);
            return null;
        }
    }
}
