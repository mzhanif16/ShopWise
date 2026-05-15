package com.shopwise.core


import android.graphics.Bitmap
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.Contents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class GemmaManager() {
    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var isProcessing = false
    private var currentJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _partialResults = MutableSharedFlow<Pair<String, Boolean>>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val partialResults: SharedFlow<Pair<String, Boolean>> = _partialResults

    fun initialize(modelPath: String, useGpu: Boolean = false) {
        close()
        if (engine != null) return

        val backend = if (useGpu) Backend.GPU() else Backend.CPU()
        val visionBackend = if (useGpu) Backend.GPU() else  Backend.CPU()

        val engineConfig = EngineConfig(
            modelPath = modelPath,
            backend = backend,
            visionBackend = visionBackend,
            audioBackend = Backend.CPU()
        )

        try {
            engine = Engine(engineConfig).apply {
                initialize()
            }
            conversation = engine?.createConversation()
            Log.d("GemmaManager", "Engine initialized successfully with ${if (useGpu) "GPU" else "CPU"}")
        } catch (e: Exception) {
            Log.e("GemmaManager", "Failed to create engine", e)
            throw e
        }
    }

    fun generateResponse(prompt: String, bitmaps: List<Bitmap>? = null, audioBytes: ByteArray? = null) {
        stop()
        isProcessing = true

        currentJob = scope.launch {
            try {
                val contents = mutableListOf<Content>()

                // Tambahkan Audio jika ada (hanya jika model mendukung/multimodal)
                audioBytes?.let {
                    contents.add(Content.AudioBytes(it))
                }

                // Tambahkan Gambar jika ada (hanya jika model mendukung/multimodal)
                bitmaps?.forEach { bitmap ->
                    contents.add(Content.ImageBytes(bitmap.toPngByteArray()))
                }

                // Tambahkan Teks (Prompt)
                contents.add(Content.Text(prompt))

                val message = Message.user(Contents.of(contents))

                conversation?.sendMessageAsync(message)?.collect { result ->
                    if (isProcessing) {
                        val cleanedText = result.toString()
                        _partialResults.tryEmit(Pair(cleanedText, false))
                    }
                }

                if (isProcessing) {
                    _partialResults.tryEmit(Pair("", true))
                    isProcessing = false
                }
            } catch (e: Exception) {
                Log.e("GemmaManager", "Generation failed", e)
                _partialResults.tryEmit(Pair("Error: ${e.message}", true))
                isProcessing = false
            }
        }
    }

    fun stop() {
        isProcessing = false
        currentJob?.cancel()
        currentJob = null
    }

    private fun Bitmap.toPngByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
    }

    fun close() {
        stop()
        conversation?.close()
        engine?.close()
        conversation = null
        engine = null
    }
}
