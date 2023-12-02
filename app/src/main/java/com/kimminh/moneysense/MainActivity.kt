package com.kimminh.moneysense

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kimminh.moneysense.databinding.ActivityMainBinding
import com.kimminh.moneysense.ui.settings.SettingsFragment
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = binding.navHostFragmentActivityMain.getFragment<NavHostFragment>().navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_history, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Get the language code of the app
        val appLanguageCode = getCurrentAppLanguageCode()
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set the TTS language
                val result = textToSpeech.setLanguage(Locale(appLanguageCode))
                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    textToSpeech.language = Locale.ENGLISH
                    Log.e("TTS", "Language not supported")
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
    }

    private fun getCurrentAppLanguageCode(): String {
        val sharedPref = getSharedPreferences(SettingsFragment.KEY_SELECTED_LANGUAGE, Context.MODE_PRIVATE)
        return sharedPref.getString(SettingsFragment.KEY_SELECTED_LANGUAGE, "") ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
    }

    companion object {
        lateinit var textToSpeech: TextToSpeech
            private set
    }
}