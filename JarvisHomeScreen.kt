package com.example.jarvis.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.data.MessageEntity
import com.example.jarvis.ui.components.ArcReactor
import com.example.jarvis.ui.components.HologramCard
import com.example.jarvis.ui.components.ReactorState
import com.example.jarvis.ui.components.WaveformVisualizer
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArcBlue
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.HologramBorder
import com.example.ui.theme.HologramSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.ReactorGold
import com.example.ui.theme.ReactorNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisHomeScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onRequestRecordAudioPermission: () -> Unit,
    hasRecordAudioPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Auto scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid),
        containerColor = ObsidianVoid,
        topBar = {
            JarvisTopBar(
                batteryPercentage = uiState.batteryInfo?.percentage ?: 0,
                isCharging = uiState.batteryInfo?.isCharging == true,
                isFlashlightOn = uiState.isFlashlightOn,
                isBackgroundActive = uiState.isBackgroundActive,
                onToggleFlashlight = { viewModel.toggleFlashlight() },
                onNavigateToSettings = onNavigateToSettings
            )
        },
        bottomBar = {
            JarvisBottomInputBar(
                inputText = uiState.inputText,
                isListening = uiState.isListening,
                isSpeaking = uiState.isSpeaking,
                onTextChanged = { viewModel.onInputTextChanged(it) },
                onSend = { viewModel.sendTextMessage() },
                onMicClick = {
                    if (!hasRecordAudioPermission) {
                        onRequestRecordAudioPermission()
                    } else {
                        if (uiState.isListening) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    }
                },
                onStopSpeaking = { viewModel.stopSpeaking() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Arc Reactor Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    ArcReactor(
                        state = uiState.reactorState,
                        audioLevel = uiState.audioLevel,
                        reactorSize = 140.dp,
                        onClick = {
                            if (!hasRecordAudioPermission) {
                                onRequestRecordAudioPermission()
                            } else {
                                if (uiState.isListening) {
                                    viewModel.stopListening()
                                } else {
                                    viewModel.startListening()
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status Telemetry Text
                    val statusText = when (uiState.reactorState) {
                        ReactorState.LISTENING -> "● LISTENING FOR COMMAND..."
                        ReactorState.PROCESSING -> "● ANALYZING DIRECTIVE..."
                        ReactorState.SPEAKING -> "● VOCALIZING SYSTEM RESPONSE..."
                        ReactorState.IDLE -> "JARVIS ONLINE • TAP TO ENGAGE"
                    }

                    Text(
                        text = statusText,
                        color = when (uiState.reactorState) {
                            ReactorState.LISTENING -> ArcCyanGlow
                            ReactorState.PROCESSING -> ReactorGold
                            ReactorState.SPEAKING -> ArcBlue
                            ReactorState.IDLE -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    AnimatedVisibility(visible = uiState.isListening) {
                        WaveformVisualizer(
                            isListening = uiState.isListening,
                            audioLevel = uiState.audioLevel,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    uiState.speechError?.let { err ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AlertRed.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clickable { viewModel.launchSystemVoiceDialog() }
                        ) {
                            Text(
                                text = "⚠️ $err • Tap for voice popup",
                                color = AlertRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Quick Action Chips Row
            QuickActionsRow(
                onActionSelected = { command ->
                    viewModel.processCommand(command, isVoice = false)
                }
            )

            // Conversation Message Feed
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HologramCard(
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "J.A.R.V.I.S.",
                                color = ArcCyanGlow,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"Hello, how can I help you today, Sir?\"",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Speak or type in English, Hindi, or Hinglish. Try saying 'Battery kitni hai?', 'Open Instagram', 'Flashlight on', or 'Read notifications'.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("JARVIS Response", msg.text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onReplaySpeech = {
                                viewModel.speakText(msg.text)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JarvisTopBar(
    batteryPercentage: Int,
    isCharging: Boolean,
    isFlashlightOn: Boolean,
    isBackgroundActive: Boolean,
    onToggleFlashlight: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(DeepSpace)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & System Status
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "JARVIS AI",
                    color = ArcCyanGlow,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = if (isBackgroundActive) "BG ACTIVE" else "STANDBY",
                    color = if (isBackgroundActive) ArcCyan else TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Action Icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Battery Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ReactorNavy,
                border = BorderStroke(1.dp, HologramBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                        contentDescription = "Battery Status",
                        tint = if (isCharging) SuccessGreen else ArcCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$batteryPercentage%",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Flashlight Toggle
            IconButton(
                onClick = onToggleFlashlight,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isFlashlightOn) ArcCyan.copy(alpha = 0.25f) else ReactorNavy)
                    .testTag("flashlight_toggle_button")
            ) {
                Icon(
                    imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = "Toggle Torch",
                    tint = if (isFlashlightOn) ArcCyanGlow else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Settings
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ReactorNavy)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onActionSelected: (String) -> Unit
) {
    val quickActions = listOf(
        "⚡ Battery" to "Battery kitni hai?",
        "🔦 Flashlight" to "Flashlight on",
        "📸 Camera" to "Open Camera",
        "📷 Instagram" to "Open Instagram",
        "▶️ YouTube" to "Open YouTube",
        "💬 WhatsApp" to "Open WhatsApp",
        "🔔 Notifications" to "Read notifications",
        "📱 Device Info" to "Phone ka model batao",
        "⏰ 10m Reminder" to "10 minute baad reminder dena",
        "🌐 Google Maps" to "Open Maps"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for ((label, command) in quickActions) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ReactorNavy,
                border = BorderStroke(1.dp, HologramBorder),
                modifier = Modifier.clickable { onActionSelected(command) }
            ) {
                Text(
                    text = label,
                    color = ArcCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageEntity,
    onCopy: () -> Unit,
    onReplaySpeech: () -> Unit
) {
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) HologramSurface else ReactorNavy
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isUser) ArcCyan.copy(alpha = 0.5f) else HologramBorder
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.92f)
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ArcCyan)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    // Header tag for JARVIS
                    if (!isUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ArcCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "JARVIS",
                                    color = ArcCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                message.actionType?.let { action ->
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = DeepSpace
                                    ) {
                                        Text(
                                            text = action,
                                            color = ReactorGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Copy & TTS Buttons
                            Row {
                                IconButton(
                                    onClick = onReplaySpeech,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Read aloud",
                                        tint = ArcCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy message",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun JarvisBottomInputBar(
    inputText: String,
    isListening: Boolean,
    isSpeaking: Boolean,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onStopSpeaking: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = DeepSpace,
        border = BorderStroke(1.dp, HologramBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Input
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Listening..." else "Ask or command JARVIS...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArcCyan,
                    unfocusedBorderColor = HologramBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ArcCyan,
                    focusedContainerColor = ReactorNavy,
                    unfocusedContainerColor = ReactorNavy
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Stop Speaking button if active
            if (isSpeaking) {
                IconButton(
                    onClick = onStopSpeaking,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AlertRed)
                        .testTag("stop_speaking_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop speech",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Mic or Send Button
            if (inputText.isNotBlank()) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ArcCyan)
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Command",
                        tint = ObsidianVoid,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Brush.radialGradient(listOf(AlertRed, AlertRed.copy(alpha = 0.7f)))
                            else Brush.radialGradient(listOf(ArcCyan, ArcBlue))
                        )
                        .testTag("mic_voice_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = ObsidianVoid,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
