package com.dadomatch.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dadomatch.shared.data.local.entity.SuccessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SuccessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuccess(success: SuccessEntity)
    
    @Query("SELECT * FROM success_records ORDER BY dateMillis DESC")
    fun getAllSuccesses(): Flow<List<SuccessEntity>>
}
