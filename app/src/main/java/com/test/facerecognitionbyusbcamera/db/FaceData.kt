package com.test.facerecognitionbyusbcamera.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dataFace", indices = [Index(
        value = ["faceToken", "name"],
        unique = true
    )]
)
class FaceData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "faceToken")
    var faceToken: String
)