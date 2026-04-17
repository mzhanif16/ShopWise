package com.shopwise.ui.screen.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopwise.ui.theme.PrimaryColor

@Composable
fun OnBoardingPage3(
    scrollState: ScrollState,
    selectedModel: String,
    onModelSelected: (String) -> Unit
) {
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
        ModelSelectionCard(
            title = "Gemma 4B (Advanced & Deep)",
            description = "Unmatched clinical precision. Detects complex allergen cross-contaminations and nutritional nuances.",
            storage = "2.8GB Storage",
            icon = Icons.Default.Info,
            badgeText = "RECOMMENDED",
            isSelected = selectedModel == "4B",
            onClick = { onModelSelected("4B") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Model 2B Card
        ModelSelectionCard(
            title = "Gemma 2B (Lite & Fast)",
            description = "Lightning quick scans for essential allergen detection. Perfect for newer hardware.",
            storage = "1.5GB Storage",
            icon = Icons.Default.Info,
            badgeText = "EFFICIENT",
            isSelected = selectedModel == "2B",
            onClick = { onModelSelected("2B") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Syncing Box
        SyncingProgressBox()

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
fun ModelSelectionCard(
    title: String,
    description: String,
    storage: String,
    icon: ImageVector,
    badgeText: String,
    isSelected: Boolean,
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
                        imageVector = icon,
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
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
        }
    }
}

@Composable
fun SyncingProgressBox() {
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
                    progress = { 0.65f },
                    modifier = Modifier.fillMaxSize(),
                    color = PrimaryColor,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "65%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "SYNCING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Gemma is learning your health\nprofile...",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

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
                    text = "Optimizing clinical weights",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}
