package com.example.iampaw.components.report

import android.net.Uri

data class ReportState(
    val isLost: Boolean = true,
    val isLocationLoading: Boolean = false,
    val imageUri: Uri? = null,
    val tempCameraUri: Uri? = null,
    val validationError: String? = null
)

data class ReportFormInput(
    val nameText: String,
    val breedText: String,
    val locationText: String,
    val colorText: String,
    val sizeText: String,
    val detailsText: String
)
