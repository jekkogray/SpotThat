package com.example.spotthat

// Spotify dependencies

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException


class SignUpActivity: AppCompatActivity() {
    companion object {
        const val TAG = "SignUpActivity"
    }

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var verifyPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var signUpButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // populate UI variables
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        verifyPassword = findViewById(R.id.verifyPassword)
        progressBar = findViewById(R.id.progressBar)
        signUpButton = findViewById(R.id.signupButton)
        signUpButton.isEnabled = false

        // Create Firebase instance
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)


        val textWatcher: TextWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                signUpButton.isEnabled = email.text.isNotEmpty() && password.text.isNotEmpty() && verifyPassword.text.isNotEmpty()
            }
        }

        // Disable ProgressBar
        progressBar.visibility = View.GONE

        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
        verifyPassword.addTextChangedListener(textWatcher)

        signUpButton.setOnClickListener{
            val inputtedUsername: String = email.text.toString()
            val inputtedPassword: String = password.text.toString()
            val inputtedVerifyPassword: String = verifyPassword.text.toString()

            if (inputtedPassword == inputtedVerifyPassword) {
                progressBar.visibility = View.VISIBLE
                firebaseAuth.createUserWithEmailAndPassword(inputtedUsername, inputtedPassword)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Successfully registered!", Toast.LENGTH_LONG)
                                .show()
                            // Go back to login screen
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                        }
                        else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Failed to register: ", it.exception)
                        }
                    }
            }
            else
                Toast.makeText(this, "Please make sure your passwords match.", Toast.LENGTH_SHORT).show()
                Log.d(TAG,"Passwords do not match.")
        }
    }


}