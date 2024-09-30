package com.example.calendapp.ui.theme.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.calendapp.R
import com.example.calendapp.ui.theme.CalendappTheme
import com.example.calendapp.ui.theme.recuperarpass.RecuperarPasswordActivity
import com.example.calendapp.ui.theme.registro.RegistroActivity
import com.example.calendapp.ui.theme.usermanagement.UserManagementActivity
import com.example.calendapp.ui.theme.welcome.WelcomeActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendappTheme {
                LoginPage()
            }
        }
    }
}

@Composable
fun LoginPage() {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp)
            )

            // Slogan
            Text(
                text = "t o u r i s t   g u i d e   a p p",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            // Email Input
            val email = remember { mutableStateOf(TextFieldValue()) }
            BasicTextField(
                value = email.value,
                onValueChange = { email.value = it },
                modifier = Modifier
                    .fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(16.dp)
                    ) {
                        if (email.value.text.isEmpty()) {
                            Text(
                                text = "C o r r e o   e l e c t r ó n i c o",
                                color = Color.Black,
                            )
                        }
                        innerTextField()
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña Input
            val password = remember { mutableStateOf(TextFieldValue()) }
            BasicTextField(
                value = password.value,
                onValueChange = { password.value = it },
                modifier = Modifier
                    .fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(16.dp)
                    ) {
                        if (password.value.text.isEmpty()) {
                            Text(
                                text = "C o n t r a s e ñ a",
                                color = Color.Black
                            )
                        }
                        innerTextField()
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Inicio de sesión
            Button(
                onClick = {
                    authenticateUser(context, email.value.text, password.value.text) { isAuthenticated, username, isAdmin ->
                        if (isAuthenticated) {
                            Toast.makeText(context, "Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                            if (isAdmin) {
                                // Redirige a la página de Gestión de Usuarios (Admin)
                                val intent = Intent(context, UserManagementActivity::class.java)
                                context.startActivity(intent)
                            } else {
                                // Redirige a la página de Bienvenida (Regular User)
                                val intent = Intent(context, WelcomeActivity::class.java)
                                intent.putExtra("username", username)
                                context.startActivity(intent)
                            }
                        } else {
                            Toast.makeText(context, "Credenciales Inválidas!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .width(300.dp)
            ) {
                Text(
                    text = "I n i c i a r   S e s i ó n",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Registrarse
            Button(
                onClick = {
                    val intent = Intent(context, RegistroActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .width(300.dp)
            ) {
                Text(
                    text = "R e g i s t r a r s e",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link Recuperar Contraseña
            Text(
                text = "Recuperar contraseña",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Redirige a la página de recuperar contraseña
                        val intent = Intent(context, RecuperarPasswordActivity::class.java)
                        context.startActivity(intent)
                    }
                    .padding(vertical = 8.dp),
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = TextDecoration.Underline
                )
            )
        }
    }
}

fun authenticateUser(
    context: Context,
    email: String,
    password: String,
    onResult: (Boolean, String?, Boolean) -> Unit
) {
    val database = FirebaseDatabase.getInstance().reference.child("users")

    database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                // Check if the password matches
                for (userSnapshot in snapshot.children) {
                    val storedPassword = userSnapshot.child("password").value as String?
                    val username = userSnapshot.child("username").value as String?
                    val isAdmin = username == "admin" && storedPassword == "admin123"

                    if (storedPassword == password) {
                        // Successful login
                        onResult(true, username, isAdmin)
                        return
                    }
                }
            }
            // Invalid credentials
            onResult(false, null, false)
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(context, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            onResult(false, null, false)
        }
    })
}
