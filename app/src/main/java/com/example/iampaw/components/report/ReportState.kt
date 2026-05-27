package com.example.iampaw.components.report

import android.net.Uri

data class ReportState(
    val breedText: String = "",
    val colorText: String = "",
    val sizeText: String = "",
    val detailsText: String = "",
    val isLost: Boolean = true,
    val locationText: String = "",
    val isLocationLoading: Boolean = false,
    val imageUri: Uri? = null,
    val tempCameraUri: Uri? = null // Guarda la ruta temporal para que no se pierda al rotar la pantalla
)