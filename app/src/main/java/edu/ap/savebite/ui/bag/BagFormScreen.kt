package edu.ap.savebite.ui.bag

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.savebite.R
import edu.ap.savebite.ui.components.ErrorMessage
import edu.ap.savebite.ui.components.SaveBiteButton
import edu.ap.savebite.ui.components.SaveBiteSecondaryButton
import edu.ap.savebite.ui.components.SaveBiteTextField
import edu.ap.savebite.ui.theme.ErrorBackground
import edu.ap.savebite.ui.theme.ErrorText
import edu.ap.savebite.ui.theme.Ink
import edu.ap.savebite.ui.theme.PhotoPeach
import edu.ap.savebite.ui.theme.Slate
import edu.ap.savebite.ui.theme.Tomato

/** Which time picker is open. */
private enum class PickupField { START, END }

/** Nieuw pakket (H3, H4): form with photo, prices, quantity and pickup window. */
@Composable
fun BagFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: BagFormViewModel = viewModel(factory = BagFormViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    // The launchers only hand the result to the ViewModel; the photo is processed there.
    // No permission needed for either of them.
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::onPhotoPicked)
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let(viewModel::onPhotoTaken)
    }

    // Only the screen state, so it lives here and not in the ViewModel
    var openPicker by rememberSaveable { mutableStateOf<PickupField?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedIconButton(
                onClick = onBack,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(2.dp, Ink),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.screen_new_bag), style = MaterialTheme.typography.headlineSmall)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PhotoBox(uiState = uiState, onClick = {
                pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            })
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SaveBiteSecondaryButton(
                    text = stringResource(R.string.photo_from_gallery),
                    onClick = {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    enabled = !uiState.isProcessingPhoto
                )
                SaveBiteSecondaryButton(
                    text = stringResource(R.string.photo_from_camera),
                    onClick = { takePhoto.launch(null) },
                    enabled = !uiState.isProcessingPhoto
                )
            }
        }
        uiState.photoError?.let { FieldError(stringResource(it)) }

        SaveBiteTextField(
            label = stringResource(R.string.label_bag_title),
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            placeholder = stringResource(R.string.placeholder_bag_title),
            errorMessage = uiState.titleError?.let { stringResource(it) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )
        SaveBiteTextField(
            label = stringResource(R.string.label_bag_description),
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            placeholder = stringResource(R.string.placeholder_bag_description),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            singleLine = false,
            minLines = 2
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SaveBiteTextField(
                label = stringResource(R.string.label_bag_price),
                value = uiState.price,
                onValueChange = viewModel::onPriceChange,
                placeholder = stringResource(R.string.placeholder_bag_price),
                errorMessage = uiState.priceError?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            SaveBiteTextField(
                label = stringResource(R.string.label_bag_original_value),
                value = uiState.originalValue,
                onValueChange = viewModel::onOriginalValueChange,
                placeholder = stringResource(R.string.placeholder_bag_original_value),
                errorMessage = uiState.originalValueError?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SaveBiteTextField(
                label = stringResource(R.string.label_bag_quantity),
                value = uiState.quantity,
                onValueChange = viewModel::onQuantityChange,
                placeholder = stringResource(R.string.placeholder_bag_quantity),
                errorMessage = uiState.quantityError?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeField(
                label = stringResource(R.string.label_bag_pickup_start),
                minutes = uiState.pickupStart,
                placeholder = stringResource(R.string.placeholder_bag_pickup_start),
                errorMessage = uiState.pickupStartError?.let { stringResource(it) },
                onClick = { openPicker = PickupField.START },
                modifier = Modifier.weight(1f)
            )
            TimeField(
                label = stringResource(R.string.label_bag_pickup_end),
                minutes = uiState.pickupEnd,
                placeholder = stringResource(R.string.placeholder_bag_pickup_end),
                errorMessage = uiState.pickupEndError?.let { stringResource(it) },
                onClick = { openPicker = PickupField.END },
                modifier = Modifier.weight(1f)
            )
        }

        uiState.errorMessage?.let { ErrorMessage(stringResource(it)) }

        SaveBiteButton(
            text = stringResource(R.string.offer_bag),
            onClick = viewModel::save,
            isLoading = uiState.isSaving,
            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
        )
    }

    when (openPicker) {
        PickupField.START -> TimePickerDialog(
            initialMinutes = uiState.pickupStart ?: (17 * 60),
            onConfirm = { viewModel.onPickupStartChange(it); openPicker = null },
            onDismiss = { openPicker = null }
        )
        PickupField.END -> TimePickerDialog(
            initialMinutes = uiState.pickupEnd ?: ((uiState.pickupStart ?: (17 * 60)) + 60).coerceAtMost(23 * 60 + 59),
            onConfirm = { viewModel.onPickupEndChange(it); openPicker = null },
            onDismiss = { openPicker = null }
        )
        null -> Unit
    }
}

/** Photo preview, or a dashed "+ foto" box when there is no photo yet. */
@Composable
private fun PhotoBox(uiState: BagFormUiState, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    val borderColor = if (uiState.photoError != null) Tomato else Ink
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(92.dp)
            .clip(shape)
            .background(PhotoPeach)
            .clickable(enabled = !uiState.isProcessingPhoto, onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx()))
                    )
                )
            }
    ) {
        when {
            uiState.isProcessingPhoto -> CircularProgressIndicator(color = Tomato, strokeWidth = 2.dp)
            uiState.photo != null -> Image(
                bitmap = uiState.photo.asImageBitmap(),
                contentDescription = stringResource(R.string.bag_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().border(2.dp, Ink, shape)
            )
            else -> Text(stringResource(R.string.add_photo), style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Looks like a text field, but opens a time picker instead of the keyboard. */
@Composable
private fun TimeField(
    label: String,
    minutes: Int?,
    placeholder: String,
    errorMessage: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.small
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp))
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(shape)
                .background(if (errorMessage != null) ErrorBackground else Color.White)
                .border(2.dp, if (errorMessage != null) Tomato else Ink, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = minutes?.let(::formatTime) ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = if (minutes != null) Ink else Slate
            )
        }
        errorMessage?.let { FieldError(it, Modifier.padding(start = 16.dp, top = 4.dp)) }
    }
}

@Composable
private fun FieldError(text: String, modifier: Modifier = Modifier) {
    Text(text, color = ErrorText, style = MaterialTheme.typography.bodySmall, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialMinutes: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) {
                Text(stringResource(R.string.ok), color = Ink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Ink) }
        },
        text = { TimePicker(state = state) }
    )
}

private fun formatTime(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
