package edu.ap.savebite.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.savebite.R
import edu.ap.savebite.ui.SaveBiteScreen
import edu.ap.savebite.ui.components.ErrorMessage
import edu.ap.savebite.ui.components.SaveBiteButton
import edu.ap.savebite.ui.theme.Ink
import edu.ap.savebite.ui.theme.Tomato

@Composable
fun StartScreen(
    onDestination: (SaveBiteScreen) -> Unit,
    viewModel: StartViewModel = viewModel(factory = StartViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.destination) {
        uiState.destination?.let(onDestination)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val error = uiState.errorMessage
        if (error == null) {
            CircularProgressIndicator(color = Tomato)
        } else {
            ErrorMessage(stringResource(error))
            SaveBiteButton(text = stringResource(R.string.retry), onClick = viewModel::load)
            TextButton(onClick = viewModel::logout) {
                Text(stringResource(R.string.logout), color = Ink)
            }
        }
    }
}
