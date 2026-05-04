package com.example.vineguard.LogInScreen.presentation

import android.service.carrier.MessagePdu
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentBrightGreen
import com.example.vineguard.ui.theme.AccentTextColor
import com.example.vineguard.ui.theme.LvlRed
import com.example.vineguard.ui.theme.Primary
import com.example.vineguard.ui.theme.PrimaryTextColor
import com.example.vineguard.ui.theme.SecondaryTextColor

@Composable
fun LogInScreen(
    modifier : Modifier = Modifier,
    state: SignInState,
    onSingInClick:() -> Unit
){
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let{ error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()

        }
    }
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var isVisible by remember {
        mutableStateOf(false)
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentBrightGreen)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ph),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(30.dp).align(alignment = Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "GrapeGuard",
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = AccentTextColor
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Protecting your vineyard with AI",
                fontSize = 14.sp,
                color = PrimaryTextColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "Welcome Back",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = AccentTextColor
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Sign in to monitor your grape health",
                fontSize = 14.sp,
                color = PrimaryTextColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Email",
                fontSize = 14.sp,
                color = AccentTextColor,
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "Enter your email",
                        fontSize = 16.sp,
                        color = SecondaryTextColor
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.mail),
                        contentDescription = null,
                        tint = SecondaryTextColor,
                        modifier = Modifier.size(25.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SecondaryTextColor
                )

            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Password",
                fontSize = 14.sp,
                color = AccentTextColor,
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "Enter your password",
                        fontSize = 16.sp,
                        color = SecondaryTextColor
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {isVisible = !isVisible}
                    ) {
                        Icon(
                            painter = painterResource(if (!isVisible) R.drawable.visible else R.drawable.not_visible),
                            contentDescription = null,
                            tint = SecondaryTextColor,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if(isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SecondaryTextColor
                )
            )
            Text(
                text = "Forgot Password?",
                fontSize = 14.sp,
                color = AccentBrightGreen,
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                textAlign = TextAlign.End
            )
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBrightGreen,
                    contentColor = Primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .width(1.dp),
                    color = SecondaryTextColor
                )
                Text(
                    text = " or continue with ",
                    fontSize = 14.sp,
                    color = SecondaryTextColor,
                )
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .width(1.dp),
                    color = SecondaryTextColor
                )
            }
            OutlinedButton(
                onClick = onSingInClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(color = SecondaryTextColor, width = 1.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.google),
                    contentDescription = null,
                    tint = LvlRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Continue with Google",
                    fontSize = 16.sp,
                    color = AccentTextColor,
                    modifier = Modifier.padding(top = 5.dp,bottom = 5.dp, start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(25.dp))
            Divider(
                color = SecondaryTextColor,
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(15.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "Don't have an account?",
                    fontSize = 16.sp,
                    color = SecondaryTextColor,
                )
                Text(
                    text = " Sign Up",
                    fontSize = 16.sp,
                    color = AccentBrightGreen,
                )
            }
        }
    }
}
