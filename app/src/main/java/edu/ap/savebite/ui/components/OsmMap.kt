package edu.ap.savebite.ui.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/** Default center when there is no pin yet: Antwerp. */
private val ANTWERP = GeoPoint(51.2194, 4.4025)

/**
 * OSMDroid map with one pin (based on lab OSM).
 * The pin is set with a tap and can be dragged. [onPinMoved] is only called when
 * the pin is placed or released, so the screen asks Nominatim once per move.
 *
 * @param pinLat latitude of the pin, or null when there is no pin yet
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun OsmMap(
    pinLat: Double?,
    pinLon: Double?,
    onPinMoved: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier,
    zoom: Double = 17.0
) {
    val context = LocalContext.current
    // The listeners below are created once, so always call the latest callback
    val currentOnPinMoved by rememberUpdatedState(onPinMoved)

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(zoom)
            controller.setCenter(ANTWERP)
            // © OpenStreetMap contributors, required by the OSM tile usage policy
            overlays.add(CopyrightOverlay(context))
            // Tap on the map: place the pin there
            overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    currentOnPinMoved(p.latitude, p.longitude)
                    return true
                }

                override fun longPressHelper(p: GeoPoint) = false
            }))
            // The screen scrolls: keep vertical swipes on the map for the map itself
            setOnTouchListener { view, _ ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
    }
    val marker = remember {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            isDraggable = true
            setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                override fun onMarkerDrag(marker: Marker) = Unit
                override fun onMarkerDragStart(marker: Marker) = Unit

                // Only when the pin is released, not during the drag
                override fun onMarkerDragEnd(marker: Marker) {
                    currentOnPinMoved(marker.position.latitude, marker.position.longitude)
                }
            })
        }
    }

    // Lifecycle of the MapView (lab OSM, there still in comments):
    // stop loading tiles in the background and free memory when the screen disappears.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        // Runs again when the pin in the UiState changes, also after a rotation
        update = { map ->
            if (pinLat != null && pinLon != null) {
                val point = GeoPoint(pinLat, pinLon)
                marker.position = point
                if (!map.overlays.contains(marker)) map.overlays.add(marker)
                // A loaded store or a new map after rotation: bring the pin into view
                if (!map.boundingBox.contains(point)) map.controller.setCenter(point)
            } else {
                map.overlays.remove(marker)
            }
            map.invalidate()
        },
        modifier = modifier
    )
}
