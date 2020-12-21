package com.example.spotthat

// Spotify dependencies

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception
import java.lang.NullPointerException


class SignInActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SignInActivity"
    }

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var signInButton: Button
    private lateinit var signUpButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var rememberMeSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        try {
            this.supportActionBar?.hide()
        } catch (e: NullPointerException) {
            Log.e(TAG, "Exception: ", e)
        }
        // UI
        // Populate lateinit var
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        progressBar = findViewById(R.id.progressBar)
        signInButton = findViewById(R.id.signinButton)
        signUpButton = findViewById(R.id.signupButton)
        rememberMeSwitch = findViewById(R.id.rememberMeSwitch)

        signInButton.isEnabled = false

        // Get shared preferences
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("SpotThat", Context.MODE_PRIVATE)

        // Create Firebase instance
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)


        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                signInButton.isEnabled = email.text.isNotEmpty() && password.text.isNotEmpty()
            }
        }

        // Disable ProgressBar
        progressBar.visibility = View.GONE

        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

        signInButton.setOnClickListener {
            val inputtedUsername: String = email.text.toString()
            val inputtedPassword: String = password.text.toString()
            progressBar.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        progressBar.visibility = View.GONE
                        Log.d(TAG, "Login successful")
                        if (rememberMeSwitch.isChecked)
                            sharedPreferences.edit()
                                .putString("SAVED_EMAIL", email.text.toString())
                                .putString("SAVED_PASSWORD", password.text.toString())
                                .putBoolean("SAVED_REMEMBERED", rememberMeSwitch.isChecked)
                                .apply()
                        else
                            sharedPreferences.edit().clear().apply()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Failed to sign in: ${it.exception?.localizedMessage}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        Log.e(TAG, "Failed to login: ", it.exception)
                    }
                }
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        email.setText(sharedPreferences.getString("SAVED_EMAIL", ""))
        password.setText(sharedPreferences.getString("SAVED_PASSWORD", ""))
        rememberMeSwitch.isChecked = sharedPreferences.getBoolean("SAVED_REMEMBERED", false)
    }
}