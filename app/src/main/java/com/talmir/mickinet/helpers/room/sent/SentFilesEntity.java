package com.talmir.mickinet.helpers.room.sent;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * A basic class representing an entity that
 * is a row in a five-columns database table
 * to store date about sent files.
 *
 * See the documentation for the full rich set of annotations.
 * https://developer.android.com/topic/libraries/architecture/room.html
 *
 * @author miri
 * @since 7/29/2018
 */
@Entity(tableName = "sent")
public class SentFilesEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int s_f_id;

    @ColumnInfo(name = "name")
    public String s_f_name;

    @ColumnInfo(name = "type")
    public String s_f_type;

    @ColumnInfo(name = "status")
    public String s_f_operation_status;

    @ColumnInfo(name = "dateTime")
    public String s_f_time;
}