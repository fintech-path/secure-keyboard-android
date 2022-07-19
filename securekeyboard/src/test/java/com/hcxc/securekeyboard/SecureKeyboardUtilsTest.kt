package com.hcxc.securekeyboard

import android.content.Context
import android.keyboard.Keyboard
import android.os.IBinder
import android.text.InputFilter
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import io.mockk.*
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SecureKeyboardUtilsTest {
    @Test
    fun test_hideSystemKeyBoard() {
        val editText = mockk<EditText>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        val imm = mockk<InputMethodManager>(relaxed = true)
        every { context.getSystemService(any()) } returns imm
        every { editText.context } returns context
        SecureKeyboardUtils.hideSystemKeyBoard(editText)
        verify { editText.showSoftInputOnFocus = false }
        verify { editText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI }
    }

    @Test
    fun test_hideSystemKeyBoard_active() {
        val editText = mockk<EditText>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        val imm = mockk<InputMethodManager>(relaxed = true)
        val token = mockk<IBinder>(relaxed = true)
        every { context.getSystemService(any()) } returns imm
        every { editText.context } returns context
        every { imm.isActive } returns true
        every { editText.windowToken } returns token
        SecureKeyboardUtils.hideSystemKeyBoard(editText)
        verify { imm.hideSoftInputFromWindow(token, 0) }
        verify { editText.showSoftInputOnFocus = false }
    }

    @Test
    fun test_disableCopyPaste() {
        val editText = mockk<EditText>(relaxed = true)
        val callback = slot<ActionMode.Callback>()
        every { editText.customSelectionActionModeCallback = capture(callback) } just Runs
        SecureKeyboardUtils.disableCopyPaste(editText)
        assertFalse(callback.captured.onCreateActionMode(mockk(), mockk()))
        assertFalse(callback.captured.onPrepareActionMode(mockk(), mockk()))
        assertFalse(callback.captured.onActionItemClicked(mockk(), mockk()))
        callback.captured.onDestroyActionMode(mockk())
    }

    @Test
    fun test_disableSelection() {
        val editText = mockk<EditText>(relaxed = true)
        SecureKeyboardUtils.disableSelection(editText)
        verify { editText.isLongClickable = false }
        verify { editText.setTextIsSelectable(false) }
    }

    @Test
    fun test_getLengthLimit_filtersNull() {
        val editText = mockk<EditText>(relaxed = true)
        every { editText.filters } returns null
        val length = SecureKeyboardUtils.getLengthLimit(editText)
        assertEquals(Int.MAX_VALUE, length)
    }

    @Test
    fun test_getLengthLimit_filtersEmpty() {
        val editText = mockk<EditText>(relaxed = true)
        every { editText.filters } returns emptyArray<InputFilter>()
        val length = SecureKeyboardUtils.getLengthLimit(editText)
        assertEquals(Int.MAX_VALUE, length)
    }

    @Test
    fun test_getLengthLimit_filtersOther() {
        val editText = mockk<EditText>(relaxed = true)
        every { editText.filters } returns arrayOf(mockk())
        val length = SecureKeyboardUtils.getLengthLimit(editText)
        assertEquals(Int.MAX_VALUE, length)
    }

    @Test
    fun test_getLengthLimit() {
        val editText = mockk<EditText>(relaxed = true)
        val filter = mockk<InputFilter.LengthFilter>(relaxed = true)
        every { filter.max } returns 30
        every { editText.filters } returns arrayOf(filter)
        val length = SecureKeyboardUtils.getLengthLimit(editText)
        assertEquals(30, length)
    }

    @Test
    fun test_disableInsertion() {
        val editText = mockk<EditText>(relaxed = true)
        val event = mockk<MotionEvent>(relaxed = true)
        val listener = slot<View.OnTouchListener>()
        every { event.action } returns MotionEvent.ACTION_UP
        every { editText.setOnTouchListener(capture(listener)) } just Runs
        SecureKeyboardUtils.disableInsertion(editText)
        assertFalse(listener.captured.onTouch(null, event))
    }

    @Test
    fun test_disableInsertion_ationDown() {
        val editText = spyk(EditText(mockk()))
        val event = mockk<MotionEvent>(relaxed = true)
        val listener = slot<View.OnTouchListener>()
        every { event.action } returns MotionEvent.ACTION_DOWN
        every { editText.setOnTouchListener(capture(listener)) } just Runs
        SecureKeyboardUtils.disableInsertion(editText)
        assertFalse(listener.captured.onTouch(null, event))
    }

    @Test
    fun test_disableInsertion_ationDownException() {
        val editText = mockk<EditText>(relaxed = true)
        val event = mockk<MotionEvent>(relaxed = true)
        val listener = slot<View.OnTouchListener>()
        every { event.action } returns MotionEvent.ACTION_DOWN
        every { editText.setOnTouchListener(capture(listener)) } just Runs
        SecureKeyboardUtils.disableInsertion(editText)
        assertFalse(listener.captured.onTouch(null, event))
    }

    @Test
    fun test_toLowerCase() {
        val keyboard = mockk<Keyboard>(relaxed = true)
        val helper = mockk<SecureEncryptHelper>(relaxed = true)
        keyboard.mCihperText = "ls3di"
        every { helper.decrypt("ls3di") } returns "34K8S"
        every { helper.encrypt("34k8s") } returns "8sel3#"
        SecureKeyboardUtils.toLowerCase(keyboard, helper)
        assertEquals("8sel3#", keyboard.mCihperText)
    }

    @Test
    fun test_toUpperCase() {
        val keyboard = mockk<Keyboard>(relaxed = true)
        val helper = mockk<SecureEncryptHelper>(relaxed = true)
        keyboard.mCihperText = "ls3di"
        every { helper.decrypt("ls3di") } returns "34k8s"
        every { helper.encrypt("34K8S") } returns "8sel3#"
        SecureKeyboardUtils.toUpperCase(keyboard, helper)
        assertEquals("8sel3#", keyboard.mCihperText)
    }
    
    @Test
    fun test_generateKeyboard() {
        val keyboard = mockk<Keyboard>(relaxed = true)
        val helper = mockk<SecureEncryptHelper>(relaxed = true)
        SecureKeyboardUtils.generateKeyboard(keyboard, false, helper)
        verify { helper.encrypt("0123456789qwertyuiopasdfghjklzxcvbnm") }
    }

    @Test
    fun test_generateKeyboard_shuffle() {
        val keyboard = mockk<Keyboard>(relaxed = true)
        val helper = mockk<SecureEncryptHelper>(relaxed = true)
        val text = slot<String>()
        every { helper.encrypt(capture(text)) } returns ""
        SecureKeyboardUtils.generateKeyboard(keyboard, true, helper)
        Assert.assertNotEquals("0123456789qwertyuiopasdfghjklzxcvbnm", text.captured)
    }
}
