package com.zaz.demo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "data8800", indices = [Index(
        value = ["name"],
        unique = true
    )]
)
class FingerData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "fingerId")
    var fingerId: Int
)