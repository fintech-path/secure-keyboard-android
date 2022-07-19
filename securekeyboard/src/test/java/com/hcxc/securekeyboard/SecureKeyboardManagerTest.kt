package com.hcxc.securekeyboard

import android.content.Context
import android.view.View
import android.widget.EditText
import io.mockk.*
import org.junit.Test

class SecureKeyboardManagerTest {
    @Test
    fun test_bindSecureEditText() {
        val editText = mockk<EditText>()
        val context = mockk<Context>()
        val view = mockk<View>()
        val builder = mockk<SecureKeyboardView.Builder>(relaxed = true)
        mockkObject(SecureKeyboardUtils)
        mockkConstructor(SecureKeyboardView.Builder::class)
        every {editText.context} returns context
        every {editText.rootView} returns view
        every {context.getString(any())} returns ""
        every {SecureKeyboardUtils.hideSystemKeyBoard(editText)} returns Unit
        every {SecureKeyboardUtils.disableCopyPaste(editText)} returns Unit
        every {SecureKeyboardUtils.disableSelection(editText)} returns Unit
        every {SecureKeyboardUtils.disableInsertion(editText)} returns Unit
        every {anyConstructed<SecureKeyboardView.Builder>().setEditText(editText)} returns builder
        every {anyConstructed<SecureKeyboardView.Builder>().setShuffle(true)} returns builder
        every {anyConstructed<SecureKeyboardView.Builder>().setTitleText("")} returns builder
        every {anyConstructed<SecureKeyboardView.Builder>().setDoneText("")} returns builder
        every {anyConstructed<SecureKeyboardView.Builder>().build()} returns Unit
        SecureKeyboardManager.bindSecureEditText(editText, true, "", "")
        verify {SecureKeyboardUtils.hideSystemKeyBoard(editText)}
        verify {SecureKeyboardUtils.disableCopyPaste(editText)}
        verify {SecureKeyboardUtils.disableSelection(editText)}
        verify {SecureKeyboardUtils.disableInsertion(editText)}
        unmockkConstructor(SecureKeyboardView.Builder::class) // If not unmockkConstructor, builder test will get error.
    }
}