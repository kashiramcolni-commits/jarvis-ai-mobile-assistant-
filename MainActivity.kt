package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.jarvis.ui.JarvisHomeScreen
import com.example.jarvis.ui.MainViewModel
import com.example.jarvis.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianVoid

enum class JarvisScreen {
    HOME,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val voiceTrigger = intent?.getBooleanExtra("EXTRA_VOICE_TRIGGER", false) ?: false

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(JarvisScreen.HOME) }

                var hasAudioPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                var hasContactsPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.READ_CONTACTS
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val audioPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasAudioPermission = isGranted
                    if (isGranted) {
                        viewModel.startListening()
                    }
                }

                val contactsPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasContactsPermission = isGranted
                }

                val notificationsPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }

                // System voice dialog fallback launcher
                val systemVoiceLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == RESULT_OK) {
                        val matches = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                        val spokenText = matches?.firstOrNull()
                        if (!spokenText.isNullOrBlank()) {
                            viewModel.processCommand(spokenText, isVoice = true)
                        }
                    }
                }

                // Collect voice fallback trigger
                LaunchedEffect(Unit) {
                    viewModel.voiceDialogTrigger.collect { voiceIntent ->
                        try {
                            systemVoiceLauncher.launch(voiceIntent)
                        } catch (e: Exception) {
                            // System voice intent not available
                        }
                    }
                }

                // Request notification permission on Android 13+
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    if (voiceTrigger && hasAudioPermission) {
                        viewModel.startListening()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianVoid)
                ) {
                    when (currentScreen) {
                        JarvisScreen.HOME -> {
                            JarvisHomeScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = { currentScreen = JarvisScreen.SETTINGS },
                                onRequestRecordAudioPermission = {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                },
                                hasRecordAudioPermission = hasAudioPermission
                            )
                        }

                        JarvisScreen.SETTINGS -> {
                            BackHandler {
                                currentScreen = JarvisScreen.HOME
                            }
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = JarvisScreen.HOME },
                                onRequestMicrophonePermission = {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                },
                                hasMicrophonePermission = hasAudioPermission,
                                onRequestContactsPermission = {
                                    contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                },
                                hasContactsPermission = hasContactsPermission
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshBattery()
    }
}
