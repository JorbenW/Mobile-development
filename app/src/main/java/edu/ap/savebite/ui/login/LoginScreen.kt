package edu.ap.savebite.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.savebite.R
import edu.ap.savebite.ui.components.ErrorMessage
import edu.ap.savebite.ui.components.InfoMessage
import edu.ap.savebite.ui.components.SaveBiteButton
import edu.ap.savebite.ui.components.SaveBiteTextField
import edu.ap.savebite.ui.theme.Cream
import edu.ap.savebite.ui.theme.Ink
import edu.ap.savebite.ui.theme.Mustard
import edu.ap.savebite.ui.theme.Slate
import edu.ap.savebite.ui.theme.Tomato

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoggedIn()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Logo(Modifier.align(Alignment.CenterHorizontally))
        Text(
            stringResource(R.string.tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = Slate,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        SaveBiteTextField(
            label = stringResource(R.string.label_email),
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = stringResource(R.string.placeholder_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        SaveBiteTextField(
            label = stringResource(R.string.label_password),
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            isPassword = true
        )

        uiState.errorMessage?.let { ErrorMessage(stringResource(it)) }
        uiState.resetSentTo?.let { InfoMessage(stringResource(R.string.reset_sent, it)) }

        SaveBiteButton(
            text = stringResource(R.string.screen_login),
            onClick = viewModel::login,
            isLoading = uiState.isLoading
        )
        TextButton(
            onClick = viewModel::forgotPassword,
            enabled = !uiState.isLoading,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.forgot_password), color = Ink)
        }

        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onRegisterClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.no_account))
                    append(" ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.screen_register))
                    }
                },
                color = Ink
            )
        }
    }
}

/** Yellow circle with a bite taken out, next to "SaveBite". */
@Composable
private fun Logo(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Mustard)
                .border(2.dp, Ink, CircleShape)
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .offset(x = 36.dp, y = (-12).dp)
                    .background(Cream, CircleShape)
                    .border(2.dp, Ink, CircleShape)
            )
        }
        Text(
            buildAnnotatedString {
                append("Save")
                withStyle(SpanStyle(color = Tomato)) { append("Bite") }
            },
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp)
        )
    }
}
