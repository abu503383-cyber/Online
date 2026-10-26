package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseAdminBridge
import com.example.model.AppLanguage
import com.example.ui.components.tr
import com.example.ui.theme.NexBluePrimary
import com.example.ui.theme.NexPurpleSecondary
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.NexEarnViewModel

private val countriesList = listOf(
    "Bangladesh", "India", "United States", "United Kingdom",
    "Saudi Arabia", "United Arab Emirates", "Malaysia", "Canada", "Australia"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoginTab by remember { mutableStateOf(true) }
    var showForgotPassDialog by remember { mutableStateOf(false) }

    // Login Fields
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Registration Fields (Section 20: Full Name, Username, Email or Phone, Password, Confirm Password, Country, Terms)
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("Bangladesh") }
    var countryDropdownExpanded by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(true) }
    var showRegPassword by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }

    val usernameClean = username.trim().removePrefix("@").lowercase()
    val isUsernameValidAndUnique = usernameClean.length >= 3 &&
        usernameClean.matches("^[a-z0-9_]+$".toRegex()) &&
        viewModel.isUsernameAvailable(usernameClean)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Language & Firebase Ready Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SuccessGreen.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = "Firebase Ready",
                            tint = SuccessGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = tr(lang, "Firebase ও Admin রেডি", "Firebase & Admin Ready"),
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                FilterChip(
                    selected = true,
                    onClick = {
                        viewModel.setLanguage(if (lang == AppLanguage.BN) AppLanguage.EN else AppLanguage.BN)
                    },
                    label = {
                        Text(if (lang == AppLanguage.BN) "বাংলা ⇄ EN" else "EN ⇄ বাংলা")
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Language, contentDescription = "Language", modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.testTag("auth_language_toggle")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hero Branding Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NexBluePrimary, NexPurpleSecondary)
                        )
                    )
                    .padding(22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "NexEarn Logo",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "NexEarn",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = tr(
                            lang,
                            "আধুনিক সোশ্যাল মিডিয়া ও ভেরিফায়েড আর্নিং প্ল্যাটফর্ম",
                            "Modern Social Media & Verified Earning Platform"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Mode Switcher (Login vs Registration)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        val tabs = listOf(
                            true to tr(lang, "লগইন (Login)", "Login"),
                            false to tr(lang, "রেজিস্ট্রেশন (Register)", "Register")
                        )
                        tabs.forEach { (loginTab, title) ->
                            val selected = isLoginTab == loginTab
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        isLoginTab = loginTab
                                        regError = null
                                    }
                                    .testTag(if (loginTab) "tab_login" else "tab_register"),
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isLoginTab) {
                        // LOGIN FORM
                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            label = {
                                Text(tr(lang, "ইউজারনেম, ইমেইল অথবা ফোন", "Username, Email or Phone"))
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = {
                                Text(tr(lang, "পাসওয়ার্ড (Password)", "Password"))
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                    Icon(
                                        imageVector = if (showLoginPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showForgotPassDialog = true },
                                modifier = Modifier.testTag("forgot_password_button")
                            ) {
                                Text(tr(lang, "পাসওয়ার্ড ভুলে গেছেন?", "Forgot Password?"))
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.loginWithCredentials(loginIdentifier, loginPassword)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr(lang, "লগইন করুন", "Sign In"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Instant One-Tap Demo User Login for immediate exploration
                        FilledTonalButton(
                            onClick = { viewModel.loginAsDemoUser() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("demo_login_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr(
                                    lang,
                                    "ডেমো ইউজার প্যানেলে সরাসরি প্রবেশ করুন",
                                    "Instant Demo User Panel Login"
                                ),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        // REGISTRATION FORM (Section 20)
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(tr(lang, "পূর্ণ নাম (Full Name)", "Full Name")) },
                            leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_fullname_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text(tr(lang, "ইউনিক ইউজারনেম (Username)", "Unique Username")) },
                            leadingIcon = { Icon(Icons.Outlined.AlternateEmail, contentDescription = null) },
                            trailingIcon = {
                                if (usernameClean.isNotEmpty()) {
                                    Icon(
                                        imageVector = if (isUsernameValidAndUnique) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                        contentDescription = null,
                                        tint = if (isUsernameValidAndUnique) SuccessGreen else MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            supportingText = {
                                if (usernameClean.isNotEmpty()) {
                                    Text(
                                        text = if (isUsernameValidAndUnique)
                                            tr(lang, "✓ @$usernameClean ইউজারনেমটি পাওয়া যাচ্ছে", "✓ @$usernameClean is available")
                                        else
                                            tr(lang, "ইউজারনেম অন্তত ৩ অক্ষরের ও ইউনিক হতে হবে", "Username must be unique & 3+ chars"),
                                        color = if (isUsernameValidAndUnique) SuccessGreen else MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_username_input")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = emailOrPhone,
                            onValueChange = { emailOrPhone = it },
                            label = { Text(tr(lang, "ইমেইল অথবা ফোন নম্বর (Email or Phone)", "Email or Phone Number")) },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_contact_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Country Dropdown
                        ExposedDropdownMenuBox(
                            expanded = countryDropdownExpanded,
                            onExpandedChange = { countryDropdownExpanded = !countryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCountry,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(tr(lang, "দেশ নির্বাচন (Country)", "Select Country")) },
                                leadingIcon = { Icon(Icons.Outlined.Public, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = countryDropdownExpanded,
                                onDismissRequest = { countryDropdownExpanded = false }
                            ) {
                                countriesList.forEach { countryItem ->
                                    DropdownMenuItem(
                                        text = { Text(countryItem) },
                                        onClick = {
                                            selectedCountry = countryItem
                                            countryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(tr(lang, "পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)", "Password (min 6 chars)")) },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                    Icon(
                                        imageVector = if (showRegPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_password_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(tr(lang, "কনফার্ম পাসওয়ার্ড (Confirm Password)", "Confirm Password")) },
                            leadingIcon = { Icon(Icons.Outlined.LockReset, contentDescription = null) },
                            visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_confirm_password_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { acceptedTerms = !acceptedTerms }
                        ) {
                            Checkbox(
                                checked = acceptedTerms,
                                onCheckedChange = { acceptedTerms = it },
                                modifier = Modifier.testTag("reg_terms_checkbox")
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "আমি NexEarn-এর সকল নিয়মাবলী ও প্রাইভেসি শর্তাবলী মেনে নিচ্ছি।",
                                    "I accept the Terms of Service & Privacy Policy."
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        AnimatedVisibility(visible = regError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = regError ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val err = viewModel.registerNewUser(
                                    fullName = fullName,
                                    username = username,
                                    emailOrPhone = emailOrPhone,
                                    passwordPlain = password,
                                    confirmPasswordPlain = confirmPassword,
                                    country = selectedCountry,
                                    acceptedTerms = acceptedTerms
                                )
                                regError = err
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("reg_submit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr(lang, "রেজিস্ট্রেশন সম্পন্ন করুন (+২০০ কয়েন)", "Create Account (+200 Coins)"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Sign-In via CredentialManager (Firebase Auth Ready)
                    OutlinedButton(
                        onClick = {
                            FirebaseAdminBridge.signInWithGoogle(
                                context = context,
                                scope = scope,
                                onSuccess = { uid, name, email ->
                                    viewModel.loginWithGoogleAccount(uid, name, email)
                                },
                                onError = { msg ->
                                    viewModel.showFeedback(msg, msg)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Google Sign-In",
                            tint = NexBluePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr(lang, "Sign in with Google (Firebase Auth)", "Sign in with Google"),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Password Reset Modal (Section 20)
    if (showForgotPassDialog) {
        var resetId by remember { mutableStateOf("") }
        var newResetPass by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showForgotPassDialog = false },
            title = {
                Text(tr(lang, "পাসওয়ার্ড রিসেট (Password Reset)", "Reset Password"))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        tr(
                            lang,
                            "আপনার রেজিস্টার্ড ইউজারনেম অথবা ইমেইল/ফোন নম্বর এবং নতুন পাসওয়ার্ড দিন:",
                            "Enter your registered username or email/phone and new password:"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = resetId,
                        onValueChange = { resetId = it },
                        label = { Text(tr(lang, "ইউজারনেম / ইমেইল / ফোন", "Username / Email / Phone")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newResetPass,
                        onValueChange = { newResetPass = it },
                        label = { Text(tr(lang, "নতুন পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)", "New Password")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.resetPasswordByIdentifier(resetId, newResetPass)) {
                            showForgotPassDialog = false
                        }
                    }
                ) {
                    Text(tr(lang, "পাসওয়ার্ড আপডেট করুন", "Update Password"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPassDialog = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }
}
