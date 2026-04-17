package com.shopwise.ui.screen.scan

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shopwise.R
import com.shopwise.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scan History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
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
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader("TODAY")
            HistoryItem(
                name = "Organic Almond Milk",
                time = "2 hours ago",
                status = "SAFE",
                statusColor = PrimaryColor,
                icon = R.drawable.img_check
            )
            Spacer(modifier = Modifier.height(12.dp))
            HistoryItem(
                name = "Protein Granola Bar",
                time = "4 hours ago",
                status = "ALERT",
                statusColor = Color(0xFFB3261E),
                icon = R.drawable.img_check,
                hasSideIndicator = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader("YESTERDAY")
            HistoryItem(
                name = "Greek Yogurt",
                time = "Yesterday, 4:12 PM",
                status = "SAFE",
                statusColor = PrimaryColor,
                icon = R.drawable.img_check
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gemma AI Insight Box
            GemmaInsightCard()

            Spacer(modifier = Modifier.height(24.dp))

            HistoryItem(
                name = "Raw Kombucha",
                time = "Yesterday, 11:30 AM",
                status = "SAFE",
                statusColor = PrimaryColor,
                icon = R.drawable.img_check
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun HistoryItem(
    name: String,
    time: String,
    status: String,
    statusColor: Color,
    icon: Int,
    hasSideIndicator: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            if (hasSideIndicator) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(statusColor)
                )
            }
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F1F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(time, color = Color.Gray, fontSize = 12.sp)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
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
fun GemmaInsightCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE3F2FD).copy(alpha = 0.7f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "GEMMA AI INSIGHT",
                    color = Color(0xFF1E88E5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "You've scanned 12 items this week. 80% of your choices were Allergen-Free. Your health score is improving.",
                fontSize = 14.sp,
                color = Color(0xFF0D47A1),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 0.8f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = PrimaryColor,
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }
}
