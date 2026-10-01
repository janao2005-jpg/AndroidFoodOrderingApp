package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject

class VerificationActivity : AppCompatActivity() {

    private val webAppUrl =
       "https://script.google.com/macros/s/AKfycbx_Cmj5rU5Ps_yADO-kNtnWr8UB75L3-vPOtPClWKhIEMxeCfxQl5FR-9WEp54I1Hgxgw/exec"

    private lateinit var email: String
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_verification)

        email = intent.getStringExtra("email") ?: ""
        uid = intent.getStringExtra("uid") ?: ""

        val verificationMessageTextView =
            findViewById<TextView>(
                R.id.verificationMessageTextView
            )

        val codeEditText =
            findViewById<EditText>(
                R.id.codeEditText
            )

        val verifyButton =
            findViewById<Button>(
                R.id.verifyButton
            )

        val resendCodeButton =
            findViewById<Button>(
                R.id.resendCodeButton
            )

        verificationMessageTextView.text =
            "We sent a 6-digit code to:\n$email"

        verifyButton.setOnClickListener {

            val code =
                codeEditText.text.toString().trim()

            if (code.length != 6) {

                Toast.makeText(
                    this,
                    "Please enter the 6-digit code",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            verifyCode(code)
        }

        resendCodeButton.setOnClickListener {
            resendCode()
        }
    }

    private fun verifyCode(code: String) {

        val requestQueue =
            Volley.newRequestQueue(this)

        val jsonBody =
            JSONObject()

        jsonBody.put("action", "verify")
        jsonBody.put("email", email)
        jsonBody.put("code", code)

        val request =
            JsonObjectRequest(
                Request.Method.POST,
                webAppUrl,
                jsonBody,

                { response ->

                    val success =
                        response.optBoolean("success")

                    val message =
                        response.optString("message")

                    if (success) {

                        FirebaseDatabase
                            .getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("verified")
                            .setValue(true)
                            .addOnSuccessListener {

                                Toast.makeText(
                                    this,
                                    "Account verified successfully",
                                    Toast.LENGTH_LONG
                                ).show()

                                FirebaseAuth
                                    .getInstance()
                                    .signOut()

                                val intent =
                                    Intent(
                                        this,
                                        MainActivity::class.java
                                    )

                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                                startActivity(intent)
                                finish()
                            }

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
                        "Verification failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )

        requestQueue.add(request)
    }

    private fun resendCode() {

        val requestQueue =
            Volley.newRequestQueue(this)

        val jsonBody =
            JSONObject()

        jsonBody.put("action", "send")
        jsonBody.put("email", email)

        val request =
            JsonObjectRequest(
                Request.Method.POST,
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
                            "New verification code sent",
                            Toast.LENGTH_LONG
                        ).show()

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
                        "Could not resend code: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )

        requestQueue.add(request)
    }
}