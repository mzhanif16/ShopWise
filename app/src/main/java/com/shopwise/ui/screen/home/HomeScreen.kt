package com.shopwise.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shopwise.R
import com.shopwise.core.UserPreferences
import com.shopwise.ui.navigation.Routes
import com.shopwise.ui.theme.PrimaryColor

@Composable
fun HomeScreen(navController: NavController, dashboardViewModel: DashboardViewModel = viewModel()) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val userData = userPreferences.getUserData()
    val fullName = (userData["fullName"] as? String) ?: "User"
    val firstName = fullName.split(" ").firstOrNull() ?: "User"
    val allergies = (userData["allergies"] as? Set<*>)?.joinToString(" and ") ?: "None"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Welcome Section
        Text(
            text = "WELCOME BACK",
            color = Color(0xFF3277D8),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Hello, $firstName!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Allergy Status Info Box
        AllergyStatusBox(allergies)

        Spacer(modifier = Modifier.height(32.dp))

        // Kondisi: Jika model sedang loading, tampilkan animasi initializing
        // Jika sudah selesai, tampilkan Quick Photo Button
        AnimatedContent(
            targetState = dashboardViewModel.isModelInitializing,
            label = "ModelStatusAnimation"
        ) { isInitializing ->
            if (isInitializing) {
                ModelInitializingAnimation(dashboardViewModel.initializationMessage)
            } else {
                QuickPhotoButton(onPhotoClick = {
                    navController.navigate(Routes.CAMERA)
                })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Assistant Button with Gradient
        AIAssistantButton()

        Spacer(modifier = Modifier.height(32.dp))

        // Recent Scans Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Scans",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "View History",
                color = PrimaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate(Routes.SCAN_HISTORY)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scan Items
        RecentScanItem(
            name = "Organic Almond Milk",
            time = "2 hours ago",
            status = "SAFE",
            statusColor = PrimaryColor,
            icon = Icons.Default.Info
        )
        Spacer(modifier = Modifier.height(12.dp))
        RecentScanItem(
            name = "Protein Granola Bar",
            time = "Yesterday",
            status = "ALERT",
            statusColor = Color(0xFFB3261E),
            icon = Icons.Default.Info
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Insight Row
        GemmaInsightRow()

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ModelInitializingAnimation(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "InitAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryColor,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PrimaryColor
            )
            Text(
                text = "Preparing intelligence brain...",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AllergyStatusBox(allergies: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF7F7F7)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryColor)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                painter = painterResource(R.drawable.img_check),
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = buildAnnotatedString {
                    append("Your allergy profile is active. Scanning for ")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    ) {
                        append(allergies)
                    }
                    append(".")
                },
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun QuickPhotoButton(onPhotoClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(elevation = 24.dp, shape = CircleShape, spotColor = PrimaryColor)
                .background(Color.White, CircleShape)
        )

        // Gradient Button
        val buttonGradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF4DB6AC), // Lighter Teal
                PrimaryColor,       // Base Primary Color
                Color(0xFF00796B)  // Darker Teal
            )
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(buttonGradient)
                .clickable(onClick = onPhotoClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.img_camera),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "QUICK PHOTO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun AIAssistantButton() {
    val aiGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFBBDEFB), // Very Light Blue
            Color(0xFF90CAF9), // Light Blue
            Color(0xFF64B5F6)  // Medium Blue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(aiGradient)
            .clickable { /* TODO: AI Assistant */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.img_gemma),
                modifier = Modifier.size(100.dp),
                contentDescription = null,
                tint = Color(0xFF004069)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "AI Assistant ready for\ningredient analysis",
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004069)
            )
        }
    }
}

@Composable
fun RecentScanItem(
    name: String,
    time: String,
    status: String,
    statusColor: Color,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF7F7F7)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left indicator bar as seen in the image
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(statusColor)
            )
            
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = time, color = Color.Gray, fontSize = 12.sp)
                }
                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.1f),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GemmaInsightRow() {
    Surface(
        onClick = { /* TODO */ },
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE3F2FD),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.img_brain),
                        contentDescription = null,
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GEMMA INSIGHT",
                    color = Color(0xFF1E88E5),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "New nut-free alternatives found",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Icon(
                painter = painterResource(R.drawable.img_arrow_right),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
