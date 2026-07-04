package com.echostream.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.echostream.data.db.TrackEntity
import com.echostream.data.model.SearchResult
import com.echostream.ui.theme.CardBGLight
import com.echostream.ui.theme.ErrorRed
import com.echostream.ui.theme.GreenAlpha10
import com.echostream.ui.theme.GreenAlpha20
import com.echostream.ui.theme.GreenDark
import com.echostream.ui.theme.GreenGlow
import com.echostream.ui.theme.GreenLight
import com.echostream.ui.theme.PlayerBgEnd
import com.echostream.ui.theme.PlayerBgStart
import com.echostream.ui.theme.SpotifyGreen
import com.echostream.ui.theme.SurfaceElevated
import com.echostream.ui.theme.SurfaceLight
import com.echostream.ui.theme.TextDim
import com.echostream.ui.theme.TextGray
import com.echostream.ui.theme.TextWhite
import com.echostream.ui.theme.WhiteOverlay10
import com.echostream.ui.theme.WhiteOverlay20
import com.echostream.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════
//  SEARCH SCREEN
// ═══════════════════════════════════════════════

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    onTrackSelected: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearchLoading by viewModel.isSearchLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = { TimeText(timeTextStyle = TimeTextDefaults.timeTextStyle(color = TextGray)) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "EchoStream",
                    style = MaterialTheme.typography.title1,
                    textAlign = TextAlign.Center,
                    color = SpotifyGreen
                )
            }

            // Animated search input
            item {
                Spacer(Modifier.height(6.dp))
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

            // Voice search hint
            item {
                Spacer(Modifier.height(4.dp))
                VoiceSearchHint()
            }

            // Action buttons
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val trimmedQuery = query.trim()
                            if (trimmedQuery.isNotEmpty()) {
                                viewModel.performSearch(trimmedQuery)
                            }
                        },
                        enabled = query.isNotBlank()
                    ) {
                        Text("Search", maxLines = 1)
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenLibrary,
                        colors = ButtonDefaults.secondaryButtonColors()
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
                        style = MaterialTheme.typography.caption1,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Empty state
            if (!isSearchLoading && searchResults.isEmpty() && errorMessage == null) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Search for music",
                            color = TextDim,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.body2
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Try typing a song or artist",
                            color = TextDim,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.caption1
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

    val borderColor = SpotifyGreen.copy(alpha = glowAlpha)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceLight, RoundedCornerShape(20.dp))
            .padding(1.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(SurfaceElevated, RoundedCornerShape(19.dp))
    ) {
        // Glow border painted on a Canvas overlay
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
            // Search icon
            Text(
                text = "\uD83D\uDD0D",
                fontSize = 14.sp
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
                    color = TextWhite,
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isBlank()) {
                            Text(
                                text = "Song or artist",
                                color = TextDim,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (query.isNotEmpty()) {
                Text(
                    text = "\u2716",
                    color = TextGray,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onQueryChange("") }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun VoiceSearchHint() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "\uD83C\uDFA4",
            fontSize = 12.sp
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Say \"OK Google, search...\" on your watch",
            color = TextDim,
            style = MaterialTheme.typography.caption1,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
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
                .background(CardBGLight)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenAlpha20),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\uD83C\uDFB5",
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.body1,
                    color = TextWhite
                )
                Text(
                    text = "${result.channelName} \u2022 ${result.formatDuration()}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextGray,
                    style = MaterialTheme.typography.caption2
                )
            }

            // Play arrow
            Text(
                text = "\u25B6",
                color = SpotifyGreen,
                fontSize = 14.sp
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

    // Background breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "playerBg")
    val bgProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgProgress"
    )

    val bgColor = lerp(PlayerBgStart, PlayerBgEnd, bgProgress)

    // Album art ring rotation (only spins when playing)
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 8000 else 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    // Focus for rotary scroll
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = { TimeText(timeTextStyle = TimeTextDefaults.timeTextStyle(color = TextDim)) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            // Decorative background particles/glow
            BackgroundParticles(isPlaying = isPlaying)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
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
                Spacer(Modifier.height(8.dp))

                // Now Playing header
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.caption1,
                    color = TextDim,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // Album art visualization
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val glowWidth = 3.dp.toPx()
                        drawCircle(
                            color = SpotifyGreen.copy(alpha = if (isPlaying) 0.3f else 0.1f),
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
                                        SpotifyGreen,
                                        GreenLight,
                                        GreenDark,
                                        SpotifyGreen,
                                        SpotifyGreen
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Music note icon drawn with Canvas
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
                        color = TextWhite,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = track.channelName,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.body2
                    )
                } ?: run {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Choose a song",
                        color = TextDim,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body1
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Buffering indicator
                AnimatedVisibility(visible = isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                }

                // Progress arc
                if (duration > 0) {
                    ProgressArc(progress = progress, isPlaying = isPlaying)

                    Spacer(Modifier.height(2.dp))

                    // Time labels
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatMs(currentPosition),
                            color = TextGray,
                            style = MaterialTheme.typography.caption2
                        )
                        Text(
                            text = formatMs(duration),
                            color = TextGray,
                            style = MaterialTheme.typography.caption2
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
                        style = MaterialTheme.typography.caption1,
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
                        // Previous track
                        SmallActionButton(
                            label = "\u23EE",
                            onClick = { viewModel.skipToPrevious() },
                            modifier = Modifier.weight(1f)
                        )

                        // Save / Remove
                        ActionButton(
                            label = if (isTrackSaved) "\u2665" else "\u2661",
                            isActive = isTrackSaved,
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

                        // Next track
                        SmallActionButton(
                            label = "\u23ED",
                            onClick = { viewModel.skipToNext() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Back button
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack,
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text("Back")
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        LaunchedEffect(Unit) {
            delay(200)
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun BackgroundParticles(isPlaying: Boolean) {
    if (!isPlaying) return

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val particleAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particleAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension * 0.4f

        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60).toDouble())
            val x = centerX + (radius * cos(angle)).toFloat()
            val y = centerY + (radius * sin(angle)).toFloat()

            drawCircle(
                color = SpotifyGreen.copy(alpha = particleAlpha * 0.5f),
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
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

            // Track arc (background)
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
            val glowColor = if (isPlaying) SpotifyGreen else GreenDark
            drawArc(
                color = glowColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )

            // Glow effect at the progress head
            if (progress > 0.01f && progress < 0.99f) {
                val headAngle = -90f + 360f * progress
                val headRad = Math.toRadians(headAngle.toDouble())
                val headRadius = arcSize / 2
                val cx = size.width / 2 + headRadius * cos(headRad).toFloat()
                val cy = size.height / 2 + headRadius * sin(headRad).toFloat()

                drawCircle(
                    color = GreenGlow.copy(alpha = 0.6f),
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
        colors = ButtonDefaults.primaryButtonColors(
            backgroundColor = SpotifyGreen
        )
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "playPauseAnim"
        ) { playing ->
            Text(
                text = if (playing) {
                    if (isBuffering) "\u23F3" else "\u23F8"
                } else "\u25B6",
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                color = TextWhite
            )
        }
    }
}

@Composable
private fun SmallActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.secondaryButtonColors(
            backgroundColor = WhiteOverlay10
        )
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = TextWhite
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) GreenAlpha20 else WhiteOverlay10
    val textColor = if (isActive) SpotifyGreen else TextGray

    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.secondaryButtonColors(
            backgroundColor = bgColor
        )
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = textColor
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = { TimeText(timeTextStyle = TimeTextDefaults.timeTextStyle(color = TextGray)) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.title2,
                    textAlign = TextAlign.Center,
                    color = TextWhite
                )
            }

            // Subtitle
            item {
                Text(
                    text = if (savedTracks.size == 1) "1 song saved"
                    else "${savedTracks.size} songs saved",
                    color = TextDim,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption1
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
                        Text(
                            text = "\uD83C\uDFB5",
                            fontSize = 28.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No saved songs yet",
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.body2
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Save songs from the player",
                            color = TextDim,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.caption1
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

            // Back button
            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack,
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text("Back")
                }
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
                .background(CardBGLight)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track number placeholder
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenAlpha10),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u266B",
                    color = SpotifyGreen,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.body1,
                    color = TextWhite
                )
                Text(
                    text = track.channelName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextGray,
                    style = MaterialTheme.typography.caption2
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
                Text(
                    text = "\u2716",
                    color = TextDim,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  SHARED DRAWING UTILITIES
// ═══════════════════════════════════════════════

private fun DrawScope.drawRotatingRings(rotation: Float, isPlaying: Boolean) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxR = size.minDimension / 2 - 2.dp.toPx()

    // Outer decorative ring
    val ringAlpha = if (isPlaying) 0.25f else 0.08f
    drawCircle(
        color = SpotifyGreen.copy(alpha = ringAlpha),
        radius = maxR,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Rotating segments
    val segments = 3
    val segmentLength = 60f
    for (i in 0 until segments) {
        val startAngle = rotation + (i * 360f / segments)
        drawArc(
            color = GreenLight.copy(alpha = ringAlpha * 2),
            startAngle = startAngle,
            sweepAngle = segmentLength,
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

    // Draw a simple music note: two vertical lines with filled circles on top
    // Left note
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

    // Oval/ellipse for left note head
    drawOval(
        color = Color.White,
        topLeft = Offset(leftX - noteSize * 0.15f, bottomY - noteSize * 0.1f),
        size = androidx.compose.ui.geometry.Size(noteSize * 0.25f, noteSize * 0.15f)
    )

    // Right note (slightly higher)
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

    // Connecting bar
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
