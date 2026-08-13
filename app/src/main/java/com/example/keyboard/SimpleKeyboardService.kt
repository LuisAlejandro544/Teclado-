package com.example.keyboard

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.keyboard.nativebridge.NativeKeyboardBridge
import com.example.keyboard.voice.VoiceRecognitionEngine
import com.example.keyboard.voice.VoiceState
import com.example.ui.theme.MyApplicationTheme

class SimpleKeyboardService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }
    private val store by lazy { ViewModelStore() }
    private val voiceEngine by lazy { VoiceRecognitionEngine(this) }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private var currentEditorActionId: Int = EditorInfo.IME_ACTION_NONE
    private var actionLabel: String? = null
    private var actionIconType: String = "enter"
    private var composingPrefix: String = ""

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        setupWindowDecorView()
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        setupWindowDecorView()
    }

    private fun setupWindowDecorView() {
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        return true
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onWindowShown() {
        super.onWindowShown()
        setupWindowDecorView()
    }

    override fun onCreateInputView(): View {
        setupWindowDecorView()
        val composeView = ComposeView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setViewTreeLifecycleOwner(this@SimpleKeyboardService)
            setViewTreeViewModelStoreOwner(this@SimpleKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@SimpleKeyboardService)
            setContent {
                MyApplicationTheme(darkTheme = false) {
                    val currentVoiceState by voiceEngine.state.collectAsState()

                    LaunchedEffect(currentVoiceState) {
                        val state = currentVoiceState
                        if (state is VoiceState.Transcribed && state.text.isNotBlank()) {
                            val ic = currentInputConnection
                            if (ic != null) {
                                composingPrefix = ""
                                ic.commitText("${state.text} ", 1)
                            }
                        }
                    }

                    KeyboardView(
                        modifier = Modifier.fillMaxWidth(),
                        imeActionLabel = actionLabel,
                        imeActionIconType = actionIconType,
                        voiceState = currentVoiceState,
                        onKeyAction = { action -> handleKeyAction(action) }
                    )
                }
            }
        }
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        setupWindowDecorView()
        composingPrefix = ""
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        setupWindowDecorView()
        if (lifecycleRegistry.currentState != Lifecycle.State.RESUMED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        composingPrefix = ""
        updateActionFromEditorInfo(info)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        voiceEngine.cancel()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        voiceEngine.cancel()
    }

    private fun updateActionFromEditorInfo(info: EditorInfo?) {
        if (info == null) {
            actionLabel = null
            actionIconType = "enter"
            return
        }

        val imeOptions = info.imeOptions
        val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
        currentEditorActionId = actionId

        when (actionId) {
            EditorInfo.IME_ACTION_SEARCH -> {
                actionLabel = null
                actionIconType = "search"
            }
            EditorInfo.IME_ACTION_SEND -> {
                actionLabel = null
                actionIconType = "send"
            }
            EditorInfo.IME_ACTION_DONE -> {
                actionLabel = null
                actionIconType = "done"
            }
            EditorInfo.IME_ACTION_GO -> {
                actionLabel = "Ir"
                actionIconType = "enter"
            }
            EditorInfo.IME_ACTION_NEXT -> {
                actionLabel = "Sig."
                actionIconType = "next"
            }
            else -> {
                actionLabel = null
                actionIconType = "enter"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceEngine.release()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }

    private fun handleKeyAction(action: KeyAction) {
        val ic = currentInputConnection ?: return
        when (action) {
            is KeyAction.StartVoiceInput -> {
                voiceEngine.startListening()
            }
            is KeyAction.StopVoiceInput -> {
                voiceEngine.stopListening(commit = true)
            }
            is KeyAction.CancelVoiceInput -> {
                voiceEngine.cancel()
            }
            is KeyAction.Text -> {
                if (action.text.length == 1 && (action.text[0].isLetterOrDigit() || action.text == "'")) {
                    composingPrefix += action.text
                } else {
                    composingPrefix = ""
                }
                ic.commitText(action.text, 1)
            }
            is KeyAction.InsertEmoji -> {
                composingPrefix = ""
                ic.commitText(action.emoji, 1)
            }
            is KeyAction.CommitSuggestion -> {
                if (composingPrefix.isNotEmpty()) {
                    ic.deleteSurroundingText(composingPrefix.length, 0)
                }
                ic.commitText("${action.word} ", 1)
                composingPrefix = ""
            }
            is KeyAction.Space -> {
                composingPrefix = ""
                ic.commitText(" ", 1)
            }
            is KeyAction.Backspace -> {
                if (composingPrefix.isNotEmpty()) {
                    composingPrefix = composingPrefix.dropLast(1)
                }
                val selectedText = ic.getSelectedText(0)
                if (selectedText.isNullOrEmpty()) {
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.commitText("", 1)
                }
            }
            is KeyAction.Enter -> {
                composingPrefix = ""
                if (currentEditorActionId != EditorInfo.IME_ACTION_NONE && currentEditorActionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ic.performEditorAction(currentEditorActionId)
                } else {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
            }
            is KeyAction.HideKeyboard -> {
                composingPrefix = ""
                voiceEngine.cancel()
                requestHideSelf(0)
            }
            else -> {}
        }
    }
}
