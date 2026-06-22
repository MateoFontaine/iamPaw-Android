package com.example.iampaw.data

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import com.example.iampaw.data.local.IPawDao
import com.example.iampaw.data.local.toDogPosts
import com.example.iampaw.data.local.toLocal
import com.example.iampaw.domain.IPawRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PawRepository @Inject constructor(
    private val pawDao: IPawDao,
    private val mockDataSource: PawMockDataSource,
    private val apiDataSource: PawApiDataSource
) : IPawRepository {

    override fun observeFeed(searchQuery: String): Flow<List<DogPost>> {
        val source = if (searchQuery.isBlank()) {
            pawDao.observeAll()
        } else {
            pawDao.search(searchQuery)
        }
        return source.map { reports -> reports.toDogPosts() }
    }

    override suspend fun refreshFeedIfEmpty() {
        if (pawDao.count() == 0) {
            pawDao.insertAll(mockDataSource.getFeedDogs().map { it.toLocal() })
        }
    }

    override fun getDogDetail(id: String): DetailState = mockDataSource.getDogDetail(id)

    override fun getMatchedDogs(): List<MatchedDog> = mockDataSource.getMatchedDogs()

    override suspend fun getBreeds(): List<DogBreed> = apiDataSource.getBreeds()
}
