package com.example.iampaw.domain

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import com.example.iampaw.components.report.ReportDraft
import com.example.iampaw.data.DogBreed
import kotlinx.coroutines.flow.Flow

interface IPawRepository {
    fun observeFeed(): Flow<List<DogPost>>
    suspend fun refreshFeedIfEmpty()
    suspend fun saveReport(report: DogPost): Result<Unit>
    suspend fun syncReportsFromFirestore()
    suspend fun getDogDetail(id: String): DetailState
    suspend fun markReportResolved(reportId: String): Result<Unit>
    fun getMatchedDogs(): List<MatchedDog>
    suspend fun getMatchCandidates(draft: ReportDraft): List<DogPost>
    suspend fun getBreeds(): List<DogBreed>
    suspend fun getUserContactPhone(): String
    suspend fun saveUserContactPhone(phone: String): Result<Unit>
}
