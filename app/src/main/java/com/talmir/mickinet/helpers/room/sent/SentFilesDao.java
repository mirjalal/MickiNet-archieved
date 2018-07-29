package com.talmir.mickinet.helpers.room.sent;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * @author miri
 * @since 7/29/2018
 */
@Dao
public interface SentFilesDao {
    @Query("SELECT id, name, type, status, dateTime FROM sent")
    LiveData<List<SentFilesEntity>> getAllSentFiles();

    // We do not need a conflict strategy, because the id is our primary key, and you cannot
    // add two items with the same primary key to the database.
    @Insert
    void insert(SentFilesEntity file);

    @Query("DELETE FROM received")
    void deleteAllRecords();
}
