package com.echostream.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.wear.compose.material3.Icon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.Text
import com.echostream.data.db.TrackEntity
import com.echostream.data.model.SearchResult
import com.echostream.ui.theme.AccentAlpha10
import com.echostream.ui.theme.AccentAlpha20
import com.echostream.ui.theme.AccentDark
import com.echostream.ui.theme.AccentGlow
import com.echostream.ui.theme.AccentLight
import com.echostream.ui.theme.AccentPrimary
import com.echostream.ui.theme.AmberLight
import com.echostream.ui.theme.AmberPrimary
import com.echostream.ui.theme.BgDark
import com.echostream.ui.theme.BgSurface
import com.echostream.ui.theme.CardBgLight
import com.echostream.ui.theme.ErrorRed
import com.echostream.ui.theme.PlayerBgEnd
import com.echostream.ui.theme.PlayerBgStart
import com.echostream.ui.theme.SurfaceElevated
import com.echostream.ui.theme.SurfaceLight
import com.echostream.ui.theme.TextPrimary
import com.echostream.ui.theme.TextSecondary
import com.echostream.ui.theme.TextTertiary
import com.echostream.ui.theme.WhiteOverlay10
import com.echostream.ui.theme.WhiteOverlay20
import com.echostream.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════
//  Icon imports (Material Design Icons)
// ═══════════════════════════════════════════════
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious

// ═══════════════════════════════════════════════
//  SEARCH SCREEN
// ═══════════════════════════════════════════════

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    onTrackSelected: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearchLoading by viewModel.isSearchLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(top = 28.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            item {
                Text(
                    text = "EchoStream",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                    color = AccentPrimary
                )
            }

            // Search input
            item {
                Spacer(Modifier.height(8.dp))
                SearchInput(
                    query = query,
                    onQueryChange = { query = it },
                    focusRequester = focusRequester,
                    onSearch = {
                        val trimmed = query.trim()
                        if (trimmed.isNotEmpty()) viewModel.performSearch(trimmed)
                    }
                )
            }

            // Action buttons
            item {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val trimmed = query.trim()
                            if (trimmed.isNotEmpty()) {
                                viewModel.performSearch(trimmed)
                            }
                        },
                        enabled = query.isNotBlank()
                    ) {
                        Text("Search", maxLines = 1)
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenLibrary
                    ) {
                        Text("Library", maxLines = 1)
                    }
                }
            }

            // Loading state
            if (isSearchLoading) {
                item {
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                }
            }

            // Error state
            errorMessage?.let { message ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = ErrorRed,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Empty state
            if (!isSearchLoading && searchResults.isEmpty() && errorMessage == null) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = IconMusicNote,
                            fontSize = 28.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Search for music",
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Try typing a song or artist",
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Results
            items(searchResults) { result ->
                Spacer(Modifier.height(4.dp))
                SearchResultCard(
                    result = result,
                    onClick = {
                        viewModel.playTrack(result)
                        onTrackSelected()
                    }
                )
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onSearch: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "searchGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderColor = AccentPrimary.copy(alpha = glowAlpha)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceLight, RoundedCornerShape(20.dp))
            .padding(1.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(SurfaceElevated, RoundedCornerShape(19.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().matchParentSize()) {
            val strokeW = 2.dp.toPx()
            drawRoundRect(
                color = borderColor,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                style = Stroke(width = strokeW)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = TextSecondary
            )

            Spacer(Modifier.width(8.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .focusable(),
                singleLine = true,
                textStyle = TextStyle(
                    color = TextPrimary,
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isBlank()) {
                            Text(
                                text = "Song or artist",
                                color = TextTertiary,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear search",
                    modifier = Modifier
                        .clickable { onQueryChange("") }
                        .padding(4.dp)
                        .size(16.dp),
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardBgLight)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentAlpha20),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AccentPrimary
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = "${result.channelName} \u2022 ${result.formatDuration()}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play ${result.title}",
                modifier = Modifier.size(16.dp),
                tint = AccentPrimary
            )
        }
    }
}

// ═══════════════════════════════════════════════
//  PLAYER SCREEN
// ═══════════════════════════════════════════════

@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isTrackSaved by viewModel.isTrackSaved.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val progress by remember(currentPosition, duration) {
        derivedStateOf { if (duration > 0) (currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f }
    }

    // Single consolidated InfiniteTransition for all player animations
    val playerTransition = rememberInfiniteTransition(label = "player")
    val bgProgress by playerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgProgress"
    )
    val ringRotation by playerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 8000 else 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    val bgColor = lerp(PlayerBgStart, PlayerBgEnd, bgProgress)

    // Focus for rotary scroll
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(top = 28.dp, bottom = 8.dp)
                .onRotaryScrollEvent { event ->
                    val delta = event.verticalScrollPixels
                    if (duration > 0) {
                        val seekMs = (currentPosition + (delta * 150).toLong())
                            .coerceIn(0L, duration)
                        viewModel.seekTo(seekMs)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            // Album art visualization
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val glowWidth = 3.dp.toPx()
                    drawCircle(
                        color = AccentPrimary.copy(alpha = if (isPlaying) 0.3f else 0.1f),
                        radius = size.minDimension / 2 - glowWidth * 2,
                        style = Stroke(width = glowWidth * 2)
                    )
                }

                // Decorative rotating rings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRotatingRings(ringRotation, isPlaying)
                }

                // Album art circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    AccentPrimary,
                                    AccentLight,
                                    AccentDark,
                                    AccentPrimary,
                                    AccentPrimary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        drawMusicNote()
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Track title
            currentTrack?.let { track ->
                Text(
                    text = track.title,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = track.channelName,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            } ?: run {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Choose a song",
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(8.dp))

            // Progress arc
            if (duration > 0) {
                ProgressArc(progress = progress, isPlaying = isPlaying)

                Spacer(Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(currentPosition),
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = formatMs(duration),
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Error message
            errorMessage?.let { message ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message,
                    color = ErrorRed,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // Play/Pause button
            PlayPauseButton(
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                onClick = { viewModel.togglePlayPause() }
            )

            Spacer(Modifier.height(8.dp))

            // Action buttons row
            if (currentTrack != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ActionButton(
                        icon = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous track",
                        onClick = { viewModel.skipToPrevious() },
                        modifier = Modifier.weight(1f)
                    )

                    ActionButton(
                        icon = if (isTrackSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        isActive = isTrackSaved,
                        contentDescription = if (isTrackSaved) "Remove from library" else "Save to library",
                        onClick = {
                            val track = currentTrack ?: return@ActionButton
                            if (isTrackSaved) {
                                viewModel.savedTracks.value.find { it.videoId == track.videoId }?.let {
                                    viewModel.deleteTrack(it)
                                }
                            } else {
                                viewModel.saveTrack(
                                    SearchResult(
                                        videoId = track.videoId,
                                        title = track.title,
                                        channelName = track.channelName,
                                        durationSeconds = track.durationSeconds,
                                        thumbnailUrl = track.thumbnailUrl
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ActionButton(
                        icon = Icons.Filled.SkipNext,
                        contentDescription = "Next track",
                        onClick = { viewModel.skipToNext() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Navigation handled by swipe-to-dismiss

            Spacer(Modifier.height(8.dp))
        }
    }

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
    }
}

@Composable
private fun ProgressArc(progress: Float, isPlaying: Boolean) {
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            val arcSize = size.minDimension - strokeWidth

            // Track arc
            drawArc(
                color = WhiteOverlay10,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )

            // Progress arc
            val glowColor = if (isPlaying) AccentPrimary else AccentDark
            drawArc(
                color = glowColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )

            // Glow dot at head
            if (progress > 0.01f && progress < 0.99f) {
                val headAngle = -90f + 360f * progress
                val headRad = Math.toRadians(headAngle.toDouble())
                val headRadius = arcSize / 2
                val cx = size.width / 2 + headRadius * cos(headRad).toFloat()
                val cy = size.height / 2 + headRadius * sin(headRad).toFloat()

                drawCircle(
                    color = AccentGlow.copy(alpha = 0.6f),
                    radius = strokeWidth * 1.5f,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.size(64.dp),
        onClick = onClick,
        enabled = !isBuffering,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentPrimary
        )
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "playPauseAnim"
        ) { playing ->
            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playing) "Pause" else "Play",
                        modifier = Modifier.size(28.dp),
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) AccentAlpha20 else WhiteOverlay10
    val fg = if (isActive) AccentPrimary else TextSecondary

    Button(
        onClick = onClick,
        modifier = modifier.size(48.dp, 44.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = fg
        )
    }
}

// ═══════════════════════════════════════════════
//  LIBRARY SCREEN
// ═══════════════════════════════════════════════

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onTrackSelected: () -> Unit,
    onBack: () -> Unit
) {
    val savedTracks by viewModel.savedTracks.collectAsState()
    val listState = rememberScalingLazyListState()

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(top = 28.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                    color = TextPrimary
                )
            }

            // Subtitle
            item {
                Text(
                    text = if (savedTracks.size == 1) "1 song saved"
                    else "${savedTracks.size} songs saved",
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Empty state
            if (savedTracks.isEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = AccentPrimary
            )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No saved songs yet",
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Save songs from the player",
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Tracks
            items(savedTracks) { track ->
                Spacer(Modifier.height(4.dp))
                SavedTrackCard(
                    track = track,
                    onClick = {
                        viewModel.playTrack(track.toSearchResult())
                        onTrackSelected()
                    },
                    onDelete = { viewModel.deleteTrack(track) }
                )
            }



            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun SavedTrackCard(
    track: TrackEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardBgLight)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentAlpha10),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AccentPrimary
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = track.channelName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Delete button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(WhiteOverlay10)
                    .clickable(onClick = onDelete)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Delete ${track.title}",
                    modifier = Modifier.size(14.dp),
                    tint = TextTertiary
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  SHARED DRAWING UTILITIES
// ═══════════════════════════════════════════════

private fun DrawScope.drawRotatingRings(rotation: Float, isPlaying: Boolean) {
    val maxR = size.minDimension / 2 - 2.dp.toPx()
    val ringAlpha = if (isPlaying) 0.25f else 0.08f

    drawCircle(
        color = AccentPrimary.copy(alpha = ringAlpha),
        radius = maxR,
        style = Stroke(width = 1.5.dp.toPx())
    )

    val segments = 3
    for (i in 0 until segments) {
        val startAngle = rotation + (i * 360f / segments)
        drawArc(
            color = AccentLight.copy(alpha = ringAlpha * 2),
            startAngle = startAngle,
            sweepAngle = 60f,
            useCenter = false,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(
                size.width - 4.dp.toPx(),
                size.height - 4.dp.toPx()
            )
        )
    }
}

private fun DrawScope.drawMusicNote() {
    val strokeWidth = 2.dp.toPx()
    val c = center
    val noteSize = size.minDimension * 0.5f

    val leftX = c.x - noteSize * 0.25f
    val topY = c.y - noteSize * 0.3f
    val bottomY = c.y + noteSize * 0.4f

    drawLine(
        color = Color.White,
        start = Offset(leftX, topY),
        end = Offset(leftX, bottomY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawOval(
        color = Color.White,
        topLeft = Offset(leftX - noteSize * 0.15f, bottomY - noteSize * 0.1f),
        size = androidx.compose.ui.geometry.Size(noteSize * 0.25f, noteSize * 0.15f)
    )

    val rightX = c.x + noteSize * 0.2f
    val rightTopY = c.y - noteSize * 0.4f
    val rightBottomY = c.y + noteSize * 0.3f

    drawLine(
        color = Color.White,
        start = Offset(rightX, rightTopY),
        end = Offset(rightX, rightBottomY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawOval(
        color = Color.White,
        topLeft = Offset(rightX - noteSize * 0.15f, rightBottomY - noteSize * 0.1f),
        size = androidx.compose.ui.geometry.Size(noteSize * 0.25f, noteSize * 0.15f)
    )
    drawLine(
        color = Color.White,
        start = Offset(leftX, topY),
        end = Offset(rightX, rightTopY),
        strokeWidth = strokeWidth * 0.8f,
        cap = StrokeCap.Round
    )
}

// ═══════════════════════════════════════════════
//  HELPERS
// ═══════════════════════════════════════════════

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun TrackEntity.toSearchResult(): SearchResult = SearchResult(
    videoId = videoId,
    title = title,
    channelName = channelName,
    durationSeconds = durationSeconds,
    thumbnailUrl = thumbnailUrl
)
