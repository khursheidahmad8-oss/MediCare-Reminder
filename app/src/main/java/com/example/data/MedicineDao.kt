package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY time ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Int): Medicine?

    @Query("SELECT * FROM medicines WHERE id = :id")
    fun getMedicineByIdFlow(id: Int): Flow<Medicine?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: Int)

    @Query("SELECT * FROM medicine_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<MedicineHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: MedicineHistory)

    @Delete
    suspend fun deleteHistory(history: MedicineHistory)

    @Query("DELETE FROM medicines")
    suspend fun clearAllMedicines()

    @Query("DELETE FROM medicine_history")
    suspend fun clearAllHistory()
}
