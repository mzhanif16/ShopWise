package com.shopwise.ui.screen.scan

import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.shopwise.R
import com.shopwise.core.UserPreferences
import com.shopwise.ui.navigation.Routes
import com.shopwise.ui.theme.PrimaryColor
import compose.icons.EvaIcons
import compose.icons.evaicons.Fill
import compose.icons.evaicons.fill.ArrowBack
import compose.icons.evaicons.fill.PlayCircle
import compose.icons.evaicons.fill.StopCircle
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.util.Locale

@Composable
fun DetailResultScreen(
    navController: NavController,
    scanId: Int,
    viewModel: DetailResultViewModel = viewModel()
) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    val userPreferences = remember { UserPreferences(context) }
    val language = remember { userPreferences.getLanguage() ?: "en" }

    // Initialize TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set language based on app preference
                if (language == "in") {
                    tts?.language = Locale("id", "ID")
                } else {
                    tts?.language = Locale.US
                }
                
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { isSpeaking = true }
                    override fun onDone(utteranceId: String?) { isSpeaking = false }
                    override fun onError(utteranceId: String?) { isSpeaking = false }
                })
            }
        }
    }

    // Load scan data from Room
    LaunchedEffect(scanId) {
        viewModel.loadScanDetail(scanId)
    }

    val scan = viewModel.scanResult

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
    } else if (scan == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.analysis_record_not_found), color = Color.Gray)
        }
    } else {
        DetailResultContent(
            navController = navController,
            productName = scan.productName,
            isSafe = scan.isSafe,
            analysisResult = scan.finalResult,
            imageUri = scan.imageUri,
            tts = tts,
            isSpeaking = isSpeaking,
            language = language
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailResultContent(
    navController: NavController,
    productName: String,
    isSafe: Boolean,
    analysisResult: String,
    imageUri: String?,
    tts: TextToSpeech?,
    isSpeaking: Boolean,
    language: String
) {
    val scrollState = rememberScrollState()

    fun speak(text: String) {
        val cleanText = text.replace("*", "")
        if (language == "in") {
            tts?.setLanguage(Locale("id", "ID"))
        } else {
            tts?.setLanguage(Locale.US)
        }
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "detail_utterance")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = PrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9F9F9),
        modifier = Modifier.navigationBarsPadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Banner
            val bannerColor = if (isSafe) PrimaryColor else Color(0xFFFF5252)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = bannerColor)
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
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isSafe) stringResource(R.string.status_safe) else stringResource(R.string.status_danger),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = if (isSafe) stringResource(R.string.no_allergens_detected) else stringResource(R.string.severe_allergen_detected),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = productName.uppercase(),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Safety Analysis Box
            val boxBgColor = if (isSafe) Color(0xFFE0F2F1) else Color(0xFFFBE9E7)
            val boxTextColor = if (isSafe) PrimaryColor else Color(0xFFD32F2F)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = boxBgColor,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.img_gemma),
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = boxTextColor
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.ai_safety_analysis),
                                fontWeight = FontWeight.Bold,
                                color = boxTextColor,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Langsung menampilkan text tanpa efek mengetik
                        MarkdownText(
                            markdown = analysisResult,
                            style = TextStyle(
                                color = boxTextColor,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }

                    // Speech Control
                    IconButton(
                        onClick = {
                            if (isSpeaking) tts?.stop() else speak(analysisResult)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(boxTextColor.copy(alpha = 0.1f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) EvaIcons.Fill.StopCircle else EvaIcons.Fill.PlayCircle,
                            contentDescription = "Speech",
                            tint = boxTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Photo Preview
            if (!imageUri.isNullOrEmpty()) {
                Text(stringResource(R.string.product_photo), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = Uri.parse(imageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { navController.navigate(Routes.DASHBOARD) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.back_to_dashboard), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { navController.navigate(Routes.CHAT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.ask_gemma_details), color = Color(0xFF0277BD))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
