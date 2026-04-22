package com.shopwise.ui.screen.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shopwise.R
import com.shopwise.ui.theme.PrimaryColor
import compose.icons.EvaIcons
import compose.icons.evaicons.Fill
import compose.icons.evaicons.fill.Camera
import compose.icons.evaicons.fill.Image
import compose.icons.evaicons.fill.Mic
import compose.icons.evaicons.fill.PlusCircle
import compose.icons.evaicons.fill.StopCircle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    var textState by remember { mutableStateOf("") }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val listState = rememberLazyListState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedBitmap = decodeUri(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { selectedBitmap = it }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

    // Scroll to bottom when new messages arrive or when AI starts thinking
    LaunchedEffect(viewModel.messages.size, viewModel.isThinking) {
        if (viewModel.messages.isNotEmpty() || viewModel.isThinking) {
            listState.animateScrollToItem(
                if (viewModel.isThinking) viewModel.messages.size else viewModel.messages.size - 1
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Ask Gemma Expert",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column {
                selectedBitmap?.let { bitmap ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Preview",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            onClick = { selectedBitmap = null },
                            color = Color.Red,
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, "Hapus", modifier = Modifier.padding(2.dp), tint = Color.White)
                        }
                    }
                }
                ChatBottomBar(
                    textValue = textState,
                    onValueChange = { textState = it },
                    onSend = {
                        viewModel.sendMessage(textState, context, selectedBitmap)
                        textState = ""
                        selectedBitmap = null
                    },
                    isSending = viewModel.isSending,
                    isRecording = viewModel.isRecording,
                    onPickImage = { imagePickerLauncher.launch("image/*") },
                    onTakeFoto = { cameraLauncher.launch() },
                    onMicClick = {
                        if (viewModel.isRecording) {
                            viewModel.stopRecording()
                        } else {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (hasPermission) {
                                viewModel.startRecording()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }
                )
            }
        },
        containerColor = Color(0xFFF9F9F9),
        modifier = Modifier.navigationBarsPadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section
            if (viewModel.messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Ask Gemma Expert",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                    Text(
                        "Professional clinical insights powered by AI",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(viewModel.messages) { message ->
                    ChatBubble(message)
                }
                if (viewModel.isThinking) {
                    item { ThinkingAnimation() }
                }
            }

            // Quick Actions
            if (viewModel.messages.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionChip("Is this keto-friendly?")
                    QuickActionChip("Why is soy risky?")
                    QuickActionChip("Check safety")
                }
            }
        }
    }
}

@Composable
fun ThinkingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column {
            Text(
                "GEMMA",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color(0xFF1E88E5)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                color = Color(0xFFBBDEFB).copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Gemma is thinking",
                        color = Color(0xFF0D47A1),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    repeat(3) { index ->
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, delayMillis = index * 200),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dotAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D47A1).copy(alpha = dotAlpha))
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(3.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) Color(0xFFF1F1F1) else Color(0xFFBBDEFB).copy(alpha = 0.8f)
    val textColor = if (message.isUser) Color.Black else Color(0xFF0D47A1)
    
    val currentTime = remember {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = alignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!message.isUser) {
                Text(
                    "GEMMA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFF1E88E5)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = currentTime,
                fontSize = 10.sp,
                color = Color.Gray
            )
            if (message.isUser) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "YOU",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 20.dp else 4.dp,
                topEnd = if (message.isUser) 4.dp else 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = bgColor
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                message.image?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Chat Image",
                        modifier = Modifier
                            .sizeIn(maxWidth = 200.dp, maxHeight = 300.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (message.isAudio) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(EvaIcons.Fill.Mic, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Pesan Suara", fontSize = 14.sp, color = textColor)
                    }
                } else if (message.text.isNotEmpty()) {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F1F1),
        modifier = Modifier.clickable { /* TODO */ }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun VoiceRecordingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    Row(
        modifier = Modifier.height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(15) { index ->
            val heightScale by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 400 + (index * 50), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight"
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight(heightScale)
                    .background(PrimaryColor, CircleShape)
            )
        }
    }
}

@Composable
fun ChatBottomBar(
    textValue: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    isRecording: Boolean,
    onPickImage: () -> Unit,
    onTakeFoto: () -> Unit,
    onMicClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRecording) {
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = EvaIcons.Fill.PlusCircle,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Galeri") },
                            onClick = { 
                                showMenu = false
                                onPickImage() 
                            },
                            leadingIcon = { Icon(EvaIcons.Fill.Image, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Kamera") },
                            onClick = { 
                                showMenu = false
                                onTakeFoto() 
                            },
                            leadingIcon = { Icon(EvaIcons.Fill.Camera, contentDescription = null) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Box(modifier = Modifier.weight(1f)) {
                if (isRecording) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VoiceRecordingAnimation()
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Recording...", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    TextField(
                        value = textValue,
                        onValueChange = onValueChange,
                        placeholder = { Text("Ask Gemma anything...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
            
            IconButton(onClick = onMicClick) {
                Icon(
                    imageVector = if (isRecording) EvaIcons.Fill.StopCircle else EvaIcons.Fill.Mic,
                    contentDescription = null,
                    tint = if (isRecording) Color.Red else PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (!isRecording) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSending) Color.Gray else PrimaryColor)
                        .clickable(enabled = !isSending) { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

fun decodeUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
    } catch (e: Exception) { null }
}
