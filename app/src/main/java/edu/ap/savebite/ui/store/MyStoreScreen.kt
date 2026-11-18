package edu.ap.savebite.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.savebite.R
import edu.ap.savebite.data.model.StoreCategory
import edu.ap.savebite.ui.components.ErrorMessage
import edu.ap.savebite.ui.components.InfoMessage
import edu.ap.savebite.ui.components.OsmMap
import edu.ap.savebite.ui.components.SaveBiteButton
import edu.ap.savebite.ui.components.SaveBiteTextField
import edu.ap.savebite.ui.theme.ErrorText
import edu.ap.savebite.ui.theme.Ink
import edu.ap.savebite.ui.theme.Mustard
import edu.ap.savebite.ui.theme.Slate
import edu.ap.savebite.ui.theme.Tomato

/** Mijn winkel (H1, H2): the merchant sets name, category and the pin of his store. */
@Composable
fun MyStoreScreen(
    onSaved: () -> Unit,
    viewModel: MyStoreViewModel = viewModel(factory = MyStoreViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Tomato)
        }
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SaveBiteTextField(
                label = stringResource(R.string.label_store_name),
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                errorMessage = uiState.nameError?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.weight(1f)
            )
            CategoryField(
                selected = uiState.category,
                onSelect = viewModel::onCategoryChange,
                modifier = Modifier.weight(1f)
            )
        }

        StoreMap(
            lat = uiState.lat,
            lon = uiState.lon,
            onPinMoved = viewModel::onPinMoved,
            hasError = uiState.pinError != null
        )
        uiState.pinError?.let {
            Text(stringResource(it), color = ErrorText, style = MaterialTheme.typography.bodySmall)
        }

        if (uiState.isLoadingAddress) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(color = Tomato, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Text(stringResource(R.string.address_loading), style = MaterialTheme.typography.bodySmall, color = Slate)
            }
        } else {
            Text(stringResource(R.string.address_hint), style = MaterialTheme.typography.bodySmall, color = Slate)
        }
        uiState.addressMessage?.let { InfoMessage(stringResource(it)) }

        SaveBiteTextField(
            label = stringResource(R.string.label_store_address),
            value = uiState.address,
            onValueChange = viewModel::onAddressChange,
            errorMessage = uiState.addressError?.let { stringResource(it) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SaveBiteTextField(
                label = stringResource(R.string.label_store_postal_code),
                value = uiState.postalCode,
                onValueChange = viewModel::onPostalCodeChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            SaveBiteTextField(
                label = stringResource(R.string.label_store_city),
                value = uiState.city,
                onValueChange = viewModel::onCityChange,
                errorMessage = uiState.cityError?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.weight(1f)
            )
        }

        uiState.errorMessage?.let { ErrorMessage(stringResource(it)) }

        SaveBiteButton(
            text = stringResource(R.string.save_store),
            onClick = viewModel::save,
            isLoading = uiState.isSaving,
            modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
        )
    }
}

/** The map with a rounded border and the hint "Tik of versleep de pin" on top. */
@Composable
private fun StoreMap(lat: Double?, lon: Double?, onPinMoved: (Double, Double) -> Unit, hasError: Boolean) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(shape)
            .border(2.dp, if (hasError) Tomato else Ink, shape)
    ) {
        OsmMap(pinLat = lat, pinLon = lon, onPinMoved = onPinMoved, modifier = Modifier.fillMaxSize())
        Text(
            stringResource(R.string.map_pin_hint),
            style = MaterialTheme.typography.labelMedium,
            color = Ink,
            modifier = Modifier
                .padding(10.dp)
                .background(Mustard, RoundedCornerShape(8.dp))
                .border(2.dp, Ink, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/** Dropdown with the store categories, styled like SaveBiteTextField. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryField(selected: StoreCategory, onSelect: (StoreCategory) -> Unit, modifier: Modifier = Modifier) {
    // Only the screen state, so it lives here and not in the ViewModel
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(modifier) {
        Text(
            stringResource(R.string.label_store_category),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = stringResource(selected.label),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Tomato,
                    unfocusedBorderColor = Ink
                ),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                StoreCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(stringResource(category.label)) },
                        onClick = {
                            onSelect(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
