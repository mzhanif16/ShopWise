package com.shopwise.ui.screen.chat

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shopwise.R
import com.shopwise.core.GemmaManager
import com.shopwise.core.GemmaUtils
import com.shopwise.core.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val images: List<Bitmap>? = null,
    val isAudio: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ChatViewModel"
    private val gemmaManager: GemmaManager? = GemmaUtils.gemmaManager
    val messages = mutableStateListOf<ChatMessage>()
    var isSending by mutableStateOf(false)
    var isThinking by mutableStateOf(false)
    private var fullResponse = ""
    
    var isRecording by mutableStateOf(false)
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    init {
        // Initial message from Gemma

        val welcome =
            getApplication<Application>().getString(R.string.asisten_fisrt_chat)

        messages.add(
            ChatMessage(
                text = welcome,
                isUser = false
            )
        )
        observeGemmaResults()
    }

    private fun observeGemmaResults() {
        viewModelScope.launch {
            gemmaManager?.partialResults?.collect { (partialText, isDone) ->
                if (isThinking && partialText.isNotEmpty()) {
                    isThinking = false
                }
                
                fullResponse += partialText

                val displayOutput = fullResponse
                    .replace(Regex("<thought>.*?</thought>", RegexOption.DOT_MATCHES_ALL), "")
                    .replace("<thought>", "")
                    .replace("</thought>", "")
                    .replace("<start_of_turn>", "")
                    .replace("<end_of_turn>", "")
                    .trim()

                if (messages.isNotEmpty() && !messages.last().isUser) {
                    messages[messages.size - 1] = ChatMessage(displayOutput, false)
                } else if (displayOutput.isNotEmpty()) {
                    messages.add(ChatMessage(displayOutput, false))
                }

                if (isDone) {
                    isSending = false
                    isThinking = false
                    fullResponse = ""
                }
            }
        }
    }

    fun sendMessage(prompt: String, context: Context, bitmaps: List<Bitmap>? = null, audioBytes: ByteArray? = null) {
        if ((prompt.isBlank() && bitmaps.isNullOrEmpty() && audioBytes == null) || isSending || gemmaManager == null) return

        val userPreferences = UserPreferences(context)
        val userData = userPreferences.getUserData()
        val allergies = (userData["allergies"] as? Set<*>)?.joinToString(", ") ?: "None"
        val language = userPreferences.getLanguage() ?: "en"

        // Sesuaikan instruksi sistem berdasarkan bahasa agar AI merespon dalam bahasa yang sama
        val systemPrompt = if (language == "in") {
            """
                Anda adalah "ShopWise", asisten AI pakar nutrisi dan keamanan pangan.
                Pengguna memiliki alergi: $allergies.
                Jawab pertanyaan pengguna dengan akurat, ramah, dan singkat dalam Bahasa Indonesia.
            """.trimIndent()
        } else {
            """
                You are "ShopWise", an AI assistant specialized in nutrition and food safety. 
                User has allergies: $allergies. 
                Answer the user's question accurately, friendly, and concisely in English.
            """.trimIndent()
        }

        val finalPrompt = if (prompt.isNotBlank()) "$systemPrompt\n\nUser: $prompt" else systemPrompt

        if (audioBytes == null) {
            messages.add(ChatMessage(prompt, true, images = bitmaps))
        }
        
        isSending = true
        isThinking = true
        fullResponse = ""
        
        gemmaManager.generateResponse(finalPrompt, bitmaps = bitmaps, audioBytes = audioBytes)
    }


    @SuppressLint("MissingPermission")
    fun startRecording(context: Context) {
        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        val outputStream = ByteArrayOutputStream()
        audioRecord?.startRecording()
        isRecording = true
        
        val userPreferences = UserPreferences(context)
        val userData = userPreferences.getUserData()
        val allergies = (userData["allergies"] as? Set<*>)?.joinToString(", ") ?: "None"
        val language = userPreferences.getLanguage() ?: "en"
        val systemPrompt = if (language == "in") {
            """
                Anda adalah "ShopWise", asisten AI pakar nutrisi dan keamanan pangan.
                Pengguna memiliki alergi: $allergies.
                Jawab pertanyaan pengguna dengan akurat, ramah, dan dalam Bahasa Indonesia.
            """.trimIndent()
        } else {
            """
                You are "ShopWise", an AI assistant specialized in nutrition and food safety. 
                User has allergies: $allergies. 
                Answer the user's question accurately, friendly, and in English.
            """.trimIndent()
        }

        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(bufferSize)
            try {
                while (isActive && isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        outputStream.write(buffer, 0, read)
                    }
                }
            } finally {
                try {
                    audioRecord?.stop()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping audio record", e)
                }
                audioRecord?.release()
                audioRecord = null
            }

            val pcmData = outputStream.toByteArray()
            if (pcmData.isNotEmpty()) {
                val wavData = addWavHeader(pcmData, sampleRate)
                withContext(Dispatchers.Main) {
                    sendAudioMessage(wavData, systemPrompt)
                }
            }
        }
    }
    
    fun stopRecording() {
        isRecording = false
    }
    
    private fun sendAudioMessage(audioData: ByteArray, systemPrompt: String) {
        messages.add(ChatMessage("Voice Message", true, isAudio = true))
        isSending = true
        isThinking = true
        fullResponse = ""
        gemmaManager?.generateResponse(systemPrompt, null, audioData)
    }
    
    private fun addWavHeader(pcmData: ByteArray, sampleRate: Int): ByteArray {
        val header = ByteArray(44)
        val pcmDataSize = pcmData.size
        val wavFileSize = pcmDataSize + 44
        val channels = 1 // Mono
        val bitsPerSample: Short = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (wavFileSize and 0xff).toByte()
        header[5] = (wavFileSize shr 8 and 0xff).toByte()
        header[6] = (wavFileSize shr 16 and 0xff).toByte()
        header[7] = (wavFileSize shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = (bitsPerSample.toInt() shr 8 and 0xff).toByte()
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmDataSize and 0xff).toByte()
        header[41] = (pcmDataSize shr 8 and 0xff).toByte()
        header[42] = (pcmDataSize shr 16 and 0xff).toByte()
        header[43] = (pcmDataSize shr 24 and 0xff).toByte()

        return header + pcmData
    }
}
