package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Replace this with your real Google Apps Script Web App URL
    private val webAppUrl =
        "https://script.google.com/macros/s/AKfycbx_Cmj5rU5Ps_yADO-kNtnWr8UB75L3-vPOtPClWKhIEMxeCfxQl5FR-9WEp54I1Hgxgw/exec"

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
                            "Could not create account",
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
                        "role" to role,
                        "verified" to false
                    )

                    FirebaseDatabase
                        .getInstance()
                        .getReference("users")
                        .child(uid)
                        .setValue(userProfile)
                        .addOnCompleteListener { databaseTask ->

                            if (databaseTask.isSuccessful) {

                                sendVerificationCode(
                                    email = email,
                                    uid = uid
                                )

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

    private fun sendVerificationCode(
        email: String,
        uid: String
    ) {

        val requestQueue = Volley.newRequestQueue(this)

        val jsonBody = JSONObject()

        jsonBody.put("action", "send")
        jsonBody.put("email", email)

        val request = JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            webAppUrl,
            jsonBody,

            { response ->

                val success =
                    response.optBoolean("success")

                val message =
                    response.optString("message")

                if (success) {

                    Toast.makeText(
                        this,
                        "Verification code sent to $email",
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
                    finish()

                } else {

                    Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            },

            { error ->

                Toast.makeText(
                    this,
                    "Could not send verification code: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        requestQueue.add(request)
    }
}