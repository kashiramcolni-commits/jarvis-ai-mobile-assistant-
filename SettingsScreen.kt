package com.example.jarvis.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.notifications.JarvisNotificationListenerService
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArcBlue
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.HologramBorder
import com.example.ui.theme.HologramSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.ReactorNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onRequestMicrophonePermission: () -> Unit,
    hasMicrophonePermission: Boolean,
    onRequestContactsPermission: () -> Unit,
    hasContactsPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val isNotificationAccessGranted = JarvisNotificationListenerService.isNotificationAccessGranted(context)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid),
        containerColor = ObsidianVoid,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(DeepSpace)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArcCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "JARVIS CONFIGURATION",
                    color = ArcCyanGlow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Voice & Speech Synthesis Section
            SettingsSectionHeader(title = "VOICE & SPEECH SYNTHESIS", icon = Icons.Default.VolumeUp)

            Card(
                colors = CardDefaults.cardColors(containerColor = ReactorNavy),
                border = BorderStroke(1.dp, HologramBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Language selection
                    Text(
                        text = "Preferred Language",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageChip(
                            label = "Auto (Smart)",
                            selected = settings.language == "AUTO",
                            onClick = { scope.launch { viewModel.settingsRepository.updateLanguage("AUTO") } }
                        )
                        LanguageChip(
                            label = "Hindi (हिंदी)",
                            selected = settings.language == "HI",
                            onClick = { scope.launch { viewModel.settingsRepository.updateLanguage("HI") } }
                        )
                        LanguageChip(
                            label = "English",
                            selected = settings.language == "EN",
                            onClick = { scope.launch { viewModel.settingsRepository.updateLanguage("EN") } }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speech Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Speech Rate",
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${String.format("%.2f", settings.speechRate)}x",
                            color = ArcCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = settings.speechRate,
                        onValueChange = { newRate ->
                            scope.launch {
                                viewModel.settingsRepository.updateSpeechRate(newRate)
                                viewModel.textToSpeechManager.setSpeechRate(newRate)
                            }
                        },
                        valueRange = 0.6f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyanGlow,
                            activeTrackColor = ArcCyan,
                            inactiveTrackColor = HologramBorder
                        )
                    )

                    // Speech Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Speech Pitch",
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${String.format("%.2f", settings.speechPitch)}x",
                            color = ArcCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = settings.speechPitch,
                        onValueChange = { newPitch ->
                            scope.launch {
                                viewModel.settingsRepository.updateSpeechPitch(newPitch)
                                viewModel.textToSpeechManager.setSpeechPitch(newPitch)
                            }
                        },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyanGlow,
                            activeTrackColor = ArcCyan,
                            inactiveTrackColor = HologramBorder
                        )
                    )
                }
            }

            // 2. Background Assistant & Wake Word
            SettingsSectionHeader(title = "BACKGROUND PROTOCOLS", icon = Icons.Default.Speed)

            Card(
                colors = CardDefaults.cardColors(containerColor = ReactorNavy),
                border = BorderStroke(1.dp, HologramBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Background Assistant Service",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Keeps persistent notification with quick microphone, torch, and battery shortcuts even when minimized.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Switch(
                            checked = uiState.isBackgroundActive,
                            onCheckedChange = { viewModel.toggleBackgroundAssistant() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianVoid,
                                checkedTrackColor = ArcCyanGlow
                            )
                        )
                    }
                }
            }

            // 3. System Permissions & Diagnostics
            SettingsSectionHeader(title = "SYSTEM PERMISSIONS & HARDWARE", icon = Icons.Default.Security)

            Card(
                colors = CardDefaults.cardColors(containerColor = ReactorNavy),
                border = BorderStroke(1.dp, HologramBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PermissionStatusRow(
                        title = "Microphone Access",
                        subtitle = "Required for speech recognition and voice commands",
                        isGranted = hasMicrophonePermission,
                        onGrant = onRequestMicrophonePermission
                    )

                    PermissionStatusRow(
                        title = "Contacts Search",
                        subtitle = "Used to dial and find contacts by voice command",
                        isGranted = hasContactsPermission,
                        onGrant = onRequestContactsPermission
                    )

                    PermissionStatusRow(
                        title = "Notification Listener",
                        subtitle = "Allows JARVIS to read incoming device notifications",
                        isGranted = isNotificationAccessGranted,
                        onGrant = { JarvisNotificationListenerService.openNotificationAccessSettings(context) }
                    )
                }
            }

            // 4. Privacy & Security
            SettingsSectionHeader(title = "PRIVACY GUARD", icon = Icons.Default.Security)

            Card(
                colors = CardDefaults.cardColors(containerColor = ReactorNavy),
                border = BorderStroke(1.dp, HologramBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mask OTPs & 2FA Codes",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Prevents reading aloud one-time passwords and authentication codes from notifications.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = settings.privacyFilterOtps,
                            onCheckedChange = { checked ->
                                scope.launch { viewModel.settingsRepository.updatePrivacyFilterOtps(checked) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianVoid,
                                checkedTrackColor = ArcCyanGlow
                            )
                        )
                    }
                }
            }

            // 5. Data & Conversation Management
            SettingsSectionHeader(title = "DATA MANAGEMENT", icon = Icons.Default.DeleteSweep)

            Card(
                colors = CardDefaults.cardColors(containerColor = ReactorNavy),
                border = BorderStroke(1.dp, HologramBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear Chat History",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Permanently deletes all saved messages from local Room database.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        OutlinedButton(
                            onClick = { showClearHistoryDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                            border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f))
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }

            // 6. About JARVIS
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSpace),
                border = BorderStroke(1.dp, HologramBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "JARVIS AI v1.0",
                        color = ArcCyanGlow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Advanced Android Personal AI Assistant combining Google Gemini intelligence with Iron Man-inspired automated device execution.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Native Kotlin • Jetpack Compose • SpeechRecognizer • Room • Camera2 Torch • BatteryManager",
                        color = ArcCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Conversation History?", color = TextPrimary) },
            text = { Text("This will remove all saved chat messages from the device.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllConversations()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear All", color = AlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DeepSpace
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ArcCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = ArcCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) ArcCyan else HologramSurface,
        border = BorderStroke(1.dp, if (selected) ArcCyanGlow else HologramBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) ObsidianVoid else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isGranted) SuccessGreen else AlertRed,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        if (!isGranted) {
            Button(
                onClick = onGrant,
                colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Grant",
                    color = ObsidianVoid,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
