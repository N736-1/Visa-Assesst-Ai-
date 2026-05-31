package com.example.data

import kotlinx.coroutines.flow.Flow

class VisaRepository(private val visaDao: VisaDao) {
    val allApplications: Flow<List<VisaApplication>> = visaDao.getAllApplications()

    fun getLogsForApplication(applicationUid: Int): Flow<List<StatusUpdateLog>> {
        return visaDao.getLogsForApplication(applicationUid)
    }

    suspend fun insertApplication(application: VisaApplication): Long {
        return visaDao.insertApplication(application)
    }

    suspend fun insertLog(log: StatusUpdateLog): Long {
        return visaDao.insertLog(log)
    }

    suspend fun insertLogs(logs: List<StatusUpdateLog>) {
        visaDao.insertLogs(logs)
    }

    suspend fun updateApplication(application: VisaApplication) {
        visaDao.updateApplication(application)
    }

    suspend fun deleteApplication(uid: Int) {
        visaDao.deleteLogsForApplication(uid)
        visaDao.deleteApplication(uid)
    }

    suspend fun seedMockApplicationIfEmpty() {
        // We will do this seed check in our ViewModel initialization
    }
}
