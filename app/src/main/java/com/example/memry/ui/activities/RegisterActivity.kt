package com.example.memry.ui.activities

import android.annotation.SuppressLint
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
import com.example.memry.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnRegister.setOnClickListener {

            val email = binding.edtEmail.text.toString()
            val pass = binding.edtPass.text.toString()
            val cPass = binding.edtConfirmPass.text.toString()

            if (check_fields(email, pass, cPass)) {
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{
                    if (it.isSuccessful) {

                        Toast.makeText(this, "Verification email has been sent", Toast.LENGTH_LONG).show()
                        firebaseAuth.currentUser?.sendEmailVerification()

                        mostrar_dialog()

                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }

        }

    }

    private fun mostrar_dialog() {
        val dialogVerify = Dialog(this)
        dialogVerify.setContentView(R.layout.dialog_verifica_email)
        dialogVerify.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogVerify.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.custom_dialog_bg))
        dialogVerify.setCancelable(false)
        dialogVerify.setCanceledOnTouchOutside(true)

        val cerrarDialogVerify: Button = dialogVerify.findViewById(R.id.confirmPop)

        dialogVerify.show()

        cerrarDialogVerify.setOnClickListener {
            dialogVerify.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun check_fields(email : String, password : String, cPass : String) : Boolean {
        if (email.isBlank() or password.isBlank() or cPass.isBlank()) {
            Toast.makeText(this, "Fill every box to continue", Toast.LENGTH_SHORT).show()
            return false
        } else
            if (!password.equals(cPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return false
            }
        return true
    }

}