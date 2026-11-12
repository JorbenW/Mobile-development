package edu.ap.savebite.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import edu.ap.savebite.ui.theme.ErrorBackground
import edu.ap.savebite.ui.theme.ErrorText
import edu.ap.savebite.ui.theme.Ink
import edu.ap.savebite.ui.theme.Tomato

/** The hard offset shadow from the wireframes, drawn behind the element. */
fun Modifier.hardShadow(offset: Dp = 4.dp, cornerRadius: Dp = 14.dp) = drawBehind {
    drawRoundRect(
        color = Ink,
        topLeft = Offset(offset.toPx(), offset.toPx()),
        size = size,
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

/** Main red button. Shows a spinner instead of the text while [isLoading]. */
@Composable
fun SaveBiteButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(2.dp, Ink),
        colors = ButtonDefaults.buttonColors(
            containerColor = Tomato,
            contentColor = Color.White,
            // Keep the brand color while loading, the spinner shows the state
            disabledContainerColor = Tomato,
            disabledContentColor = Color.White
        ),
        modifier = modifier.fillMaxWidth().height(52.dp).hardShadow()
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

/** Text field with the label above the box, as in the wireframes. */
@Composable
fun SaveBiteTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Tomato,
                unfocusedBorderColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Red error box below a form. */
@Composable
fun ErrorMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = ErrorText,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .fillMaxWidth()
            .background(ErrorBackground, MaterialTheme.shapes.small)
            .border(2.dp, Tomato, MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    )
}
