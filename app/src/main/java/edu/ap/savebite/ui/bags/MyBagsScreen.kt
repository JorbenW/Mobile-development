package edu.ap.savebite.ui.bags

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.savebite.R
import edu.ap.savebite.ui.components.ErrorMessage
import edu.ap.savebite.ui.components.InfoMessage
import edu.ap.savebite.ui.components.SaveBiteButton
import edu.ap.savebite.ui.components.SaveBiteSecondaryButton
import edu.ap.savebite.ui.components.StatusBadge
import edu.ap.savebite.ui.components.hardShadow
import edu.ap.savebite.ui.theme.Cream
import edu.ap.savebite.ui.theme.CreamDark
import edu.ap.savebite.ui.theme.Ink
import edu.ap.savebite.ui.theme.Mustard
import edu.ap.savebite.ui.theme.PhotoPeach
import edu.ap.savebite.ui.theme.Slate
import edu.ap.savebite.ui.theme.StatusGreen
import edu.ap.savebite.ui.theme.Tomato

/** Mijn pakketten (H5): own bags with reserved and left, realtime. */
@Composable
fun MyBagsScreen(
    onNewBag: () -> Unit,
    onGoToStore: () -> Unit,
    viewModel: MyBagsViewModel = viewModel(factory = MyBagsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            // shortcut: "Code check" (H6) from the wireframe comes in week 4
            SaveBiteButton(
                text = stringResource(R.string.new_bag),
                onClick = {
                    if (uiState.hasStore == false) viewModel.onNewBagWithoutStore() else onNewBag()
                }
            )
            if (uiState.showNoStoreMessage) {
                InfoMessage(stringResource(R.string.no_store_yet))
                SaveBiteSecondaryButton(text = stringResource(R.string.go_to_my_store), onClick = onGoToStore)
            }
            uiState.errorMessage?.let { ErrorMessage(stringResource(it)) }
        }

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Tomato)
            }
            uiState.bags.isEmpty() && uiState.errorMessage == null -> Text(
                stringResource(R.string.no_bags_yet),
                style = MaterialTheme.typography.bodyLarge,
                color = Slate,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(32.dp)
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 18.dp, end = 22.dp, top = 10.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // key: the list changes realtime, so Compose must know which row is which
                items(uiState.bags, key = { it.id }) { bag -> BagRow(bag) }
            }
        }
    }
}

/** One bag as a card, as in the wireframe (based on FruitItem in lab Fruits 2). */
@Composable
private fun BagRow(bag: BagItem) {
    val shape = MaterialTheme.shapes.large
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(cornerRadius = 16.dp)
            .background(Color.White, shape)
            .border(2.dp, Ink, shape)
            .padding(10.dp)
    ) {
        BagPhoto(bag)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                Text(
                    bag.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(stringResource(bag.status.label), background = bag.status.color())
            }
            Text(
                stringResource(R.string.pickup_window, bag.pickupWindow),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                Count(bag.reserved, stringResource(R.string.reserved))
                Count(bag.left, stringResource(R.string.left))
            }
        }
    }
}

@Composable
private fun BagPhoto(bag: BagItem) {
    val shape = MaterialTheme.shapes.small
    val modifier = Modifier.size(64.dp).clip(shape).border(2.dp, Ink, shape)
    if (bag.photo != null) {
        Image(
            bitmap = bag.photo.asImageBitmap(),
            contentDescription = stringResource(R.string.bag_photo),
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier.background(PhotoPeach))
    }
}

/** A big number with a word after it, e.g. "2 gereserveerd". */
@Composable
private fun Count(number: Int, label: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(number.toString(), style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 2.dp))
    }
}

private fun BagStatus.color(): Color = when (this) {
    BagStatus.ACTIVE -> StatusGreen
    BagStatus.SOLD_OUT -> Mustard
    BagStatus.OFFLINE -> Cream
    BagStatus.ENDED -> CreamDark
}
