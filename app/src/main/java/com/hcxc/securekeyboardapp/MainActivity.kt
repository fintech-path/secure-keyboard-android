package com.hcxc.securekeyboardapp

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.hcxc.securekeyboard.ScreenShotManager
import com.hcxc.securekeyboard.SecureKeyboardManager

class MainActivity : AppCompatActivity(), ScreenShotManager.OnScreenShotListener {
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

        addScreenShotListener()
    }

    override fun onShot(imagePath: String?) {
        Toast.makeText(applicationContext, "onScreenShot", Toast.LENGTH_LONG).show()
    }

    private fun addScreenShotListener() {
        ActivityCompat.requestPermissions(
            this as Activity,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )

        val shot = ScreenShotManager()
        shot.init(applicationContext)
        shot.setListener(this)
    }
}