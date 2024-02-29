package com.zaz060.demo.db

import androidx.room.*

@Dao
interface FingerDataDao {

    @Query("DELETE FROM data")
    fun delAllData()

    @Query("SELECT * FROM data order by id asc")
    fun selectAllData(): MutableList<FingerData>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(vararg fingerData: FingerData)

    @Update
    fun updateData(fingerData: FingerData)

    @Delete
    fun deleteData(fingerData: FingerData)

    @Query("SELECT * FROM data WHERE name =:m")
    fun selectDataByName(m: String): List<FingerData>

    @Query("SELECT name FROM data WHERE fingerId =:m")
    fun selectNameById(m: Int): List<String>

}