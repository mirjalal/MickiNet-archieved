package com.talmir.mickinet.helpers.room.received;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.talmir.mickinet.helpers.room.utils.DateConverter;

import java.util.Date;

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
    @ColumnInfo(name = "id")
    public int r_f_id;

    @ColumnInfo(name = "name")
    public String r_f_name;

    @ColumnInfo(name = "type")
    public String r_f_type;

    @ColumnInfo(name = "status")
    public String r_f_operation_status;

    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "dateTime")
    public Date r_f_time;
}
