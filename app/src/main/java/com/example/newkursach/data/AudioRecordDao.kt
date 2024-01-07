package com.example.newkursach.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AudioRecordDao {
    @Query("SELECT*FROM audioRecords")
    fun getAll(): LiveData<List<AudioRecord>>

    @Insert
    fun insert(vararg audioRecord: AudioRecord)

    @Delete
    fun delete(audioRecord: AudioRecord)

    @Delete
    fun delete(audioRecords: List<AudioRecord>)

    @Update
    fun update(audioRecord: AudioRecord)

    @Query("DELETE FROM audioRecords")
    suspend fun deleteAll()

}