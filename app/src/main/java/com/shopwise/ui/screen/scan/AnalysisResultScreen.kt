package com.shopwise.ui.screen.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shopwise.R
import com.shopwise.ui.navigation.Routes
import com.shopwise.ui.theme.PrimaryColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun AnalysisResultScreen(navController: NavController, imageUri: String) {
    var isLoading by remember { mutableStateOf(true) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    // Simulasi: Jika URI mengandung kata "safe", maka tampilkan kondisi aman
    val isSafe = remember { !imageUri.contains("safe", ignoreCase = true) }
    val context = LocalContext.current

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(imageUri)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    bitmap = BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        delay(3000) 
        isLoading = false
    }

    if (isLoading) {
        LoadingView()
    } else {
        ResultContent(navController, bitmap, isSafe)
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.img_gemma),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    tint = PrimaryColor
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Gemma is analyzing label...",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .width(150.dp)
                    .clip(CircleShape),
                color = PrimaryColor,
                trackColor = PrimaryColor.copy(alpha = 0.2f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultContent(navController: NavController, bitmap: Bitmap?, isSafe: Boolean) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "ShopWise",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Profile */ }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.img_brain),
                                contentDescription = "Profile",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSafe) PrimaryColor else Color(0xFF00897B)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isSafe) "Back to Dashboard" else "Find Safe Alternatives", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ask Gemma for Details", color = Color(0xFF0277BD))
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Header Banner (Danger or Safe)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSafe) PrimaryColor else Color(0xFFFF5252)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSafe) Color.White else Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (isSafe) "SAFE" else "DANGER",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSafe) Color.White else Color(0xFF310000)
                    )
                    Text(
                        if (isSafe) "NO ALLERGENS DETECTED" else "SEVERE ALLERGEN DETECTED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSafe) Color.White.copy(alpha = 0.8f) else Color(0xFF310000)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = if (isSafe) "Product: Fresh Whole Grain\nOatmeal" else "Product: Crunchy Peanut\nButter Granola",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (isSafe) Color.White else Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Safety Analysis Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isSafe) Color(0xFFE0F2F1) else Color(0xFFE3F2FD),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.img_gemma),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = if (isSafe) PrimaryColor else Color(0xFF01579B)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "AI Safety\nAnalysis",
                            fontWeight = FontWeight.Bold,
                            color = if (isSafe) PrimaryColor else Color(0xFF01579B),
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isSafe) {
                            buildAnnotatedString {
                                append("Gemma has analyzed the ingredients and found ")
                                withStyle(SpanStyle(color = PrimaryColor, fontWeight = FontWeight.Bold)) {
                                    append("no matches")
                                }
                                append(" with your allergy profile. This product is considered safe for consumption based on your clinical safeguards.")
                            }
                        } else {
                            buildAnnotatedString {
                                append("Gemma 4 has identified high concentrations of ")
                                withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                    append("Peanut Protein")
                                }
                                append(" and trace amounts of ")
                                withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                    append("Soy Lecithin")
                                }
                                append(". This product is strictly prohibited for your clinical profile.")
                            }
                        },
                        color = if (isSafe) PrimaryColor else Color(0xFF0277BD),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Detected Ingredients Section
            if (!isSafe) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Detected Ingredients", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("2 ALLERGENS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetectedIngredientItem(
                    name = "Peanut",
                    detail = "Primary Ingredient • 15% concentration",
                    risk = "HIGH RISK",
                    riskColor = Color(0xFFFFEBEE),
                    riskTextColor = Color.Red,
                    sideColor = Color.Red,
                    icon = R.drawable.img_check
                )
                Spacer(modifier = Modifier.height(12.dp))
                DetectedIngredientItem(
                    name = "Soy",
                    detail = "Processing Agent • Trace amounts",
                    risk = "MEDIUM RISK",
                    riskColor = Color(0xFFFFEBEE),
                    riskTextColor = Color.Red,
                    sideColor = Color.Red,
                    icon = R.drawable.img_check
                )
            } else {
                Text("Ingredients Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                DetectedIngredientItem(
                    name = "Whole Grain Oats",
                    detail = "Safe for your profile",
                    risk = "SAFE",
                    riskColor = Color(0xFFE0F2F1),
                    riskTextColor = PrimaryColor,
                    sideColor = PrimaryColor,
                    icon = R.drawable.img_check
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Photo Preview
            Text("Captured Photo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Captured Label",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metadata
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(Color(0xFF80CBC4))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("SCAN METADATA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(
                        text = "Captured at Healthy Market •\n10:30 AM Today",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
fun DetectedIngredientItem(
    name: String,
    detail: String,
    risk: String,
    riskColor: Color,
    riskTextColor: Color,
    sideColor: Color,
    icon: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F1F1)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(sideColor)
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
                    Icon(
                        painterResource(icon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(detail, color = Color.Gray, fontSize = 11.sp)
                }
                Surface(
                    color = riskColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = risk,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = riskTextColor
                    )
                }
            }
        }
    }
}
