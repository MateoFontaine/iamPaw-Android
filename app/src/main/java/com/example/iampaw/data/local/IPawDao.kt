package com.example.iampaw.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IPawDao {

    @Query("SELECT * FROM pet_reports ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PetReportLocal>>

    @Query(
        """
        SELECT * FROM pet_reports
        WHERE name LIKE '%' || :query || '%'
           OR breed LIKE '%' || :query || '%'
           OR location LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun search(query: String): Flow<List<PetReportLocal>>

    @Query("SELECT COUNT(*) FROM pet_reports")
    suspend fun count(): Int

    @Query("SELECT * FROM pet_reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PetReportLocal?

    @Query("SELECT id FROM pet_reports WHERE userId != ''")
    suspend fun getSyncedReportIds(): List<String>

    @Query("DELETE FROM pet_reports WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<PetReportLocal>)
}
