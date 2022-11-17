/**
 *    Copyright 2007-2022 Home Credit Xinchi Consulting Co. Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hcxc.securekeyboard

import android.content.Context
import android.keyboard.Keyboard
import android.keyboard.KeyboardView
import android.text.Editable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hcxc.securekeyboard.SecureKeyboardUtils.generateKeyboard
import com.hcxc.securekeyboard.SecureKeyboardUtils.generateNumberKeyboard
import com.hcxc.securekeyboard.SecureKeyboardUtils.generateSymbolKeyboard
import com.hcxc.securekeyboard.SecureKeyboardUtils.getLengthLimit
import com.hcxc.securekeyboard.SecureKeyboardUtils.toLowerCase
import com.hcxc.securekeyboard.SecureKeyboardUtils.toUpperCase
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class SecureKeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0
) : LinearLayout(
    context,
    attrs,
    defStyleAttr
), OnFocusChangeListener {
    private var keyboardView: KeyboardView? = null
    private var editTextRef: WeakReference<EditText>? = null
    private var isCapitalized = false
    private var maxLength = Int.MAX_VALUE
    private var showAnimation = AnimationUtils.loadAnimation(context, R.anim.keyboard_in)
    private var hideAnimation = AnimationUtils.loadAnimation(context, R.anim.keyboard_out)
    private var popupWindow: PopupWindow? = null
    private var hostViewRef: WeakReference<View>? = null
    private val helper = SecureEncryptHelper()
    private var isShuffle: Boolean = false // Whether shuffle keyboard
    private var isHideCancel = AtomicBoolean(false)
    private var isSymbol = false
    private val normalKeyboard = Keyboard(
        context, R.xml.keyboard
    )
    private val symbolKeyboard = Keyboard(
        context, R.xml.keyboard_symbol
    )
    private val numberKeyboard = Keyboard(
        context, R.xml.keyboard_number
    )

    private val hideAnimationListener: Animation.AnimationListener =
        object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isHideCancel.getAndSet(false)
            }

            override fun onAnimationEnd(animation: Animation) {
                if (isHideCancel.compareAndSet(false, false)) {
                    popupWindow?.dismiss()
                } else {
                    showKeyboard()
                }
                keyboardView?.let {
                    if (isSymbol) {
                        it.keyboard = symbolKeyboard
                        generateSymbolKeyboard(it.keyboard, isShuffle, helper)
                    } else {
                        it.keyboard = normalKeyboard
                        generateKeyboard(it.keyboard, isShuffle, helper)
                    }
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

    private val keyboardActionListener: KeyboardView.OnKeyboardActionListener =
        object : KeyboardView.OnKeyboardActionListener {
            override fun onPress(i: Int) {}
            override fun onRelease(i: Int) {}
            override fun onKey(primaryCode: Int, keyCodes: IntArray) {
                val plaintext: Editable = editTextRef?.get()?.text ?: return
                val pos = editTextRef?.get()?.selectionStart ?: 0
                if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                    if (isCapitalized) {
                        isCapitalized = false
                        keyboardView?.keyboard = toLowerCase(keyboardView?.keyboard, helper)
                    } else {
                        isCapitalized = true
                        keyboardView?.keyboard = toUpperCase(keyboardView?.keyboard, helper)
                    }
                } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                    if (plaintext.isNotEmpty()) {
                        if (pos > 0) {
                            plaintext.delete(pos - 1, pos)
                        }
                    }
                    if (editTextRef?.get() != null) {
                        editTextRef?.get()?.text = plaintext
                        if (pos > 0) {
                            editTextRef?.get()?.setSelection(pos - 1)
                        }
                    }
                } else {
                    if (plaintext.length <= maxLength) {
                        var code = primaryCode
                        if (primaryCode == -7) {
                            code = 32
                        }
                        if (primaryCode == -11) {
                            code = 46
                        }
                        plaintext.insert(pos, code.toChar().toString())
                        if (editTextRef?.get() != null) {
                            editTextRef?.get()?.text = plaintext
                            editTextRef?.get()
                                ?.setSelection(if (plaintext.length == pos) pos else pos + 1)
                        }
                    }
                }
            }

            override fun onText(charSequence: CharSequence) {}
            override fun swipeLeft() {}
            override fun swipeRight() {}
            override fun swipeDown() {}
            override fun swipeUp() {}
        }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            showKeyboard()
        } else {
            hideKeyboard()
        }
    }

    private fun showKeyboard() {
        if (hideAnimation.hasStarted() && !hideAnimation.hasEnded()) {
            isHideCancel.getAndSet(true)
            hideAnimation.cancel()
        } else if (popupWindow?.isShowing != true && hostViewRef?.get() != null && hostViewRef?.get()?.windowToken != null) {
            popupWindow?.showAtLocation(hostViewRef?.get(), Gravity.BOTTOM, 0, 0)
            startAnimation(showAnimation)
        } else {
            editTextRef?.get()?.clearFocus()
        }
    }

    private fun hideKeyboard() {
        if (popupWindow?.isShowing == true && !(hideAnimation.hasStarted() && !hideAnimation.hasEnded())) {
            startAnimation(hideAnimation)
            editTextRef?.get()?.clearFocus()
        }
    }

    private fun initKeyboard() {
        LayoutInflater.from(context).inflate(R.layout.view_secure_keyboard, this, true)
        setBackgroundColor(ContextCompat.getColor(context, R.color.keyboard_background))
        findViewById<View>(R.id.keyboard_done).setOnClickListener {
            hideKeyboard()
        }
        findViewById<KeyboardView>(R.id.secure_keyboard)?.let {
            keyboardView = it
            it.setEncryptHelper(helper)
            if (isSymbol) {
                it.keyboard = symbolKeyboard
                generateSymbolKeyboard(it.keyboard, isShuffle, helper)
            } else {
                it.keyboard = normalKeyboard
                generateKeyboard(it.keyboard, isShuffle, helper)
            }
            it.setOnKeyboardActionListener(keyboardActionListener)
        }
        findViewById<TextView>(R.id.keyboard_symbol).setOnClickListener {
            if (isSymbol) {
                (it as TextView).setText(R.string.keyboard_symbol)
            } else {
                (it as TextView).setText(R.string.keyboard_abc)
            }
            isSymbol = !isSymbol
            keyboardView?.run {
                if (isSymbol) {
                    keyboard = symbolKeyboard
                    generateSymbolKeyboard(keyboard, isShuffle, helper)
                } else {
                    keyboard = normalKeyboard
                    generateKeyboard(keyboard, isShuffle, helper)
                }
            }
        }


        findViewById<TextView>(R.id.keyboard_number).setOnClickListener {
            (it as TextView).setText(R.string.keyboard_123)

            keyboardView?.run {

                if (keyboard != numberKeyboard) {
                    keyboard = numberKeyboard
                    generateNumberKeyboard(keyboard, isShuffle, helper)
                }

            }
        }
    }

    fun init(editText: EditText, popupWindow: PopupWindow, shuffle: Boolean) {
        this.popupWindow = popupWindow
        isShuffle = shuffle
        editTextRef = WeakReference(editText)
        hostViewRef = WeakReference(editText.rootView)
        maxLength = getLengthLimit(editText)
        hideAnimation.setAnimationListener(hideAnimationListener)
        initKeyboard()
        if (editText.hasFocus()) {
            showKeyboard()
        }
        editText.onFocusChangeListener = this
    }

    class Builder(private val context: Context) {
        private var shuffle: Boolean = true
        private var title: String = context.getString(R.string.keyboard_title)
        private var done: String = context.getString(R.string.keyboard_done)
        private lateinit var editText: EditText

        fun setShuffle(shuffle: Boolean?): Builder {
            shuffle?.let { this.shuffle = it }
            return this
        }

        fun setTitleText(title: String?): Builder {
            if (TextUtils.isEmpty(title)) {
                return this
            }
            this.title = title!!
            return this
        }

        fun setDoneText(done: String?): Builder {
            if (TextUtils.isEmpty(done)) {
                return this
            }
            this.done = done!!
            return this
        }

        fun setEditText(editText: EditText): Builder {
            this.editText = editText
            return this
        }

        fun build() {
            val contentView = LayoutInflater.from(context)
                .inflate(R.layout.view_secure_popup, LinearLayout(context))
            val secureKeyboard =
                contentView.findViewById<SecureKeyboardView>(R.id.secure_keyboard_view)
            val popupWindow = PopupWindow(
                contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            secureKeyboard.init(editText, popupWindow, shuffle)
            secureKeyboard.findViewById<TextView>(R.id.keyboard_title).text = title
            secureKeyboard.findViewById<TextView>(R.id.keyboard_done).text = done
        }
    }
}