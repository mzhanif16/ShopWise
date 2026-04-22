package com.shopwise.ui.screen.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.shopwise.R
import com.shopwise.core.database.ScanHistory
import com.shopwise.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    navController: NavController,
    viewModel: ScanHistoryViewModel = viewModel()
) {
    val scans by viewModel.allScans.collectAsStateWithLifecycle()

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
        if (scans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No scan history found", color = Color.Gray)
            }
        } else {
            val groupedScans = remember(scans) {
                scans.groupBy { formatGroupDate(it.timestamp) }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedScans.forEach { (dateHeader, dateScans) ->
                    item {
                        SectionHeader(dateHeader)
                    }
                    items(dateScans) { scan ->
                        HistoryItem(
                            name = scan.productName,
                            time = formatTimeOnly(scan.timestamp),
                            status = if (scan.isSafe) "SAFE" else "ALERT",
                            statusColor = if (scan.isSafe) PrimaryColor else Color(0xFFB3261E),
                            imageUri = scan.imageUri,
                            hasSideIndicator = !scan.isSafe
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    GemmaInsightCard()
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

fun formatGroupDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val time = Calendar.getInstance().apply { timeInMillis = timestamp }
    
    return when {
        isSameDay(now, time) -> "TODAY"
        isYesterday(now, time) -> "YESTERDAY"
        else -> SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(timestamp)).uppercase()
    }
}

fun formatTimeOnly(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply { 
        timeInMillis = cal1.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1) 
    }
    return yesterday.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           yesterday.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
fun HistoryItem(
    name: String,
    time: String,
    status: String,
    statusColor: Color,
    imageUri: String?,
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
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.img_check),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(60.dp)
                        )
                    }
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
                text = "You're consistently choosing safe products. 85% of your recent scans align with your allergy profile. Great job!",
                fontSize = 14.sp,
                color = Color(0xFF0D47A1),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 0.85f },
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
