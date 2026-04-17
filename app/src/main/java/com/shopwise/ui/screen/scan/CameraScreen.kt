package com.shopwise.ui.screen.scan

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.shopwise.R
import com.shopwise.ui.navigation.Routes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(isFlashOn, camera) {
        camera?.cameraControl?.enableTorch(isFlashOn)
        imageCapture.flashMode = if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    LaunchedEffect(lensFacing, hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val encodedUri = URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())
            navController.navigate("${Routes.ANALYSIS_RESULT}/$encodedUri")
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Health Guardian", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { isFlashOn = !isFlashOn }) {
                    Icon(
                        painter = painterResource(R.drawable.img_flash),
                        contentDescription = "Flash",
                        tint = if (isFlashOn) Color.Yellow else Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.img_gallery), contentDescription = "Gallery", tint = Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(4.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .clickable { capturePhoto(context, imageCapture) { uri ->
                                val encodedUri = URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())
                                navController.navigate("${Routes.ANALYSIS_RESULT}/$encodedUri")
                            } }
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(4.dp).clip(CircleShape).background(Color.White))
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { 
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) 
                                    CameraSelector.LENS_FACING_FRONT 
                                else 
                                    CameraSelector.LENS_FACING_BACK 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painter = painterResource(id = R.drawable.img_flip), contentDescription = "Flip", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("CAPTURE LABEL FOR ANALYSIS", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Place ingredients clearly in view", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission is required", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

private fun capturePhoto(context: Context, imageCapture: ImageCapture, onPhotoCaptured: (Uri) -> Unit) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ShopWise")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { onPhotoCaptured(it) }
            }
            override fun onError(exc: ImageCaptureException) {
                Log.e("CameraX", "Capture failed: ${exc.message}", exc)
            }
        }
    )
}
