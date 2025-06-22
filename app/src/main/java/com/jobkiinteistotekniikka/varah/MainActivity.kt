package com.jobkiinteistotekniikka.varah

// ALL NECESSARY IMPORTS
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.jobkiinteistotekniikka.varah.databinding.ActivityMainBinding
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


// Data classes
@JsonClass(generateAdapter = true)
data class TapRequest(val nfc_card_id: String)
@JsonClass(generateAdapter = true)
data class TapResponse(val success: Boolean, val message: String, val employee_name: String?, val action: String?)

// API interface
interface ApiService {
    @POST("api/kiosk/tap")
    suspend fun recordTap(
        @Header("x-api-key") apiKey: String,
        @Body request: TapRequest
    ): TapResponse
}

class MainActivity : AppCompatActivity() {

    // UI Binding
    private lateinit var binding: ActivityMainBinding
    // Navigation Drawer UI
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var topAppBar: MaterialToolbar
    // NFC and API
    private var nfcAdapter: NfcAdapter? = null
    private var kioskApiKey: String? = null

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://varah-8asg.onrender.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup for Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        topAppBar = findViewById(R.id.topAppBar)

        topAppBar.setNavigationOnClickListener {
            drawerLayout.open()
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            drawerLayout.close()
            true
        }

        // Setup for NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC not available on this device.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        kioskApiKey = sharedPreferences.getString(SettingsActivity.KEY_API_KEY, null)

        if (kioskApiKey == null) {
            Toast.makeText(this, getString(R.string.main_error_no_api_key), Toast.LENGTH_LONG).show()
        }

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try { addDataType("text/plain") } catch (e: Exception) {}
        }
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, arrayOf(ndefFilter), null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        vibratePhone()
        if (intent?.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val ndefMessages: Array<NdefMessage>? = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.map { it as NdefMessage }?.toTypedArray()
            if (!ndefMessages.isNullOrEmpty()) {
                val record = ndefMessages[0].records[0]
                val nfcCardId = String(record.payload).drop(3)
                sendCardIdToServer(nfcCardId)
            }
        }
    }

    private fun sendCardIdToServer(cardId: String) {
        if (kioskApiKey == null) {
            binding.textViewStatus.text = getString(R.string.main_error_no_api_key)
            return
        }

        lifecycleScope.launch {
            try {
                binding.textViewStatus.text = getString(R.string.main_status_connecting)
                val request = TapRequest(nfc_card_id = cardId)
                val response = apiService.recordTap(kioskApiKey!!, request)

                binding.textViewStatus.text = response.message // Display server message directly

            } catch (e: Exception) {
                binding.textViewStatus.text = getString(R.string.main_status_connection_error)
                Log.e("NetworkError", "Failed to connect to server", e)
            }
        }
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
}