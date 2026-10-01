package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val nameEditText =
            findViewById<EditText>(R.id.profileNameEditText)

        val emailEditText =
            findViewById<EditText>(R.id.profileEmailEditText)

        val phoneEditText =
            findViewById<EditText>(R.id.profilePhoneEditText)

        val addressEditText =
            findViewById<EditText>(R.id.profileAddressEditText)

        val roleTextView =
            findViewById<TextView>(R.id.profileRoleTextView)

        val saveButton =
            findViewById<Button>(R.id.saveProfileButton)

        val logoutButton =
            findViewById<Button>(R.id.logoutButton)

        val currentUser = auth.currentUser

        if (currentUser == null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val uid = currentUser.uid

        val userReference = FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)

        // Load user profile
        userReference.get()
            .addOnSuccessListener { snapshot ->

                val name =
                    snapshot.child("name")
                        .getValue(String::class.java)

                val email =
                    snapshot.child("email")
                        .getValue(String::class.java)

                val phone =
                    snapshot.child("phone")
                        .getValue(String::class.java)

                val address =
                    snapshot.child("address")
                        .getValue(String::class.java)

                val role =
                    snapshot.child("role")
                        .getValue(String::class.java)

                nameEditText.setText(name)
                emailEditText.setText(email)
                phoneEditText.setText(phone)
                addressEditText.setText(address)

                roleTextView.text = "Role: ${role ?: ""}"
            }
            .addOnFailureListener { exception ->

                Toast.makeText(
                    this,
                    "Failed to load profile: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        // Save profile changes
        saveButton.setOnClickListener {

            val updatedName =
                nameEditText.text.toString().trim()

            val updatedPhone =
                phoneEditText.text.toString().trim()

            val updatedAddress =
                addressEditText.text.toString().trim()

            if (
                updatedName.isEmpty() ||
                updatedPhone.isEmpty() ||
                updatedAddress.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val updates = mapOf(
                "name" to updatedName,
                "phone" to updatedPhone,
                "address" to updatedAddress
            )

            userReference.updateChildren(updates)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { exception ->

                    Toast.makeText(
                        this,
                        "Update failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // Logout
        logoutButton.setOnClickListener {

            auth.signOut()

            val intent =
                Intent(this, MainActivity::class.java)

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }
}