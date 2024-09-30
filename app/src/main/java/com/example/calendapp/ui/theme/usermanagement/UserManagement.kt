package com.example.calendapp.ui.theme.usermanagement

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendapp.ui.theme.CalendappTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class EditableUser(var username: String, var email: String, var password: String)

class UserManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendappTheme {
                UserManagementPage()
            }
        }
    }

    @Composable
    fun UserManagementPage() {
        val database = FirebaseDatabase.getInstance().reference.child("users")
        var users by remember { mutableStateOf<List<EditableUser>>(emptyList()) }

        LaunchedEffect(Unit) {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userList = mutableListOf<EditableUser>()
                    snapshot.children.forEach {
                        val username = it.child("username").getValue(String::class.java) ?: ""
                        val email = it.child("email").getValue(String::class.java) ?: ""
                        val password = it.child("password").getValue(String::class.java) ?: ""
                        userList.add(EditableUser(username, email, password))
                    }
                    users = userList
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserManagementActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
                }
            })
        }

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
                // Título
                Text(
                    text = "L i s t a   d e   u s u a r i o s",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(), // Use LazyColumn for scrollable list
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Add space between items
                ) {
                    items(users) { user ->
                        UserCard(
                            user = user,
                            onUpdate = { updateUserInFirebase(user) },
                            onDelete = { deleteUserFromFirebase(user.email) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun UserCard(user: EditableUser, onUpdate: () -> Unit, onDelete: () -> Unit) {
        var username by remember { mutableStateOf(user.username) }
        var email by remember { mutableStateOf(user.email) }
        var password by remember { mutableStateOf(user.password) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "E d i t a r   u s u a r i o", fontSize = 18.sp, color = Color.Black)

            // Username input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email input (non-editable)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp)
            ) {
                Text(text = email, color = Color.Black) // Make email non-editable
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Password input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row to place the buttons side by side and centered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Update button
                Button(
                    onClick = {
                        user.username = username
                        user.password = password
                        onUpdate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    modifier = Modifier
                        .padding(end = 8.dp) // Space between the buttons
                ) {
                    Text("U p d a t e", color = Color.Black)
                }

                // Delete button
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("D e l e t e", color = Color.Black)
                }
            }
        }
    }

    // Update user in Firebase
    private fun updateUserInFirebase(user: EditableUser) {
        val database = FirebaseDatabase.getInstance().reference.child("users")
        val query = database.orderByChild("email").equalTo(user.email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    it.ref.child("username").setValue(user.username)
                    it.ref.child("password").setValue(user.password)
                    Toast.makeText(this@UserManagementActivity, "User updated", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserManagementActivity, "Update failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Delete user from Firebase
    private fun deleteUserFromFirebase(email: String) {
        val database = FirebaseDatabase.getInstance().reference.child("users")
        val query = database.orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { it.ref.removeValue() }
                Toast.makeText(this@UserManagementActivity, "User deleted", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserManagementActivity, "Delete failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
