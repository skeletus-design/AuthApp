package com.example.vshpauth

import android.app.Activity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.MaskedTextChangedListener.Companion.installOn

class RegistrationActivity : Activity() {

    private lateinit var phoneInput: EditText
    private lateinit var countrySpinner: Spinner
    private lateinit var maskListener: MaskedTextChangedListener

    // Данные стран: эмодзи, название, маска, пример номера для hint
    private val countryData = listOf(
        Country("🇷🇺", "Россия", "+7 ([000]) [000]-[00]-[00]", "+7 (912) 345-67-89"),
        Country("🇺🇸", "США", "+1 ([000]) [000]-[0000]", "+1 (212) 555-1234"),
        Country("🇬🇧", "Британия", "+44 [00] [0000] [0000]", "+44 20 7946 0958"),
        Country("🇩🇪", "Германия", "+49 [00] [00000000]", "+49 30 901820")
    )

    data class Country(
        val emoji: String,
        val name: String,
        val mask: String,
        val example: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_page)

        phoneInput = findViewById(R.id.editTextPhone)
        countrySpinner = findViewById(R.id.spinner2)

        // Настройка списка стран с эмодзи
        countrySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            countryData.map { "${it.emoji} ${it.name} (${it.example.take(4)}...)" }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Инициализация первой маски
        setupMaskForCountry(countryData[0])

        // Обработчик выбора страны
        countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                setupMaskForCountry(countryData[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                setupMaskForCountry(countryData[0])
            }
        }
    }

    private fun setupMaskForCountry(country: Country) {
        // Удаляем предыдущий listener
        if (::maskListener.isInitialized) {
            phoneInput.removeTextChangedListener(maskListener)
        }

        // Устанавливаем новую маску
        maskListener = installOn(
            phoneInput,
            country.mask,
            object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(
                    maskFilled: Boolean,
                    extractedValue: String,
                    formattedValue: String,
                    tailPlaceholder: String
                ) {
                    // Логирование при необходимости
                }
            }
        )

        // Обновляем hint с примером номера
        phoneInput.hint = "${country.example}"
        phoneInput.text?.clear()
    }
}