package com.example.iampaw.components.report

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = viewModel() // Inyectamos el ViewModel
) {
    val context = LocalContext.current

    // Observamos los estados
    val state by viewModel.uiState.collectAsState()
    val breeds by viewModel.breeds.collectAsState()

    // Estados puramente visuales (se quedan en la UI)
    var expanded by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if(uri != null) viewModel.onImageSelected(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && state.tempCameraUri != null) {
            viewModel.onImageSelected(state.tempCameraUri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.setLocationLoading(true)
            obtenerUbicacionActual(context) { lat, lng ->
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val direcciones = geocoder.getFromLocation(lat, lng, 1)

                    if (!direcciones.isNullOrEmpty()) {
                        viewModel.updateLocationText(direcciones[0].getAddressLine(0) ?: "Dirección desconocida")
                    } else {
                        viewModel.updateLocationText("Lat: $lat, Lng: $lng")
                    }
                } catch (e: Exception) {
                    viewModel.updateLocationText("Coordenadas: $lat, $lng")
                } finally {
                    viewModel.setLocationLoading(false)
                }
            }
        } else {
            viewModel.updateLocationText("Permiso denegado")
        }
    }

    val orangePaw = Color(0xFFFF9800)
    val bgColor = Color(0xFFFBFBFB)

    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Seleccionar foto", fontWeight = FontWeight.Bold) },
            text = { Text("Elegí de dónde querés obtener la imagen de la mascota.") },
            confirmButton = {
                TextButton(onClick = {
                    val uri = crearUriTemporal(context)
                    viewModel.setTempCameraUri(uri)
                    cameraLauncher.launch(uri)
                    showPhotoOptions = false
                }) {
                    Text("Cámara", color = orangePaw, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showPhotoOptions = false
                }) {
                    Text("Galería", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver", tint = Color.Black)
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.Black)) { append("iam") }
                    withStyle(SpanStyle(color = orangePaw)) { append("Paw") }
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }

        Text(
            text = "Completá los datos para emitir la alerta.",
            color = Color.Gray,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFEEEEEE))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (state.isLost) Color.White else Color.Transparent)
                    .clickable { viewModel.setLostStatus(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Busco a mi mascota",
                    fontWeight = FontWeight.Bold,
                    color = if (state.isLost) Color.Black else Color.Gray,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (!state.isLost) Color.White else Color.Transparent)
                    .clickable { viewModel.setLostStatus(false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Encontré una mascota",
                    fontWeight = FontWeight.Bold,
                    color = if (!state.isLost) Color.Black else Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF5F5F5))
                .clickable { showPhotoOptions = true }
                .drawDashedBorder(orangePaw, 20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.imageUri == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Toca para subir una foto", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Text("Soporta Cámara, JPG, PNG", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                AsyncImage(
                    model = state.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Raza detectada", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.breedText,
                onValueChange = { viewModel.updateBreedText(it); expanded = true },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                placeholder = { Text("Ej: Golden Retriever") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = orangePaw,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            val filteredOptions = breeds.filter { it.name.contains(state.breedText, ignoreCase = true) }
            if (filteredOptions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    filteredOptions.take(5).forEach { breed ->
                        DropdownMenuItem(
                            text = { Text(breed.name) },
                            onClick = {
                                viewModel.updateBreedText(breed.name)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Ubicación", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.locationText,
                onValueChange = { viewModel.updateLocationText(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("¿Dónde fue visto?") },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (state.isLocationLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = orangePaw
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = orangePaw,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = {
                    if (!state.isLocationLoading) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFF5E6D3), RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = "Usar mi ubicación", tint = orangePaw)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.colorText,
                onValueChange = { viewModel.updateColorText(it) },
                modifier = Modifier.weight(1f),
                label = { Text("Color principal") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = orangePaw, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                singleLine = true
            )
            OutlinedTextField(
                value = state.sizeText,
                onValueChange = { viewModel.updateSizeText(it) },
                modifier = Modifier.weight(1f),
                label = { Text("Tamaño aprox.") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = orangePaw, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.detailsText,
            onValueChange = { viewModel.updateDetailsText(it) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            label = { Text("Detalles adicionales") },
            placeholder = { Text("Llevaba un collar azul, está asustado...") },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = orangePaw, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate("match_screen")
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = orangePaw)
        ) {
            Text(if (state.isLost) "Publicar Búsqueda" else "Reportar Mascota", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- FUNCIONES AUXILIARES INTACTAS ---
fun crearUriTemporal(context: Context): Uri {
    val directory = File(context.cacheDir, "camera_images")
    if (!directory.exists()) directory.mkdirs()
    val file = File.createTempFile("iampaw_snap_", ".jpg", directory)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@SuppressLint("MissingPermission")
fun obtenerUbicacionActual(context: Context, onLocationReceived: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        CancellationTokenSource().token
    ).addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived(location.latitude, location.longitude)
        } else {
            onLocationReceived(0.0, 0.0)
        }
    }.addOnFailureListener {
        onLocationReceived(0.0, 0.0)
    }
}

fun Modifier.drawDashedBorder(color: Color, radius: Dp) = this.drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
        ),
        cornerRadius = CornerRadius(radius.toPx())
    )
}