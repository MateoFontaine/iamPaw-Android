package com.example.iampaw.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_reports")
data class PetReportLocal(
    @PrimaryKey val id: String,
    val name: String,
    val breed: String,
    val color: String = "",
    val size: String = "",
    val details: String = "",
    val aiAnalysis: String = "",
    val location: String,
    val time: String,
    val imageUrl: String,
    val status: String,
    val userId: String = "",
    val contactPhone: String = "",
    val createdAt: Long = 0L
)
