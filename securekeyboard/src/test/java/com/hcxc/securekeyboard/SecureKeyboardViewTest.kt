package com.hcxc.securekeyboard

import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.keyboard.KeyboardView
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class SecureKeyboardViewTest {
    @MockK(relaxed = true)
    lateinit var mContext: Context

    @MockK(relaxed = true)
    lateinit var mResources: Resources

    @MockK(relaxed = true)
    lateinit var mParser: XmlResourceParser

    @MockK(relaxed = true)
    lateinit var mShowAnimation: Animation

    @MockK(relaxed = true)
    lateinit var mHideAnimation: Animation

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { mContext.resources } returns mResources
        every { mResources.getXml(any()) } returns mParser
        every { mParser.next() } returns XmlResourceParser.END_DOCUMENT
        mockkStatic(AnimationUtils::class)
        every { AnimationUtils.loadAnimation(any(), R.anim.keyboard_in) } returns mShowAnimation
        every { AnimationUtils.loadAnimation(any(), R.anim.keyboard_out) } returns mHideAnimation
    }

    @Test
    fun test_onFocusChange_hasFocus() {
        val view = spyk(SecureKeyboardView(mContext, null), recordPrivateCalls = true)
        every { view["showKeyboard"]() } returns Unit
        view.onFocusChange(mockk(), true)
        verify { view["showKeyboard"]() }
    }

    @Test
    fun test_onFocusChange_lossFocus() {
        val view = spyk(SecureKeyboardView(mContext, null), recordPrivateCalls = true)
        every { view["hideKeyboard"]() } returns Unit
        view.onFocusChange(mockk(), false)
        verify { view["hideKeyboard"]() }
    }

    @Test
    fun test_showKeyboard_duringEndAnim() {
        val view = spyk(SecureKeyboardView(mContext, null), recordPrivateCalls = true)
        every { mHideAnimation.hasStarted() } returns true
        every { mHideAnimation.hasEnded() } returns false
        view.onFocusChange(mockk(), true)
        verify { mHideAnimation.cancel() }
    }

    private fun test_showKeyboard(hasStarted: Boolean, hasEnded: Boolean) {
        val view = spyk(SecureKeyboardView(mContext, null), recordPrivateCalls = true)
        val popupWindow = mockk<PopupWindow>(relaxed = true)
        val host = mockk<View>(relaxed = true)
        val hostViewRef = WeakReference(host)
        every { mHideAnimation.hasStarted() } returns hasStarted
        every { mHideAnimation.hasEnded() } returns hasEnded
        every { popupWindow.isShowing } returns false
        every { host.windowToken } returns mockk()
        every { view.startAnimation(mShowAnimation) } just Runs
        InternalPlatformDsl.dynamicSet(view, "popupWindow", popupWindow)
        InternalPlatformDsl.dynamicSet(view, "hostViewRef", hostViewRef)
        view.onFocusChange(mockk(), true)
        verify { popupWindow.showAtLocation(host, Gravity.BOTTOM, 0, 0) }
        verify { view.startAnimation(mShowAnimation) }
    }

    @Test
    fun test_showKeyboard_startAnimNotStart() {
        test_showKeyboard(hasStarted = false, hasEnded = false)
    }

    @Test
    fun test_showKeyboard_startAnimHasStartAndEnd() {
        test_showKeyboard(hasStarted = true, hasEnded = true)
    }

    @Test
    fun test_showKeyboard_startAnimHasEnd() {
        test_showKeyboard(hasStarted = false, hasEnded = true)
    }

    @Test
    fun test_showKeyboard_clearFocus() {
        val view = spyk(SecureKeyboardView(mContext, null), recordPrivateCalls = true)
        val popupWindow = mockk<PopupWindow>(relaxed = true)
        val host = mockk<View>(relaxed = true)
        val hostViewRef = WeakReference(host)
        val editText = mockk<EditText>(relaxed = true)
        val editTextRef = WeakReference(editText)
        every { mHideAnimation.hasStarted() } returns true
        every { mHideAnimation.hasEnded() } returns true
        every { popupWindow.isShowing } returns true
        every { host.windowToken } returns mockk()
        InternalPlatformDsl.dynamicSet(view, "popupWindow", popupWindow)
        InternalPlatformDsl.dynamicSet(view, "hostViewRef", hostViewRef)
        InternalPlatformDsl.dynamicSet(view, "editTextRef", editTextRef)
        view.onFocusChange(mockk(), true)
        verify { editText.clearFocus() }
    }

    @Test
    fun test_hideKeyboard() {
        val view = spyk(SecureKeyboardView(mContext, null), recordPrivateCalls = true)
        val popupWindow = mockk<PopupWindow>(relaxed = true)
        every { mHideAnimation.hasStarted() } returns false
        every { mHideAnimation.hasEnded() } returns true
        every { popupWindow.isShowing } returns true
        every { view.startAnimation(mHideAnimation) } just Runs
        InternalPlatformDsl.dynamicSet(view, "popupWindow", popupWindow)
        view.onFocusChange(mockk(), false)
        verify { view.startAnimation(mHideAnimation) }
    }

    @Test
    fun test_initKeyboard() {
        mockkStatic(ViewConfiguration::class)
        every { ViewConfiguration.getLongPressTimeout() } returns 400
        val view = spyk(SecureKeyboardView(mContext, null), recordPrivateCalls = true)
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val keyboardView = mockk<KeyboardView>(relaxed = true)
        val parser = mockk<XmlResourceParser>(relaxed = true)
        val doneView = mockk<View>(relaxed = true)
        val listener = slot<View.OnClickListener>()
        mockkStatic(LayoutInflater::class)
        mockkStatic(ContextCompat::class)
        mockkObject(SecureKeyboardUtils)
        every { LayoutInflater.from(mContext) } returns inflater
        every { inflater.inflate(R.layout.view_secure_keyboard, view, true) } returns mockk(relaxed = true)
        every { ContextCompat.getColor(mContext, R.color.keyboard_background) } returns 0
        every { parser.next() } returns XmlResourceParser.END_DOCUMENT
        every { SecureKeyboardUtils.generateKeyboard(any(), any(), any()) } just Runs
        every { doneView.setOnClickListener(capture(listener)) } just Runs

        every { view.context } returns mContext
        every { view.setBackgroundColor(0) } returns Unit
        every { view.findViewById<View>(R.id.keyboard_done) } returns doneView
        every { view.findViewById<KeyboardView>(R.id.secure_keyboard) } returns keyboardView
        every { view.findViewById<TextView>(R.id.keyboard_symbol) } returns mockk(relaxed = true)
        every { keyboardView.setEncryptHelper(any()) } just Runs

        InternalPlatformDsl.dynamicCall(view, "initKeyboard", emptyArray()) { mockk() }

        verify { view.setBackgroundColor(0) }
        verify { view.findViewById<View>(R.id.keyboard_done) }
        verify { view.findViewById<KeyboardView>(R.id.secure_keyboard) }
        verify { SecureKeyboardUtils.generateKeyboard(any(), any(), any()) }

        listener.captured.onClick(null)

        verify { view["hideKeyboard"]() }
    }

    @Test
    fun test_init() {
        val view = spyk(SecureKeyboardView(mContext, null), recordPrivateCalls = true)
        val editText = mockk<EditText>(relaxed = true)
        val popupWindow = mockk<PopupWindow>()
        val shuffle = true

        every { view["initKeyboard"]() } returns Unit
        every { view["showKeyboard"]() } returns Unit
        every { editText.hasFocus() } returns true

        view.init(editText, popupWindow, shuffle)

        verify { mHideAnimation.setAnimationListener(any()) }
        verify { view["initKeyboard"]() }
        verify { view["showKeyboard"]() }
        verify { editText.onFocusChangeListener = view }
    }

    @Test
    fun test_shuffle() {
        val context = mockk<Context>(relaxed = true)
        val builder = SecureKeyboardView.Builder(context)
        builder.setShuffle(false)
        assertFalse(InternalPlatformDsl.dynamicGet(builder, "shuffle") as Boolean)
    }

    @Test
    fun test_setTitleText() {
        val context = mockk<Context>(relaxed = true)
        val builder = SecureKeyboardView.Builder(context)
        builder.setTitleText("Title")
        assertEquals("Title", InternalPlatformDsl.dynamicGet(builder, "title") as String)
    }

    @Test
    fun test_setDoneText() {
        val context = mockk<Context>(relaxed = true)
        val builder = SecureKeyboardView.Builder(context)
        builder.setDoneText("Done")
        assertEquals("Done", InternalPlatformDsl.dynamicGet(builder, "done") as String)
    }

    @Test
    fun test_setEditText() {
        val context = mockk<Context>(relaxed = true)
        val editText = mockk<EditText>()
        val builder = SecureKeyboardView.Builder(context)
        builder.setEditText(editText)
        assertEquals(editText, InternalPlatformDsl.dynamicGet(builder, "editText") as EditText)
    }

    @Test
    fun test_build() {
        val context = mockk<Context>(relaxed = true)
        val inflater = mockk<LayoutInflater>(relaxed = true)
        val contentView = mockk<View>(relaxed = true)
        val keyboardView = mockk<SecureKeyboardView>(relaxed = true)
        val editText = mockk<EditText>(relaxed = true)
        mockkStatic(LayoutInflater::class)
        every { context.getString(any()) } returns ""
        every { LayoutInflater.from(context) } returns inflater
        every { inflater.inflate(R.layout.view_secure_popup, any()) } returns contentView
        every { contentView.findViewById<SecureKeyboardView>(R.id.secure_keyboard_view) } returns keyboardView
        every { keyboardView.findViewById<TextView>(any()) } returns mockk(relaxed = true)
        SecureKeyboardView.Builder(context).setEditText(editText).build()
        verify { keyboardView.init(any(), any(), any()) }
    }

    @Test
    fun test_onAnimationStart() {
        val view = spyk(SecureKeyboardView(mContext, null), recordPrivateCalls = true)
        val listener = InternalPlatformDsl.dynamicGet(view, "hideAnimationListener") as Animation.AnimationListener
        listener.onAnimationStart(mockk())
        assertFalse((InternalPlatformDsl.dynamicGet(view, "isHideCancel") as AtomicBoolean).get())
    }
}