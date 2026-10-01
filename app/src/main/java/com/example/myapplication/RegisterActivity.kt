package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val nameEditText =
            findViewById<EditText>(R.id.nameEditText)

        val emailEditText =
            findViewById<EditText>(R.id.emailEditText)

        val phoneEditText =
            findViewById<EditText>(R.id.phoneEditText)

        val addressEditText =
            findViewById<EditText>(R.id.addressEditText)

        val passwordEditText =
            findViewById<EditText>(R.id.passwordEditText)

        val confirmPasswordEditText =
            findViewById<EditText>(R.id.confirmPasswordEditText)

        val buyerRadioButton =
            findViewById<RadioButton>(R.id.buyerRadioButton)

        val sellerRadioButton =
            findViewById<RadioButton>(R.id.sellerRadioButton)

        val registerButton =
            findViewById<Button>(R.id.registerButton)

        val backToLoginButton =
            findViewById<Button>(R.id.backToLoginButton)

        registerButton.setOnClickListener {

            val name =
                nameEditText.text.toString().trim()

            val email =
                emailEditText.text.toString().trim()

            val phone =
                phoneEditText.text.toString().trim()

            val address =
                addressEditText.text.toString().trim()

            val password =
                passwordEditText.text.toString()

            val confirmPassword =
                confirmPasswordEditText.text.toString()

            val role = when {
                buyerRadioButton.isChecked -> "buyer"
                sellerRadioButton.isChecked -> "seller"
                else -> ""
            }

            if (
                name.isEmpty() ||
                email.isEmpty() ||
                phone.isEmpty() ||
                address.isEmpty() ||
                password.isEmpty() ||
                confirmPassword.isEmpty() ||
                role.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (password != confirmPassword) {

                Toast.makeText(
                    this,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val firebaseUser = auth.currentUser

                    if (firebaseUser == null) {

                        Toast.makeText(
                            this,
                            "Could not create user",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@addOnCompleteListener
                    }

                    val uid = firebaseUser.uid

                    val userProfile = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "address" to address,
                        "role" to role
                    )

                    FirebaseDatabase
                        .getInstance()
                        .getReference("users")
                        .child(uid)
                        .setValue(userProfile)
                        .addOnCompleteListener { databaseTask ->

                            if (databaseTask.isSuccessful) {

                                firebaseUser
                                    .sendEmailVerification()
                                    .addOnCompleteListener { verifyTask ->

                                        if (verifyTask.isSuccessful) {

                                            Toast.makeText(
                                                this,
                                                "Verification email sent to ${firebaseUser.email}. Check Inbox and Spam.",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            auth.signOut()
                                            finish()

                                        } else {

                                            Toast.makeText(
                                                this,
                                                "Verification email failed: ${verifyTask.exception?.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                            } else {

                                Toast.makeText(
                                    this,
                                    "Could not save profile: ${databaseTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                } else {

                    Toast.makeText(
                        this,
                        task.exception?.message
                            ?: "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        backToLoginButton.setOnClickListener {
            finish()
        }
    }
}