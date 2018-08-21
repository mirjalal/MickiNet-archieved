package com.talmir.mickinet.helpers.room.sent;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.talmir.mickinet.helpers.room.utils.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
        new insertAsyncTask(mSentFilesDao).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sentFilesEntity);
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
    }

    public boolean deleteAllRecords() {
        AsyncTask<Void, Void, Boolean> deleteTask = new deleteAllRecordsAsyncTask(mSentFilesDao).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        try {
            return deleteTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class deleteAllRecordsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private SentFilesDao dao;

        deleteAllRecordsAsyncTask(SentFilesDao dao) {
            this.dao = dao;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                dao.deleteAllRecords();
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }
}
