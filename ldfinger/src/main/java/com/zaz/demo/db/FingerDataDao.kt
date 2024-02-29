package com.zaz.demo.db

import androidx.room.*
import com.zaz.demo.db.FingerData

@Dao
interface FingerDataDao {

    @Query("DELETE FROM data8800")
    fun delAllData()

    @Query("SELECT * FROM data8800 order by id asc")
    fun selectAllData(): MutableList<FingerData>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(vararg fingerData: FingerData)

    @Update
    fun updateData(fingerData: FingerData)

    @Delete
    fun deleteData(fingerData: FingerData)

    @Query("SELECT * FROM data8800 WHERE name =:m")
    fun selectDataByName(m: String): List<FingerData>

    @Query("SELECT name FROM data8800 WHERE fingerId =:m")
    fun selectNameById(m: Int): List<String>

}