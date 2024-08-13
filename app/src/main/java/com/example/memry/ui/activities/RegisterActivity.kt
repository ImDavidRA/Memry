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
import com.example.memry.dataClasses.Usuario
import com.example.memry.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        iniciar_variables()

        enableEdgeToEdge()

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        iniciar_botones()

    }

    // Método utilizado para iniciar las variables que se usarán en el activity
    private fun iniciar_variables() {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    /**
     *     Método utilizado para iniciar las funciones que realizan los botones
     *     del binding pasado como parámetro.
     *
     *     Args:
     *          binding (Binding): El XML con la información necesaria a utilizar.
     */
    private fun iniciar_botones() {
        binding.btnRegister.setOnClickListener {

            val nombre = binding.edtName.text.toString()
            val apellido = binding.edtApellido.text.toString()
            val email = binding.edtEmail.text.toString()
            val pass = binding.edtPass.text.toString()
            val cPass = binding.edtConfirmPass.text.toString()

            if (check_fields(email, pass, cPass)) {
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{
                    if (it.isSuccessful) {

                        Toast.makeText(this, "Se ha enviado un email de verificación", Toast.LENGTH_LONG).show()
                        firebaseAuth.currentUser?.sendEmailVerification()

                        guardar_datos_firebase(nombre, apellido, email)

                        mostrar_dialog()

                    } else {
                        Toast.makeText(this, "Algo salió mal", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }


    /**
     *     Permite guardar datos sobre el nuevo usuario en la base de datos
     *
     *     Args:
     *          nombre (String):    Texto a almacenar como nombre
     *          apellidos (String): Texto a almacenar como apellidos
     *          email (String):     Texto a almacenar como email
     */
    private fun guardar_datos_firebase(nombre: String, apellido: String, email: String) {
        val uid = firebaseAuth.currentUser?.uid

        val dataRef = FirebaseDatabase
            .getInstance("https://memry--app-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("Usuarios")

        dataRef.child("$uid").child("Datos Personales").setValue(Usuario(nombre, apellido, email))

    }

    /**
     * Muestra un diálogo pidiendo al usuario que verifique su email
     * al cerrarlo abre el LoginActivity
     */
    private fun mostrar_dialog() {
        val dialogVerify = Dialog(this)
        dialogVerify.setContentView(R.layout.dialog_verifica_email)

        // Configura las dimensiones del diálogo y el fondo personalizado
        dialogVerify.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogVerify.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.custom_dialog_bg))

        // Hace que el diálogo no se pueda cancelar tocando fuera de él
        dialogVerify.setCancelable(false)
        dialogVerify.setCanceledOnTouchOutside(true)

        val cerrarDialogVerify: Button = dialogVerify.findViewById(R.id.confirmPop)

        dialogVerify.show()

        cerrarDialogVerify.setOnClickListener {
            dialogVerify.dismiss()
        }

        dialogVerify.setOnDismissListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    /**
     *     Método que comprueba que los campos estén rellenos y
     *     ambas contraseñas coincidan
     *
     *     Args:
     *          email (String): Texto a email a comprobar si está vacío o no.
     *          pass (String):  Texto de contraseña a comprobar si está vacío o no.
     *                          o si coincide con cPass
     *          cPass (String): Texto de confirmar contraseña a comprobar si está vacío
     *                          o si coincide con pass
     *
     *     Returns -> Boolean:
     *          True:   Si hay texto en todos y las contraseñas coinciden
     *          False:  Si alguno de ellos está en blanco o las contraseñas no coinciden
     */
    private fun check_fields(email : String, password : String, cPass : String) : Boolean {

        if (email.isBlank() or password.isBlank() or cPass.isBlank()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            return false
        } else
            if (!password.equals(cPass)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return false
            }
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}