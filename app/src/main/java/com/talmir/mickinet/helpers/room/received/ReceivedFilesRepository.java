package com.talmir.mickinet.helpers.room.received;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.talmir.mickinet.helpers.room.db.AppDatabase;

import java.util.List;

/**
 * @author miri
 * @since 7/26/2018
 */
public class ReceivedFilesRepository {
    private ReceivedFilesDao mReceivedFilesDao;
    private LiveData<List<ReceivedFilesEntity>> mAllReceivedFiles;

    ReceivedFilesRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mReceivedFilesDao = db.mReceivedFilesDao();
        mAllReceivedFiles = mReceivedFilesDao.getAllReceivedFiles();
    }

    LiveData<List<ReceivedFilesEntity>> getAllReceivedFiles() { return mAllReceivedFiles; }

    public void insert(ReceivedFilesEntity entity) {
        new insertAsyncTask(mReceivedFilesDao).execute(entity);
    }

    private static class insertAsyncTask extends AsyncTask<ReceivedFilesEntity, Void, Void> {
        private ReceivedFilesDao mAsyncTaskDao;

        insertAsyncTask(ReceivedFilesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ReceivedFilesEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
