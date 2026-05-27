package com.example.iampaw.data

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog

class PawRepository(private val dataSource: IPawDataSource) {

    fun getFeedDogs(): List<DogPost> {
        return dataSource.getFeedDogs()
    }

    fun getDogDetail(id: String): DetailState {
        return dataSource.getDogDetail(id)
    }

    fun getMatchedDogs(): List<MatchedDog> {
        return dataSource.getMatchedDogs()
    }
}