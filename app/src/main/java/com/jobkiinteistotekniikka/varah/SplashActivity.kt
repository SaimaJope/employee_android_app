package com.jobkiinteistotekniikka.varah

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "KioskPrefs"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val SPLASH_DELAY = 1500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skip splash if already opened once
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_FIRST_LAUNCH, true)) {
            goMain()
            return
        }
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()

        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splashLogo)
        val text = findViewById<TextView>(R.id.splashText)
        val anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        logo.startAnimation(anim)
        text.startAnimation(anim)

        Handler(Looper.getMainLooper()).postDelayed({
            goMain()
        }, SPLASH_DELAY)
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
