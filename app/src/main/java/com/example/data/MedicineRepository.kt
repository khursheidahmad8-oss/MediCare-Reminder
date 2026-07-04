package com.example.data

import kotlinx.coroutines.flow.Flow

class MedicineRepository(private val medicineDao: MedicineDao) {

    val allMedicines: Flow<List<Medicine>> = medicineDao.getAllMedicines()
    val allHistory: Flow<List<MedicineHistory>> = medicineDao.getAllHistory()

    suspend fun getMedicineById(id: Int): Medicine? {
        return medicineDao.getMedicineById(id)
    }

    fun getMedicineByIdFlow(id: Int): Flow<Medicine?> {
        return medicineDao.getMedicineByIdFlow(id)
    }

    suspend fun insertMedicine(medicine: Medicine): Long {
        return medicineDao.insertMedicine(medicine)
    }

    suspend fun updateMedicine(medicine: Medicine) {
        medicineDao.updateMedicine(medicine)
    }

    suspend fun deleteMedicine(medicine: Medicine) {
        medicineDao.deleteMedicine(medicine)
    }

    suspend fun deleteMedicineById(id: Int) {
        medicineDao.deleteMedicineById(id)
    }

    suspend fun insertHistory(history: MedicineHistory) {
        medicineDao.insertHistory(history)
    }

    suspend fun deleteHistory(history: MedicineHistory) {
        medicineDao.deleteHistory(history)
    }

    suspend fun resetAllData() {
        medicineDao.clearAllMedicines()
        medicineDao.clearAllHistory()
    }
}
