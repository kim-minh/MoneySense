package com.kimminh.moneysense.ui.history

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(historyEntity: HistoryEntity)

    @Query("DELETE FROM history_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM history_table ")
    fun getAllHistory(): LiveData<List<HistoryEntity>>


}
