package com.shopwise.ui.screen.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shopwise.R
import com.shopwise.core.UserPreferences
import com.shopwise.ui.navigation.Routes
import com.shopwise.ui.theme.PrimaryColor
import compose.icons.EvaIcons
import compose.icons.evaicons.Fill
import compose.icons.evaicons.fill.PlayCircle
import compose.icons.evaicons.fill.StopCircle
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

@Composable
fun AnalysisResultScreen(
    navController: NavController, 
    imageUri: String,
    viewModel: AnalysisResultViewModel = viewModel()
) {
    val context = LocalContext.current
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisResult by viewModel.analysisResult.collectAsStateWithLifecycle()
    
    val bitmaps = remember { mutableStateListOf<Bitmap>() }
    var isSafe by remember { mutableStateOf<Boolean?>(null) }
    var hasStartedAnalysis by remember { mutableStateOf(false) }

    // State to track if TTS is currently speaking
    var isSpeaking by remember { mutableStateOf(false) }

    val userPreferences = remember { UserPreferences(context) }
    val language = remember { userPreferences.getLanguage() ?: "en" }

    // Text to Speech Initialization
    val tts = remember {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set language based on app preference immediately
                if (language == "in") {
                    textToSpeech?.language = Locale("id", "ID")
                } else {
                    textToSpeech?.language = Locale.US
                }
                
                val voices = textToSpeech?.voices
                val targetLang = if (language == "in") "id" else "en"
                
                val maleVoice = voices?.find { voice ->
                    voice.locale.language == targetLang &&
                            (voice.name.lowercase().contains("male") || voice.name.lowercase().contains("man"))
                }

                if (maleVoice != null) {
                    textToSpeech?.voice = maleVoice
                } else {
                    val backupVoices = voices?.filter { it.locale.language == targetLang }
                    if (!backupVoices.isNullOrEmpty() && backupVoices.size > 1) {
                        textToSpeech?.voice = backupVoices[1]
                    }
                }

                textToSpeech?.setPitch(0.9f)
                textToSpeech?.setSpeechRate(1.0f)
                
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { isSpeaking = true }
                    override fun onDone(utteranceId: String?) { isSpeaking = false }
                    override fun onError(utteranceId: String?) { isSpeaking = false }
                    override fun onStop(utteranceId: String?, interrupted: Boolean) { isSpeaking = false }
                })
            }
        }
        textToSpeech
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(imageUri) {
        if (!hasStartedAnalysis) {
            withContext(Dispatchers.IO) {
                try {
                    val uriStrings = imageUri.split(",")
                    val loadedBitmaps = mutableListOf<Bitmap>()
                    
                    uriStrings.forEach { uriStr ->
                        val decodedUri = try {
                            URLDecoder.decode(uriStr.trim(), StandardCharsets.UTF_8.toString())
                        } catch (e: Exception) {
                            uriStr.trim()
                        }
                        val uri = Uri.parse(decodedUri)
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)?.let { 
                                loadedBitmaps.add(it)
                            }
                        }
                    }
                    
                    if (loadedBitmaps.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            bitmaps.addAll(loadedBitmaps)
                            viewModel.analyzePicture(loadedBitmaps, imageUri = imageUri) { finalResult ->
                                isSafe = if (finalResult.contains("CONCLUSION", ignoreCase = true)) {
                                    finalResult.contains("SAFE", ignoreCase = true) && !finalResult.contains("NOT SAFE", ignoreCase = true)
                                } else {
                                    finalResult.contains("AMAN", ignoreCase = false) && !finalResult.contains("TIDAK AMAN", ignoreCase = false)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AnalysisResult", "Failed to load bitmaps", e)
                }
            }
            hasStartedAnalysis = true
        }
    }

    if (isAnalyzing && analysisResult.isEmpty()) {
        LoadingView()
    } else {
        ResultContent(navController, bitmaps, isSafe, analysisResult, tts, isAnalyzing, isSpeaking, language)
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
                text = stringResource(R.string.loading_gemma_thinking),
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
fun ResultContent(
    navController: NavController, 
    bitmaps: List<Bitmap>, 
    isSafe: Boolean?,
    analysisResult: String,
    tts: TextToSpeech?,
    isAnalyzing: Boolean,
    isSpeaking: Boolean,
    language: String
) {
    val scrollState = rememberScrollState()
    
    // Typewriter effect state
    var displayedText by remember { mutableStateOf("") }
    var hasSpokenAutomatically by remember { mutableStateOf(false) }
    
    // Typewriter animation
    LaunchedEffect(analysisResult) {
        if (analysisResult.length > displayedText.length) {
            val newText = analysisResult.substring(displayedText.length)
            for (char in newText) {
                displayedText += char
                delay(if (char == '\n') 40L else 8L) 
            }
        }
    }

    // Helper for speaking based on app language preference
    fun speakWithAppLanguage(text: String) {
        val cleanText = text.replace("*", "")
        
        if (language == "in") {
            tts?.setLanguage(Locale("id", "ID"))
        } else {
            tts?.setLanguage(Locale.US)
        }
        
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "analysis_utterance_id")
    }

    // Trigger voice ONLY when analysis is completely finished
    LaunchedEffect(isAnalyzing, displayedText) {
        if (!isAnalyzing && analysisResult.isNotEmpty() && !hasSpokenAutomatically) {
            if (displayedText.length >= analysisResult.length) {
                speakWithAppLanguage(analysisResult)
                hasSpokenAutomatically = true
            }
        }
    }
    
    // Auto-scroll logic
    LaunchedEffect(displayedText) {
        if (displayedText.isNotEmpty() && isSafe == null) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
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
            // Header Banner
            val bannerColor = when(isSafe) {
                true -> PrimaryColor
                false -> Color(0xFFFF5252)
                null -> Color(0xFF9E9E9E)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
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
                            imageVector = when(isSafe) {
                                true -> Icons.Default.CheckCircle
                                false -> Icons.Default.Warning
                                null -> Icons.Default.Search
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when(isSafe) {
                            true -> stringResource(R.string.status_safe)
                            false -> stringResource(R.string.status_danger)
                            null -> stringResource(R.string.status_analyzing)
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = when(isSafe) {
                            true -> stringResource(R.string.no_allergens_detected)
                            false -> stringResource(R.string.severe_allergen_detected)
                            null -> stringResource(R.string.please_wait)
                        },
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
                            text = stringResource(R.string.product_analysis_result),
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
            val boxBgColor = when(isSafe) {
                true -> Color(0xFFE0F2F1)
                false -> Color(0xFFFBE9E7)
                null -> Color(0xFFF5F5F5)
            }
            val boxTextColor = when(isSafe) {
                true -> PrimaryColor
                false -> Color(0xFFD32F2F)
                null -> Color.Gray
            }

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
                                text = stringResource(R.string.ai_safety_analysis),
                                fontWeight = FontWeight.Bold,
                                color = boxTextColor,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        MarkdownText(
                            markdown = displayedText.ifEmpty { stringResource(R.string.reading_labels) },
                            style = TextStyle(
                                color = boxTextColor,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }

                    // --- STOP/PLAY BUTTON ---
                    if (analysisResult.isNotEmpty() && !isAnalyzing) {
                        IconButton(
                            onClick = { 
                                if (isSpeaking) {
                                    tts?.stop()
                                } else {
                                    speakWithAppLanguage(analysisResult)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(boxTextColor.copy(alpha = 0.1f), CircleShape).size(25.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) EvaIcons.Fill.StopCircle else EvaIcons.Fill.PlayCircle,
                                contentDescription = "Speech Control",
                                tint = boxTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Photo Preview(s)
            Text(stringResource(R.string.captured_photo), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            bitmaps.forEach { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 4.dp)
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
                        .height(40.dp)
                        .background(Color(0xFF80CBC4))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.scan_metadata), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(
                        text = stringResource(R.string.processing_info),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    enabled = isSafe != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when(isSafe) {
                            true -> PrimaryColor
                            false -> Color(0xFF00897B)
                            null -> Color.Gray
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val btnText = if (isSafe != null) stringResource(R.string.back_to_home) else stringResource(R.string.initializing)
                    Text(btnText, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        navController.navigate(Routes.CHAT) {
                            popUpTo("${Routes.ANALYSIS_RESULT}/{imageUri}") { inclusive = true }
                        }
                    },
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
                        Text(stringResource(R.string.ask_gemma_details), color = Color(0xFF0277BD))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
