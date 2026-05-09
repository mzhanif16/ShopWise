package com.shopwise.ui.screen.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopwise.R
import com.shopwise.ui.screen.onboarding.GemmaViewModel
import com.shopwise.ui.screen.onboarding.SyncingProgressBox
import com.shopwise.ui.theme.PrimaryColor
import compose.icons.EvaIcons
import compose.icons.evaicons.Fill
import compose.icons.evaicons.fill.CloudUpload
import compose.icons.evaicons.fill.Download

@Composable
fun ModelSetupScreen(dashboardViewModel: DashboardViewModel) {
    val gemmaViewModel: GemmaViewModel = viewModel()
    val scrollState = rememberScrollState()
    
    // Use reactive state from ViewModel
    val selectedModel = dashboardViewModel.selectedModel

    val model4B = gemmaViewModel.models["4B"]
    val model2B = gemmaViewModel.models["2B"]
    val activeModel = gemmaViewModel.activeDownloadId?.let { gemmaViewModel.models[it] }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Intelligence Center",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Select your preferred AI model. Higher models provide deeper analysis but require more storage.",
            color = Color.Gray,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Model 4B Card
        model4B?.let { model ->
            ModelSelectionCardInDashboard(
                title = "Gemma 4B (Advanced)",
                description = "Deep clinical precision. Detects complex allergen cross-contaminations and nutritional nuances.",
                storage = "3.65GB Storage",
                icon = painterResource(R.drawable.img_e4b),
                badgeText = "BEST ANALYSIS",
                isSelected = selectedModel == "4B",
                isDownloaded = model.isDownloaded,
                isDownloading = model.isDownloading,
                progress = model.downloadProgress,
                onDownloadClick = { gemmaViewModel.downloadModel("4B") },
                onClick = { 
                    dashboardViewModel.updateSelectedModel("4B")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Model 2B Card
        model2B?.let { model ->
            ModelSelectionCardInDashboard(
                title = "Gemma 2B (Lite & Fast)",
                description = "Quick scans for essential allergen detection. Optimized for efficiency.",
                storage = "2.58GB Storage",
                icon = painterResource(R.drawable.img_e2b),
                badgeText = "EFFICIENT",
                isSelected = selectedModel == "2B",
                isDownloaded = model.isDownloaded,
                isDownloading = model.isDownloading,
                progress = model.downloadProgress,
                onDownloadClick = { gemmaViewModel.downloadModel("2B") },
                onClick = { 
                    dashboardViewModel.updateSelectedModel("2B")
                }
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

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ModelSelectionCardInDashboard(
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
                            enabled = isSelected,
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
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ready",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
