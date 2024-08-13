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
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    // Variables Tardías que se inicializarán en "onCreate"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iniciar_variables()
        si_user_logged_inicia_principal()

        enableEdgeToEdge()

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        iniciar_botones()
    }

    /**
     *     Método utilizado para iniciar las funciones que realizan los botones
     *     del binding pasado como parámetro.     *
     */
    private fun iniciar_botones() {
        // Botón para rellenar los campos de email y contraseña con valores predefinidos
        // TODO: Eliminar cuando acabe
        binding.rellenar.setOnClickListener {
            binding.userEdt.setText("lendobar@gmail.com")
            binding.passwrdEdt.setText("David200")
        }

        // TODO: Añadir botón para "Contraseña Olvidada"

        // Listener para redirigir a la actividad de registro
        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        // Listener para iniciar sesión con email y contraseña
        binding.loginBtn.setOnClickListener {
            val email = binding.userEdt.text.toString()
            val pass = binding.passwrdEdt.text.toString()

            if (check_fields(email, pass)) {
                // Intenta iniciar sesión con Firebase Authentication
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        currentUser?.let {
                            if (it.isEmailVerified) {
                                // Si el email está verificado, redirige a la actividad principal
                                startActivity(Intent(this, ActivityPrincipal::class.java))
                                finish()
                            } else {
                                // Si el email no está verificado, muestra un diálogo pidiendo la verificación
                                mostrar_dialog()
                            }
                        } ?: run {
                            // Manejo de error en caso de que `currentUser` sea `null`
                            Toast.makeText(this, "Error inesperado: usuario no encontrado.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Muestra un mensaje si las credenciales son incorrectas
                        Toast.makeText(this, "Credenciales erróneas", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Método utilizado para iniciar las variables que se usarán en el activity
    private fun iniciar_variables() {
        binding = ActivityLoginBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser
    }

    // Verifica si el usuario ya ha iniciado sesión y redirige a la actividad principal si es así
    private fun si_user_logged_inicia_principal() {
        currentUser?.let {
            // Si el email está verificado, redirige a la actividad principal
            if (it.isEmailVerified) {
                startActivity(Intent(this, ActivityPrincipal::class.java))
                finish()
            }
        }
    }

    // Muestra un diálogo pidiendo al usuario que verifique su email
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
    }

    /**
     *     Método utilizado para asegurarse de que los textos de los campos parámetro,
     *     están o no rellenos, mostrará un Toast si no lo están
     *
     *     Args:
     *          email (String): Texto a email a comprobar si está vacío o no
     *          pass (String):  Texto de contraseña a comprobar si está vacío o no
     *
     *     Returns -> Boolean:
     *          True:   Si tanto email como pass no están en blanco.
     *          False:  Si alguno de ellos está en blanco.
     */
    private fun check_fields(email: String, pass: String): Boolean {
        return if (email.isBlank() || pass.isBlank()) {
            // Muestra un mensaje de error si alguno de los campos está vacío
            Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }
}
