package com.shopwise.ui.screen.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopwise.R
import com.shopwise.ui.theme.PrimaryColor
import compose.icons.EvaIcons
import compose.icons.SimpleIcons
import compose.icons.evaicons.Fill
import compose.icons.evaicons.Outline
import compose.icons.evaicons.fill.CloudUpload
import compose.icons.evaicons.fill.Download
import compose.icons.evaicons.outline.Cast
import compose.icons.evaicons.outline.CloudUpload

@Composable
fun OnBoardingPage3(
    scrollState: ScrollState,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    gemmaViewModel: GemmaViewModel
) {
    val model4B = gemmaViewModel.models["4B"]
    val model2B = gemmaViewModel.models["2B"]
    val activeModel = gemmaViewModel.activeDownloadId?.let { gemmaViewModel.models[it] }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Model Brain Setup",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose your intelligence level. Gemma runs locally on your device for maximum privacy.",
            color = Color.Gray,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Model 4B Card
        model4B?.let { model ->
            ModelSelectionCard(
                title = "Gemma 4B (Advanced & Deep)",
                description = "Unmatched clinical precision. Detects complex allergen cross-contaminations and nutritional nuances.",
                storage = "3.65GB Storage",
                icon = painterResource(R.drawable.img_e4b),
                badgeText = "RECOMMENDED",
                isSelected = selectedModel == "4B",
                isDownloaded = model.isDownloaded,
                isDownloading = model.isDownloading,
                progress = model.downloadProgress,
                onDownloadClick = { gemmaViewModel.downloadModel("4B") },
                onClick = { onModelSelected("4B") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Model 2B Card
        model2B?.let { model ->
            ModelSelectionCard(
                title = "Gemma 2B (Lite & Fast)",
                description = "Lightning quick scans for essential allergen detection. Perfect for newer hardware.",
                storage = "2.58GB Storage",
                icon = painterResource(R.drawable.img_e2b),
                badgeText = "EFFICIENT",
                isSelected = selectedModel == "2B",
                isDownloaded = model.isDownloaded,
                isDownloading = model.isDownloading,
                progress = model.downloadProgress,
                onDownloadClick = { gemmaViewModel.downloadModel("2B") },
                onClick = { onModelSelected("2B") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Syncing Box
        SyncingProgressBox(
            isDownloading = gemmaViewModel.activeDownloadId != null,
            progress = activeModel?.downloadProgress ?: 0f,
            downloadSpeed = activeModel?.downloadSpeed ?: "",
            downloadedBytesDisplay = activeModel?.downloadedBytesDisplay ?: "",
            onCancel = { gemmaViewModel.cancelActiveDownload() }
        )

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
fun ModelSelectionCard(
    title: String,
    description: String,
    storage: String,
    icon: Painter,
    badgeText: String,
    isSelected: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onDownloadClick: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = if (isSelected) BorderStroke(2.dp, PrimaryColor) else null,
        color = Color(0xFFF7F7F7),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) PrimaryColor.copy(alpha = 0.1f) else Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = if (isSelected) PrimaryColor else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) PrimaryColor else Color.LightGray.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = EvaIcons.Fill.CloudUpload,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = storage,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (!isDownloaded) {
                    if (isDownloading) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PrimaryColor,
                                trackColor = Color.LightGray.copy(alpha = 0.3f)
                            )
                        }
                    } else {
                        Button(
                            onClick = onDownloadClick,
                            enabled = isSelected, // Hanya bisa di klik jika card terpilih
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColor,
                                disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = EvaIcons.Fill.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color.White else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Download",
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ready", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SyncingProgressBox(
    isDownloading: Boolean,
    progress: Float,
    downloadSpeed: String,
    downloadedBytesDisplay: String,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background glossy circle (blue gradient) in the center
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryColor.copy(0.6f), Color(0xFFBBDEFB))
                            )
                        )
                )
                
                // The progress indicator with track color and thickness
                CircularProgressIndicator(
                    progress = { if (isDownloading) progress else 0f },
                    modifier = Modifier.fillMaxSize(),
                    color = PrimaryColor,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isDownloading) "${(progress * 100).toInt()}%" else "0%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.Black
                    )
                    Text(
                        text = if (isDownloading) "DOWNLOADING" else "WAITING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                
                if (isDownloading) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp), tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isDownloading) {
                Text(
                    text = downloadedBytesDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = downloadSpeed,
                    fontSize = 14.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Gemma is learning your health\nprofile...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDownloading) "Downloading intelligence weights" else "Optimizing clinical weights",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}
