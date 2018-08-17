package com.talmir.mickinet.helpers.room.sent;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.talmir.mickinet.helpers.room.db.AppDatabase;

import java.util.List;

/**
 * @author miri
 * @since 7/29/2018
 */
public class SentFilesRepository {
    private static SentFilesDao mSentFilesDao;
    private LiveData<List<SentFilesEntity>> mAllSentFiles;

    SentFilesRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mSentFilesDao = db.mSentFilesDao();
        mAllSentFiles = mSentFilesDao.getAllSentFiles();
    }

    LiveData<List<SentFilesEntity>> getAllSentFiles() { return mAllSentFiles; }

    public void insert(SentFilesEntity sentFilesEntity) {
        // AsyncTask.THREAD_POOL_EXECUTOR is important!
        new SentFilesRepository.insertAsyncTask(mSentFilesDao).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sentFilesEntity);
    }

    private static class insertAsyncTask extends AsyncTask<SentFilesEntity, Void, Void> {
        private SentFilesDao mAsyncTaskDao;

        insertAsyncTask(SentFilesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SentFilesEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e("doInBackground: ", "reached here!");
        }
    }
}
