package com.ugikpoenya.sampleapp.adsmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ugikpoenya.servermanager.ServerManager


class SplashscreenActivity : AppCompatActivity() {
    val LOG = "LOG_ADS_MANAGER"
    val serverManager = ServerManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splashscreen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        serverManager.setBaseUrl(this, "https://asia-southeast1-project-bangau.cloudfunctions.net/cms/api")
        serverManager.setApiKey(this, "DA8BB129F7C1ED5BD07046961C995A77")
        serverManager.getApiResponse(this) { response ->
            Log.d(LOG, response?.name.toString())
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}