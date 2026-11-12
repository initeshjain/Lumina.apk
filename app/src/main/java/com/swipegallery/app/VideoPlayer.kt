package com.swipegallery.app

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ZoomInMap
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Rich full-screen video player with:
 * - Pinch-to-zoom and pan on video surface
 * - Double tap to zoom or seek
 * - Tap to toggle controls visibility
 * - Background color for controls
 * - Speed, aspect ratio, repeat, mute, seek bar, skip 10s, info details
 */
@OptIn(UnstableApi::class)
@Composable
internal fun VideoPlayer(
    video: Photo,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onClose: () -> Unit,
    onShowDetails: ((Photo) -> Unit)? = null
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(video.durationMs.coerceAtLeast(0L)) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var isMuted by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(Player.REPEAT_MODE_OFF) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var speedMenuOpen by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    // Controls visibility state
    var controlsVisible by remember { mutableStateOf(true) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderTempPosition by remember { mutableFloatStateOf(0f) }

    // Finger zoom and pan states on video
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val player = remember(video.id) {
        ExoPlayer.Builder(context)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(video.uri))
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    totalDuration = player.duration.coerceAtLeast(0L)
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { player.pause() }

    // Auto-update playback progress
    LaunchedEffect(player, isDraggingSlider) {
        while (true) {
            if (!isDraggingSlider) {
                currentPosition = player.currentPosition.coerceAtLeast(0L)
                bufferedPosition = player.bufferedPosition.coerceAtLeast(0L)
                if (player.duration > 0) totalDuration = player.duration
            }
            delay(200)
        }
    }

    // Auto-hide controls after 4 seconds of playback
    LaunchedEffect(controlsVisible, isPlaying, isDraggingSlider, speedMenuOpen) {
        if (controlsVisible && isPlaying && !isDraggingSlider && !speedMenuOpen) {
            delay(4000)
            controlsVisible = false
        }
    }

    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val containerWidth = maxWidth.value
        val containerHeight = maxHeight.value

        // Video Surface container with finger zoom & pan
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(video.id) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                        scale = newScale
                        offset = if (newScale <= 1.02f) {
                            Offset.Zero
                        } else {
                            val maxOffsetX = (containerWidth * (newScale - 1f)) / 2f
                            val maxOffsetY = (containerHeight * (newScale - 1f)) / 2f
                            Offset(
                                x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        }
                    }
                }
                .pointerInput(video.id) {
                    detectTapGestures(
                        onTap = {
                            controlsVisible = !controlsVisible
                        },
                        onDoubleTap = { tapOffset ->
                            if (scale > 1.05f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                // Double tap left side rewinds 10s, right side forwards 10s, center zooms
                                val touchX = tapOffset.x
                                val screenThird = size.width / 3f
                                when {
                                    touchX < screenThird -> {
                                        player.seekTo((player.currentPosition - 10000L).coerceAtLeast(0L))
                                    }
                                    touchX > screenThird * 2f -> {
                                        player.seekTo((player.currentPosition + 10000L).coerceAtMost(player.duration))
                                    }
                                    else -> {
                                        scale = 2.5f
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        this.resizeMode = resizeMode
                        this.player = player
                    }
                },
                update = { view ->
                    view.player = player
                    view.resizeMode = resizeMode
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )
        }

        // Indicator when zoomed
        if (scale > 1.05f) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 70.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable {
                        scale = 1f
                        offset = Offset.Zero
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ZoomInMap, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${String.format(java.util.Locale.US, "%.1fx", scale)} · Tap to reset",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Animated controls overlay with background colors
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(Modifier.fillMaxSize()) {

                // Top Bar with background color
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 0.60f),
                                    Color.Transparent
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(0.45f))
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Close video", tint = Color.White)
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                video.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (totalDuration > 0) {
                                Text(
                                    formatDuration(totalDuration),
                                    color = Color.White.copy(0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Speed dropdown button
                        Box {
                            IconButton(
                                onClick = { speedMenuOpen = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(0.45f))
                            ) {
                                Text(
                                    if (playbackSpeed == 1.0f) "1x" else "${playbackSpeed}x",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DropdownMenu(
                                expanded = speedMenuOpen,
                                onDismissRequest = { speedMenuOpen = false }
                            ) {
                                speeds.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text("${s}x speed", fontWeight = if (s == playbackSpeed) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            playbackSpeed = s
                                            player.playbackParameters = PlaybackParameters(s)
                                            speedMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(6.dp))

                        // Resize mode toggle (Fit vs Fill)
                        IconButton(
                            onClick = {
                                resizeMode = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                } else {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(0.45f))
                        ) {
                            Icon(
                                if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) Icons.Rounded.AspectRatio else Icons.Rounded.FitScreen,
                                "Aspect ratio",
                                tint = Color.White
                            )
                        }

                        if (onToggleFavorite != null) {
                            Spacer(Modifier.width(6.dp))
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(0.45f))
                            ) {
                                Icon(
                                    if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    "Favorite",
                                    tint = if (isFavorite) Color(0xFFE53935) else Color.White
                                )
                            }
                        }

                        if (onShowDetails != null) {
                            Spacer(Modifier.width(6.dp))
                            // Info "i" icon button
                            IconButton(
                                onClick = { onShowDetails(video) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(0.45f))
                            ) {
                                Icon(Icons.Rounded.Info, "Details", tint = Color.White)
                            }
                        }
                    }
                }

                // Center Play/Pause & Quick Seek Buttons
                Row(
                    Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replay 10s
                    FilledIconButton(
                        onClick = {
                            player.seekTo((player.currentPosition - 10000L).coerceAtLeast(0L))
                        },
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(0.6f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Rounded.Replay10, "Rewind 10 seconds", modifier = Modifier.size(28.dp))
                    }

                    // Main Play/Pause Button
                    FilledIconButton(
                        onClick = {
                            if (player.isPlaying) player.pause() else player.play()
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White.copy(0.92f),
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    // Forward 10s
                    FilledIconButton(
                        onClick = {
                            player.seekTo((player.currentPosition + 10000L).coerceAtMost(player.duration))
                        },
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(0.6f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Rounded.Forward10, "Forward 10 seconds", modifier = Modifier.size(28.dp))
                    }
                }

                // Bottom Controls with background color scrim
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.70f),
                                    Color.Black.copy(alpha = 0.90f)
                                )
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        // Slider & Timers
                        val positionToShow = if (isDraggingSlider) sliderTempPosition.toLong() else currentPosition
                        val sliderMax = totalDuration.coerceAtLeast(1L).toFloat()
                        val sliderValue = positionToShow.toFloat().coerceIn(0f, sliderMax)

                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                formatDuration(positionToShow),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Slider(
                                value = sliderValue,
                                onValueChange = {
                                    isDraggingSlider = true
                                    sliderTempPosition = it
                                },
                                onValueChangeFinished = {
                                    player.seekTo(sliderTempPosition.toLong())
                                    isDraggingSlider = false
                                },
                                valueRange = 0f..sliderMax,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(0.3f)
                                )
                            )

                            Text(
                                formatDuration(totalDuration),
                                color = Color.White.copy(0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Bottom Actions Row
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mute toggle
                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    player.volume = if (isMuted) 0f else 1f
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(0.4f))
                            ) {
                                Icon(
                                    if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                                    "Mute toggle",
                                    tint = Color.White
                                )
                            }

                            // Repeat mode toggle
                            IconButton(
                                onClick = {
                                    repeatMode = when (repeatMode) {
                                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                                        Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                                        else -> Player.REPEAT_MODE_OFF
                                    }
                                    player.repeatMode = repeatMode
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (repeatMode != Player.REPEAT_MODE_OFF) Color.White.copy(0.25f) else Color.Black.copy(0.4f))
                            ) {
                                Icon(
                                    when (repeatMode) {
                                        Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                                        else -> Icons.Rounded.Repeat
                                    },
                                    "Repeat mode",
                                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) GreenDark else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
