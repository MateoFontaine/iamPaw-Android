package com.example.iampaw.components.commons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.iampaw.R

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ReportGlideImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    GlideImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    ) {
        it
            .placeholder(R.drawable.report_image_placeholder)
            .error(R.drawable.report_image_placeholder)
    }
}
