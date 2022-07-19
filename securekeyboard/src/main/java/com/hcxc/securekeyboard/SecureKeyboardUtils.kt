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

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputFilter.LengthFilter
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.util.*
import android.keyboard.Keyboard
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import java.lang.StringBuilder

object SecureKeyboardUtils {

    fun hideSystemKeyBoard(editText: EditText) {
        val imm =
            editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }
        editText.showSoftInputOnFocus = false
        editText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
    }

    fun disableCopyPaste(editText: EditText) {
        editText.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
        }
    }

    fun disableSelection(editText: EditText) {
        editText.isLongClickable = false
        editText.setTextIsSelectable(false)
    }

    fun getLengthLimit(editText: EditText): Int {
        val inputFilters = editText.filters
        if (inputFilters != null) {
            for (filter in inputFilters) {
                if (filter is LengthFilter) {
                    return filter.max
                }
            }
        }
        return Int.MAX_VALUE
    }

    @SuppressLint("ClickableViewAccessibility")
    fun disableInsertion(editText: EditText) {
        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // setInsertionDisabled when user touches the view
                setInsertionDisabled(editText)
            }
            false
        }
    }

    /**
     * Xiaomi/OPPO forbidden copy paste
     * reflection android.widget.Editor do not popup copy paste dialog
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun setInsertionDisabled(editText: EditText) {
        try {
            val editorField = TextView::class.java.getDeclaredField("mEditor")
            editorField.isAccessible = true
            val editorObject = editorField[editText]
            val editorClass = Class.forName("android.widget.Editor")
            val mInsertionControllerEnabledField =
                editorClass.getDeclaredField("mInsertionControllerEnabled")
            mInsertionControllerEnabledField.isAccessible = true
            mInsertionControllerEnabledField[editorObject] = false
            val mSelectionControllerEnabledField =
                editorClass.getDeclaredField("mSelectionControllerEnabled")
            mSelectionControllerEnabledField.isAccessible = true
            mSelectionControllerEnabledField[editorObject] = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toLowerCase(keyboard: Keyboard?, helper: SecureEncryptHelper): Keyboard? {
        keyboard?:return null
        val builder = StringBuilder()
        val keys = decryptKeyboard(keyboard, helper)
        for (key in keys) {
            if (isUpperCaseLetter(key.toString())) {
                builder.append(key.toString().lowercase())
            } else {
                builder.append(key)
            }
        }
        encryptKeyboard(keyboard, builder.toString().toMutableList(), helper)
        return keyboard
    }

    fun toUpperCase(keyboard: Keyboard?, helper: SecureEncryptHelper): Keyboard? {
        keyboard?:return null
        val builder = StringBuilder()
        val keys = decryptKeyboard(keyboard, helper)
        for (key in keys) {
            if (isLowerCaseLetter(key.toString())) {
                builder.append(key.toString().uppercase())
            } else {
                builder.append(key)
            }
        }
        encryptKeyboard(keyboard, builder.toString().toMutableList(), helper)
        return keyboard
    }

    private fun isUpperCaseLetter(charSequence: CharSequence?): Boolean {
        if (charSequence == null || charSequence.length != 1) return false
        val c = charSequence[0]
        return c in 'A'..'Z'
    }

    private fun isLowerCaseLetter(charSequence: CharSequence?): Boolean {
        if (charSequence == null || charSequence.length != 1) return false
        val c = charSequence[0]
        return c in 'a'..'z'
    }

    fun generateKeyboard(keyboard: Keyboard, shuffle: Boolean, helper: SecureEncryptHelper) {
        val allLabels: MutableList<Char> = ArrayList<Char>().apply {
            addAll(mutableListOf('1', '2', '3', '4', '5', '6', '7', '8', '9','0').apply {
                if (shuffle)
                    shuffle()
            })
            addAll(mutableListOf(
                'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p',
                'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm'
            ).apply {
                if (shuffle)
                    shuffle()
            })
        }
        encryptKeyboard(keyboard, allLabels, helper)
    }

    fun generateSymbolKeyboard(keyboard: Keyboard, shuffle: Boolean, helper: SecureEncryptHelper) {
        val allLabels: MutableList<Char> = ArrayList<Char>().apply {
            addAll(mutableListOf(
                '&', '"', ';', '^', ',', '|', '$', '*', ':', '\'',
                '?', '{', '[', '~', '#', '}', '.', ']', '\\', '!', '(', '%', '-', '_', '+', '/',
                ')', '=', '<', '`', '>', '@'
            ).apply {
                if (shuffle)
                    shuffle()
            })
        }
        encryptKeyboard(keyboard, allLabels, helper)
    }

    private fun encryptKeyboard(keyboard: Keyboard, allLabels: MutableList<Char>, helper: SecureEncryptHelper) {
        String(allLabels.toCharArray()).let {
                helper.encrypt(it)
            }.let {
                keyboard.mCihperText = it
            }
    }

    fun decryptKeyboard(keyboard: Keyboard, helper: SecureEncryptHelper) : MutableList<Char> {
        return helper.decrypt(keyboard.mCihperText)
            .toMutableList()
    }
}