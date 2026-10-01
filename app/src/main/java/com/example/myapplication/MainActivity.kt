package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth =
            FirebaseAuth.getInstance()

        val emailEditText =
            findViewById<EditText>(
                R.id.emailEditText
            )

        val passwordEditText =
            findViewById<EditText>(
                R.id.passwordEditText
            )

        val loginButton =
            findViewById<Button>(
                R.id.loginButton
            )

        val registerButton =
            findViewById<Button>(
                R.id.registerButton
            )

        loginButton.setOnClickListener {

            val email =
                emailEditText
                    .text
                    .toString()
                    .trim()

            val password =
                passwordEditText
                    .text
                    .toString()

            if (
                email.isEmpty() ||
                password.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val firebaseUser =
                        auth.currentUser

                    if (firebaseUser == null) {

                        Toast.makeText(
                            this,
                            "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@addOnCompleteListener
                    }

                    val uid =
                        firebaseUser.uid

                    FirebaseDatabase
                        .getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("verified")
                        .get()
                        .addOnSuccessListener { snapshot ->

                            val verified =
                                snapshot
                                    .getValue(Boolean::class.java)
                                    ?: false

                            if (verified) {

                                Toast.makeText(
                                    this,
                                    "Login successful",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent =
                                    Intent(
                                        this,
                                        ProfileActivity::class.java
                                    )

                                startActivity(intent)
                                finish()

                            } else {

                                Toast.makeText(
                                    this,
                                    "Please verify your account first.",
                                    Toast.LENGTH_LONG
                                ).show()

                                val intent =
                                    Intent(
                                        this,
                                        VerificationActivity::class.java
                                    )

                                intent.putExtra(
                                    "email",
                                    email
                                )

                                intent.putExtra(
                                    "uid",
                                    uid
                                )

                                startActivity(intent)
                            }
                        }

                } else {

                    Toast.makeText(
                        this,
                        task.exception?.message
                            ?: "Login failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        registerButton.setOnClickListener {

            val intent =
                Intent(
                    this,
                    RegisterActivity::class.java
                )

            startActivity(intent)
        }
    }
}