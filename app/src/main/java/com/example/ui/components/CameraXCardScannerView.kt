package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.security.HapticsHelper
import java.util.concurrent.Executors

/**
 * Modern CameraX live viewfinder Composable tailored for financial cards scanning.
 * - Live camera stream using PreviewView
 * - Card alignment bounding reticle with animated scanning laser
 * - Flash/Torch control
 * - High-speed ImageCapture pipeline delivering Bitmap for Gemini AI OCR
 */
@Composable
fun CameraXCardScannerView(
    onCardImageCaptured: (Bitmap) -> Unit,
    onError: (String) -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            onError("Camera permission is required to scan cards.")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF13100E))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2219)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFFE5A93C),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant camera access to align and capture physical cards for Gemini AI OCR recognition.",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                ElevatedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFFE5A93C),
                        contentColor = Color(0xFF261502)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Camera Preview Feed
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        cameraInstance = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )

                        // Enable tap-to-focus on PreviewView
                        previewView.setOnTouchListener { _, event ->
                            if (event.action == android.view.MotionEvent.ACTION_UP) {
                                val factory = previewView.meteringPointFactory
                                val point = factory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(point).build()
                                cameraInstance?.cameraControl?.startFocusAndMetering(action)
                            }
                            true
                        }
                    } catch (e: Exception) {
                        Log.e("CameraX", "Binding failed: ${e.message}", e)
                        onError("Failed to start CameraX: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Card Frame Mask & Overlay
        CardViewfinderOverlay(
            modifier = Modifier.fillMaxSize()
        )

        // Controls Header (Torch toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = {
                    haptics?.cardSelect()
                    isTorchOn = !isTorchOn
                    cameraInstance?.cameraControl?.enableTorch(isTorchOn)
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Torch",
                    tint = if (isTorchOn) Color(0xFFE5A93C) else Color.White
                )
            }
        }

        // Shutter Button & Capture Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    color = Color(0xFFE5A93C),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(56.dp)
                )
            } else {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5A93C))
                        .clickable {
                            haptics?.success()
                            isCapturing = true

                            imageCapture.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        try {
                                            val rawBitmap = imageProxy.toBitmap()
                                            val rotation = imageProxy.imageInfo.rotationDegrees
                                            val finalBitmap = if (rotation != 0) {
                                                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                                                Bitmap.createBitmap(
                                                    rawBitmap,
                                                    0,
                                                    0,
                                                    rawBitmap.width,
                                                    rawBitmap.height,
                                                    matrix,
                                                    true
                                                )
                                            } else {
                                                rawBitmap
                                            }

                                            // Crop to card area corresponding to viewfinder reticle with margin
                                            val cropWidth = (finalBitmap.width * 0.92f).toInt().coerceAtMost(finalBitmap.width)
                                            val cropHeight = (cropWidth / 1.586f).toInt().coerceAtMost(finalBitmap.height)
                                            val cropX = ((finalBitmap.width - cropWidth) / 2).coerceAtLeast(0)
                                            val cropY = ((finalBitmap.height - cropHeight) / 2).coerceAtLeast(0)

                                            val cardRegionBitmap = try {
                                                Bitmap.createBitmap(finalBitmap, cropX, cropY, cropWidth, cropHeight)
                                            } catch (e: Exception) {
                                                finalBitmap
                                            }

                                            imageProxy.close()

                                            ContextCompat.getMainExecutor(context).execute {
                                                isCapturing = false
                                                onCardImageCaptured(cardRegionBitmap)
                                            }
                                        } catch (e: Exception) {
                                            imageProxy.close()
                                            ContextCompat.getMainExecutor(context).execute {
                                                isCapturing = false
                                                onError("Error capturing photo: ${e.message}")
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        ContextCompat.getMainExecutor(context).execute {
                                            isCapturing = false
                                            onError("Camera capture failed: ${exception.message}")
                                        }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Capture Card",
                        tint = Color(0xFF261502),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Viewfinder overlay masking non-card areas and showing alignment guides
 */
@Composable
private fun CardViewfinderOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPos"
    )

    BoxWithConstraints(modifier = modifier) {
        val screenW = maxWidth.value
        val screenH = maxHeight.value

        // Standard credit card aspect ratio is 85.60 mm x 53.98 mm ~= 1.586
        val cardW = (screenW * 0.88f)
        val cardH = cardW / 1.586f

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(cardW.dp)
                    .height(cardH.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(2.dp, Color(0xFFE5A93C).copy(alpha = 0.85f), RoundedCornerShape(18.dp))
            ) {
                // Animated laser scanner beam
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(top = (cardH * laserProgress).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFFE5A93C),
                                    Color.White,
                                    Color(0xFFE5A93C),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Corner target brackets
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val bracketLen = 24.dp.toPx()
                    val strokeW = 4.dp.toPx()
                    val color = Color(0xFFE5A93C)

                    // Top-Left
                    drawLine(color, Offset(0f, 0f), Offset(bracketLen, 0f), strokeW)
                    drawLine(color, Offset(0f, 0f), Offset(0f, bracketLen), strokeW)

                    // Top-Right
                    drawLine(color, Offset(size.width, 0f), Offset(size.width - bracketLen, 0f), strokeW)
                    drawLine(color, Offset(size.width, 0f), Offset(size.width, bracketLen), strokeW)

                    // Bottom-Left
                    drawLine(color, Offset(0f, size.height), Offset(bracketLen, size.height), strokeW)
                    drawLine(color, Offset(0f, size.height), Offset(0f, size.height - bracketLen), strokeW)

                    // Bottom-Right
                    drawLine(color, Offset(size.width, size.height), Offset(size.width - bracketLen, size.height), strokeW)
                    drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - bracketLen), strokeW)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Align card edges inside the yellow frame",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
