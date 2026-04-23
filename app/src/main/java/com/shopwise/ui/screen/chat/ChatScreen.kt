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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.TextStyle
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
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    var textState by remember { mutableStateOf("") }
    val selectedBitmaps = remember { mutableStateListOf<Bitmap>() }
    val listState = rememberLazyListState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            decodeUri(context, uri)?.let { selectedBitmaps.add(it) }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { selectedBitmaps.add(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

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
                        "ShopWise AI Expert",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
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
                if (selectedBitmaps.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(selectedBitmaps) { bitmap ->
                            Box(modifier = Modifier.size(90.dp)) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Image Preview",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { selectedBitmaps.remove(bitmap) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-8).dp)
                                        .size(24.dp)
                                        .background(Color.Red, CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(14.dp), tint = Color.White)
                                }
                            }
                        }
                    }
                }
                ChatBottomBar(
                    textValue = textState,
                    onValueChange = { textState = it },
                    onSend = {
                        viewModel.sendMessage(textState, context, selectedBitmaps.toList())
                        textState = ""
                        selectedBitmaps.clear()
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
        containerColor = Color(0xFFFBFBFB),
        modifier = Modifier.navigationBarsPadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewModel.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE3F2FD),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Ready to Analyze?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.Black
                        )
                        Text(
                            "Send images of food labels for instant analysis",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                items(viewModel.messages) { message ->
                    ChatBubble(message)
                }
                if (viewModel.isThinking) {
                    item { ThinkingAnimation() }
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column {
            Text(
                "SYSTEM",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                color = Color(0xFFEEEEEE)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Analyzing content...",
                        color = Color.DarkGray,
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
                                .background(Color.DarkGray.copy(alpha = dotAlpha))
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(3.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) PrimaryColor else Color.White
    val textColor = if (message.isUser) Color.White else Color.Black
    val elevation = if (message.isUser) 2.dp else 1.dp
    
    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!message.isUser) {
                Text(
                    "ASSISTANT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = currentTime,
                fontSize = 9.sp,
                color = Color.LightGray
            )
            if (message.isUser) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "ME",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 16.dp else 2.dp,
                topEnd = if (message.isUser) 2.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = bgColor,
            shadowElevation = elevation
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                message.images?.let { bitmaps ->
                    if (bitmaps.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            maxItemsInEachRow = 2
                        ) {
                            bitmaps.forEach { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Product Image",
                                    modifier = Modifier
                                        .sizeIn(maxWidth = 140.dp, maxHeight = 140.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Gray.copy(alpha = 0.1f)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
                if (message.isAudio) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(EvaIcons.Fill.Mic, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Voice clipping", fontSize = 14.sp, color = textColor)
                    }
                } else if (message.text.isNotEmpty()) {
                    MarkdownText(
                        markdown = message.text,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.clickable { /* TODO */ }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}

@Composable
fun VoiceRecordingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    Row(
        modifier = Modifier.height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(12) { index ->
            val heightScale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 350 + (index * 40), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
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
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRecording) {
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = EvaIcons.Fill.PlusCircle,
                            contentDescription = "Add content",
                            tint = PrimaryColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Gallery") },
                            onClick = { 
                                showMenu = false
                                onPickImage() 
                            },
                            leadingIcon = { Icon(EvaIcons.Fill.Image, contentDescription = null, tint = PrimaryColor) }
                        )
                        DropdownMenuItem(
                            text = { Text("Take Photo") },
                            onClick = { 
                                showMenu = false
                                onTakeFoto() 
                            },
                            leadingIcon = { Icon(EvaIcons.Fill.Camera, contentDescription = null, tint = PrimaryColor) }
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
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Listening...", color = PrimaryColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    TextField(
                        value = textValue,
                        onValueChange = onValueChange,
                        placeholder = { Text("Type a message or describe food...", color = Color.LightGray) },
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
                    contentDescription = "Voice input",
                    tint = if (isRecording) Color.Red else PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (!isRecording) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isSending || (textValue.isBlank() && !isSending)) Color.LightGray else PrimaryColor)
                        .clickable(enabled = !isSending && textValue.isNotBlank()) { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
