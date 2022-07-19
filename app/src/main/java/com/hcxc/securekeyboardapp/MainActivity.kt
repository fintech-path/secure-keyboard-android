package com.hcxc.securekeyboardapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hcxc.securekeyboard.SecureKeyboardManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SecureKeyboardManager.bindSecureEditText(findViewById(R.id.et_shuffle), true, null, null)
        SecureKeyboardManager.bindSecureEditText(
            findViewById(R.id.et_un_shuffle),
            false,
            null,
            null
        )
    }
}