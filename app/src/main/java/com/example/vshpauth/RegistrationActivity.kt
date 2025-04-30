package com.example.vshpauth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.MaskedTextChangedListener.Companion.installOn

class RegistrationActivity: Activity() {

    private lateinit var listener: MaskedTextChangedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_page)

        val phoneInput = findViewById<EditText>(R.id.editTextPhone)
        val showButton = findViewById<Button>(R.id.regButton)

        listener = installOn(
            phoneInput,
            "+7 ([000]) [000]-[00]-[00]",
            object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(
                    maskFilled: Boolean,
                    extractedValue: String,
                    formattedValue: String,
                    tailPlaceholder: String
                ) {
                    Log.d("MASK", "Полный номер: $formattedValue")
                    Log.d("MASK", "Чистый номер: $extractedValue")
                }
            }
        )
    }

    fun onTextViewClick(view: View) {
        if (view is TextView) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}