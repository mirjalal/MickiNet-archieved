package com.talmir.mickinet.helpers.room.received;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * A basic class representing an entity that
 * is a row in a five-columns database table
 * to store date about received files.
 *
 * See the documentation for the full rich set of annotations.
 * https://developer.android.com/topic/libraries/architecture/room.html
 *
 * @author miri
 * @since 7/26/2018
 */
@Entity(tableName = "received")
public class ReceivedFilesEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String f_name;

    @ColumnInfo(name = "type")
    public String f_type;

    @ColumnInfo(name = "status")
    public String f_operation_status;

    @ColumnInfo(name = "dateTime")
    public String f_time;
}
