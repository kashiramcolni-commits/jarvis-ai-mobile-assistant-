package com.example.jarvis.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jarvis.ai.AiAssistantRepository
import com.example.jarvis.apps.AppLauncher
import com.example.jarvis.background.JarvisForegroundService
import com.example.jarvis.commands.CommandParser
import com.example.jarvis.commands.CommandRouter
import com.example.jarvis.commands.JarvisAction
import com.example.jarvis.contacts.CallController
import com.example.jarvis.contacts.ContactItem
import com.example.jarvis.contacts.ContactsRepository
import com.example.jarvis.data.ConversationDatabase
import com.example.jarvis.data.JarvisSettings
import com.example.jarvis.data.JarvisSettingsRepository
import com.example.jarvis.data.MessageEntity
import com.example.jarvis.device.BatteryController
import com.example.jarvis.device.BatteryInfo
import com.example.jarvis.device.DeviceInfoController
import com.example.jarvis.device.FlashlightController
import com.example.jarvis.device.VolumeController
import com.example.jarvis.notifications.JarvisNotification
import com.example.jarvis.notifications.JarvisNotificationListenerService
import com.example.jarvis.reminders.ReminderScheduler
import com.example.jarvis.ui.components.ReactorState
import com.example.jarvis.voice.SpeechRecognitionManager
import com.example.jarvis.voice.SpeechState
import com.example.jarvis.voice.TextToSpeechManager
import com.example.jarvis.voice.WakeWordManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val reactorState: ReactorState = ReactorState.IDLE,
    val inputText: String = "",
    val isListening: Boolean = false,
    val audioLevel: Float = 0f,
    val speechError: String? = null,
    val batteryInfo: BatteryInfo? = null,
    val isFlashlightOn: Boolean = false,
    val isBackgroundActive: Boolean = false,
    val isSpeaking: Boolean = false,
    val multipleContactsChoice: List<ContactItem>? = null,
    val liveNotifications: List<JarvisNotification> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ConversationDatabase.getDatabase(application)
    private val conversationDao = db.conversationDao()

    val messages: StateFlow<List<MessageEntity>> = conversationDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settingsRepository = JarvisSettingsRepository(application)
    val settings: StateFlow<JarvisSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JarvisSettings())

    // Device & System Controllers
    val batteryController = BatteryController(application)
    val flashlightController = FlashlightController(application)
    val volumeController = VolumeController(application)
    val deviceInfoController = DeviceInfoController(application)
    val appLauncher = AppLauncher(application)
    val contactsRepository = ContactsRepository(application)
    val callController = CallController(application, contactsRepository)
    val reminderScheduler = ReminderScheduler(application)
    val aiAssistantRepository = AiAssistantRepository(application)

    // Voice & Intelligence
    val wakeWordManager = WakeWordManager(application)
    val speechRecognitionManager = SpeechRecognitionManager(application)
    val textToSpeechManager = TextToSpeechManager(application)
    val commandParser = CommandParser(wakeWordManager)
    val commandRouter = CommandRouter(
        context = application,
        appLauncher = appLauncher,
        batteryController = batteryController,
        flashlightController = flashlightController,
        volumeController = volumeController,
        deviceInfoController = deviceInfoController,
        contactsRepository = contactsRepository,
        callController = callController,
        reminderScheduler = reminderScheduler,
        aiAssistantRepository = aiAssistantRepository
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshBattery()
        observeSpeechState()
        observeTorchState()
        observeNotifications()
        observeTtsSpeakingState()
        observeForegroundService()
    }

    private fun observeSpeechState() {
        viewModelScope.launch {
            speechRecognitionManager.speechState.collect { state ->
                when (state) {
                    is SpeechState.Idle -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            reactorState = if (_uiState.value.isSpeaking) ReactorState.SPEAKING else ReactorState.IDLE
                        )
                    }
                    is SpeechState.Ready, is SpeechState.Listening -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = true,
                            reactorState = ReactorState.LISTENING,
                            speechError = null
                        )
                    }
                    is SpeechState.Processing -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            reactorState = ReactorState.PROCESSING
                        )
                    }
                    is SpeechState.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            reactorState = ReactorState.PROCESSING
                        )
                        processCommand(state.recognizedText, isVoice = true)
                    }
                    is SpeechState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            speechError = state.message,
                            reactorState = ReactorState.IDLE
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            speechRecognitionManager.rmsAudioLevel.collect { level ->
                _uiState.value = _uiState.value.copy(audioLevel = level)
            }
        }
    }

    private fun observeTorchState() {
        viewModelScope.launch {
            flashlightController.isFlashlightOn.collect { on ->
                _uiState.value = _uiState.value.copy(isFlashlightOn = on)
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            JarvisNotificationListenerService.notificationsFlow.collect { notifs ->
                _uiState.value = _uiState.value.copy(liveNotifications = notifs)
            }
        }
    }

    private fun observeTtsSpeakingState() {
        viewModelScope.launch {
            textToSpeechManager.isSpeaking.collect { speaking ->
                _uiState.value = _uiState.value.copy(
                    isSpeaking = speaking,
                    reactorState = if (speaking) ReactorState.SPEAKING else (if (_uiState.value.isListening) ReactorState.LISTENING else ReactorState.IDLE)
                )
            }
        }
    }

    private fun observeForegroundService() {
        viewModelScope.launch {
            JarvisForegroundService.isRunning.collect { running ->
                _uiState.value = _uiState.value.copy(isBackgroundActive = running)
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun refreshBattery() {
        _uiState.value = _uiState.value.copy(batteryInfo = batteryController.getBatteryInfo())
    }

    fun toggleFlashlight() {
        flashlightController.toggleFlashlight()
    }

    fun toggleBackgroundAssistant() {
        val app = getApplication<Application>()
        if (_uiState.value.isBackgroundActive) {
            JarvisForegroundService.stopService(app)
        } else {
            JarvisForegroundService.startService(app)
        }
    }

    private val _voiceDialogTrigger = kotlinx.coroutines.flow.MutableSharedFlow<android.content.Intent>(extraBufferCapacity = 1)
    val voiceDialogTrigger: kotlinx.coroutines.flow.SharedFlow<android.content.Intent> = _voiceDialogTrigger

    fun startListening() {
        val lang = settings.value.language
        textToSpeechManager.stop()
        speechRecognitionManager.startListening(lang) {
            // Trigger system voice dialog fallback
            val fallbackIntent = speechRecognitionManager.buildSpeechRecognizerIntent(lang)
            _voiceDialogTrigger.tryEmit(fallbackIntent)
        }
    }

    fun launchSystemVoiceDialog() {
        val lang = settings.value.language
        textToSpeechManager.stop()
        val intent = speechRecognitionManager.buildSpeechRecognizerIntent(lang)
        _voiceDialogTrigger.tryEmit(intent)
    }

    fun stopListening() {
        speechRecognitionManager.stopListening()
    }

    fun stopSpeaking() {
        textToSpeechManager.stop()
    }

    fun speakText(text: String) {
        textToSpeechManager.speak(text, settings.value.language)
    }

    fun sendTextMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        _uiState.value = _uiState.value.copy(inputText = "")
        processCommand(text, isVoice = false)
    }

    fun processCommand(commandText: String, isVoice: Boolean = false) {
        viewModelScope.launch {
            // Save User message
            conversationDao.insertMessage(
                MessageEntity(
                    sender = "USER",
                    text = commandText,
                    isVoice = isVoice
                )
            )

            _uiState.value = _uiState.value.copy(reactorState = ReactorState.PROCESSING)

            val parsedAction = commandParser.parse(commandText)
            val isHindi = settings.value.language == "HI" ||
                    commandText.any { it in '\u0900'..'\u097F' } ||
                    commandText.contains("kholo") || commandText.contains("karo") ||
                    commandText.contains("hai") || commandText.contains("kitni")

            val result = commandRouter.executeAction(parsedAction, isHindiPreferred = isHindi)

            // Save JARVIS reply
            conversationDao.insertMessage(
                MessageEntity(
                    sender = "JARVIS",
                    text = result.displayText,
                    actionType = result.actionName,
                    actionPayload = result.spokenResponse,
                    isVoice = isVoice
                )
            )

            // Speak response if voice interaction or always
            textToSpeechManager.speak(result.spokenResponse, settings.value.language)

            // Update UI state with action side-effects
            refreshBattery()
            _uiState.value = _uiState.value.copy(
                reactorState = if (textToSpeechManager.isSpeaking.value) ReactorState.SPEAKING else ReactorState.IDLE
            )
        }
    }

    fun dismissContactChoice() {
        _uiState.value = _uiState.value.copy(multipleContactsChoice = null)
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            conversationDao.clearAllMessages()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.stopListening()
        textToSpeechManager.shutdown()
    }
}
