package com.kimminh.moneysense.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.Voice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.kimminh.moneysense.MainActivity
import com.kimminh.moneysense.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        val textToSpeech = MainActivity.textToSpeech

        val voicesAdapter: ArrayAdapter<Voice> // Declare the adapter with the appropriate type

        // Initialize the list of voices for each language
        val englishVoices = mutableListOf<Voice>()
        val vietnameseVoices = mutableListOf<Voice>()

        englishVoices.addAll(textToSpeech.voices.filter { it.locale.language == "en"})
        vietnameseVoices.addAll(textToSpeech.voices.filter { it.locale.language == "vi" })

        sharedPreferences = requireActivity().getSharedPreferences(KEY_SELECTED_LANGUAGE, Context.MODE_PRIVATE)
        val languagesList = mutableListOf("Select Language", "Tiếng Việt", "English")
        val langCode = sharedPreferences.getString(KEY_SELECTED_LANGUAGE, "")

        val voicesList: List<Voice> // Declare a list to hold the selected language voices

        if (langCode == "vi") {
            languagesList[0] = "Đã chọn: Tiếng Việt"
            voicesList = vietnameseVoices
        } else {
            languagesList[0] = "Selected: English"
            voicesList = englishVoices
        }

        val languageAdapter = ArrayAdapter(view.context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, languagesList)
        binding.spinnerChooseLanguage.adapter = languageAdapter
        binding.spinnerChooseLanguage.setSelection(0)
        binding.spinnerChooseLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLang = parent?.getItemAtPosition(position).toString()
                if (selectedLang == "Tiếng Việt") {
                    applyNewChanges(requireActivity())
                    saveSelectedLanguage("vi")
                }else if(selectedLang == "English"){
                    applyNewChanges(requireActivity())
                    saveSelectedLanguage("en")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing here or implement as needed
            }
        }

        // Create ArrayAdapter with the correct list of voices
        voicesAdapter = ArrayAdapter(view.context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, voicesList)
        binding.spinnerChooseAccent.adapter = voicesAdapter
        binding.spinnerChooseAccent.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedVoice = parent?.getItemAtPosition(position)
                textToSpeech.setVoice(selectedVoice as Voice?)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyNewChanges(activity: Activity) {
        // Restart the activity to apply the language change
        val intent = Intent(activity, activity.javaClass)
        activity.finish()
        activity.startActivity(intent)
    }

    private fun saveSelectedLanguage(selectedLanguage: String) {
        with (sharedPreferences.edit()) {
            putString("selected_language", selectedLanguage)
            apply()
        }
    }

    companion object {
        const val KEY_SELECTED_LANGUAGE = "selected_language"
    }
}
