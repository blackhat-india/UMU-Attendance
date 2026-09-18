package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun AuthDialog(
    onLogin: (regNo: String, pass: String) -> Unit,
    onSignUp: (regNo: String, name: String, pass: String, branch: String, rollNo: String) -> Unit,
    onQuickDemo: () -> Unit,
    onDismiss: () -> Unit
) {
    var isLoginTab by remember { mutableStateOf(true) }

    // Login state
    var loginRegNo by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }

    // Sign Up state
    var signupRegNo by remember { mutableStateOf("") }
    var signupName by remember { mutableStateOf("") }
    var signupPass by remember { mutableStateOf("") }
    var signupBranch by remember { mutableStateOf("B.Tech CSE") }
    var signupRoll by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // University Logo Header
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(UmuNavy, UmuPrimary, UmuPrimaryLight)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Usha Martin University",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "B.Tech CSE Attendance Portal",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Switcher (Log In / Sign Up)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(100.dp)
                        )
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { isLoginTab = true },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoginTab) UmuPrimaryLight else Color.Transparent,
                            contentColor = if (isLoginTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Log In", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isLoginTab = false },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isLoginTab) UmuPrimaryLight else Color.Transparent,
                            contentColor = if (!isLoginTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Sign Up", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoginTab) {
                    // --- LOGIN FORM ---
                    OutlinedTextField(
                        value = loginRegNo,
                        onValueChange = { loginRegNo = it },
                        label = { Text("College Reg. Number") },
                        placeholder = { Text("e.g. UMU/2026/CSE/001") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = loginPass,
                        onValueChange = { loginPass = it },
                        label = { Text("Password") },
                        placeholder = { Text("Enter password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onLogin(loginRegNo, loginPass) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UmuPrimaryLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Log In to Portal", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onQuickDemo,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ Quick Demo (Aman Sharma)",
                            fontWeight = FontWeight.Bold,
                            color = UmuPrimaryLight
                        )
                    }
                } else {
                    // --- SIGN UP FORM ---
                    OutlinedTextField(
                        value = signupRegNo,
                        onValueChange = { signupRegNo = it },
                        label = { Text("Registration Number") },
                        placeholder = { Text("e.g. UMU/2026/CSE/042") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signupName,
                        onValueChange = { signupName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Rahul Verma") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signupPass,
                        onValueChange = { signupPass = it },
                        label = { Text("Set Password") },
                        placeholder = { Text("Min 4 characters") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signupBranch,
                        onValueChange = { signupBranch = it },
                        label = { Text("Branch & Semester") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onSignUp(signupRegNo, signupName, signupPass, signupBranch, signupRoll) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UmuPrimaryLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Create Account & Log In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
