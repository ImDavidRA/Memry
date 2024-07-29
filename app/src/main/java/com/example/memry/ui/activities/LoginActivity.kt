package com.example.memry.ui.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memry.R
import com.example.memry.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var fAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fAuth = FirebaseAuth.getInstance()

        val currentUser = fAuth.currentUser

        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                startActivity(Intent(this, ActivityPrincipal::class.java))
                finish()
            }
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.txtRegister.setOnClickListener {

            startActivity(Intent(this, RegisterActivity::class.java))
            finish()

        }

        binding.loginBtn.setOnClickListener {

            val email = binding.userEdt.text.toString()
            val pass = binding.passwrdEdt.text.toString()

            if (check_fields(email, pass))
                fAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {

                        val user = currentUser!!

                        if (user.isEmailVerified) {

                            startActivity(Intent(this, ActivityPrincipal::class.java))
                            finish()

                        } else {

                            mostrar_dialog()
                        }
                    } else
                        Toast.makeText(this, "Credenciales erroneas", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun mostrar_dialog() {
        val dialogVerify = Dialog(this)
        dialogVerify.setContentView(R.layout.pop_up_verifica_email)
        dialogVerify.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogVerify.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.custom_dialog_bg))
        dialogVerify.setCancelable(false)

        val cerrarDialogVerify: Button = dialogVerify.findViewById(R.id.confirmPop)

        dialogVerify.show()

        cerrarDialogVerify.setOnClickListener {
            dialogVerify.dismiss()
        }
    }

    private fun check_fields(email: String, pass: String): Boolean {

        return if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

}