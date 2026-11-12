package edu.ap.savebite.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.savebite.R
import edu.ap.savebite.data.Role
import edu.ap.savebite.ui.components.ErrorMessage
import edu.ap.savebite.ui.components.SaveBiteButton
import edu.ap.savebite.ui.components.SaveBiteTextField
import edu.ap.savebite.ui.components.hardShadow
import edu.ap.savebite.ui.theme.Ink
import edu.ap.savebite.ui.theme.Mustard

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: (Role) -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registeredRole) {
        uiState.registeredRole?.let(onRegistered)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.back), color = Ink, style = MaterialTheme.typography.labelLarge)
        }
        Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium)

        SaveBiteTextField(
            label = stringResource(R.string.label_name),
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            placeholder = stringResource(R.string.placeholder_name),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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
            placeholder = stringResource(R.string.placeholder_password),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            isPassword = true
        )
        SaveBiteTextField(
            label = stringResource(R.string.label_password_confirm),
            value = uiState.passwordConfirm,
            onValueChange = viewModel::onPasswordConfirmChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            isPassword = true
        )

        Text(stringResource(R.string.label_role), style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.selectableGroup()) {
            RoleCard(
                title = stringResource(R.string.role_customer),
                subtitle = stringResource(R.string.role_customer_hint),
                selected = uiState.role == Role.CUSTOMER,
                onClick = { viewModel.onRoleChange(Role.CUSTOMER) },
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                title = stringResource(R.string.role_merchant),
                subtitle = stringResource(R.string.role_merchant_hint),
                selected = uiState.role == Role.MERCHANT,
                onClick = { viewModel.onRoleChange(Role.MERCHANT) },
                modifier = Modifier.weight(1f)
            )
        }

        uiState.errorMessage?.let { ErrorMessage(stringResource(it)) }

        Spacer(Modifier.height(8.dp))
        SaveBiteButton(
            text = stringResource(R.string.screen_register),
            onClick = viewModel::register,
            isLoading = uiState.isLoading
        )
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected) Mustard else Color.White,
        contentColor = Ink,
        border = BorderStroke(2.dp, Ink),
        modifier = modifier
            .then(if (selected) Modifier.hardShadow(offset = 3.dp) else Modifier)
            .selectable(selected = selected, onClick = onClick, role = SemanticsRole.RadioButton)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
