package com.test.facerecognitionbyusbcamera.db

import androidx.room.*

@Dao
interface FaceDataDao {

    @Query("DELETE FROM dataFace")
    fun delAllData()

    @Query("SELECT * FROM dataFace order by id asc")
    fun selectAllData(): MutableList<FaceData>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(vararg fingerData: FaceData)

    @Update
    fun updateData(fingerData: FaceData)

    @Delete
    fun deleteData(fingerData: FaceData)

    @Query("SELECT * FROM dataFace WHERE name =:m")
    fun selectDataByName(m: String): List<FaceData>

    @Query("SELECT * FROM dataFace WHERE faceToken =:m")
    fun selectFaceDataByFaceToken(m: String): List<FaceData>

    @Query("SELECT name FROM dataFace WHERE faceToken =:m")
    fun selectNameByFaceToken(m: String): List<String>

}