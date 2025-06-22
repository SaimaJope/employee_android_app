package com.jobkiinteistotekniikka.varah

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.jobkiinteistotekniikka.varah.databinding.SettingsactivityBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsactivityBinding
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREFS_NAME = "KioskPrefs"
        const val KEY_API_KEY = "kiosk_api_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        loadApiKey()

        binding.buttonSave.setOnClickListener {
            saveApiKey()
        }
    }

    private fun loadApiKey() {
        val savedKey = sharedPreferences.getString(KEY_API_KEY, null)
        binding.editTextApiKey.setText(savedKey)
    }

    private fun saveApiKey() {
        val newApiKey = binding.editTextApiKey.text.toString().trim()

        if (newApiKey.isNotEmpty()) {
            sharedPreferences.edit().putString(KEY_API_KEY, newApiKey).apply()
            // Using string resource for the Toast
            Toast.makeText(this, getString(R.string.settings_toast_saved), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // Using string resource for the Toast
            Toast.makeText(this, getString(R.string.settings_toast_empty), Toast.LENGTH_SHORT).show()
        }
    }
}