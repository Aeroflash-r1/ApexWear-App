package com.echostream.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import com.echostream.data.db.TrackEntity
import com.echostream.data.model.SearchResult
import com.echostream.ui.theme.SpotifyGreen
import com.echostream.ui.theme.TextGray
import com.echostream.viewmodel.MusicViewModel

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

    val listState = androidx.wear.compose.foundation.lazy.rememberScalingLazyListState()

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
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "EchoStream",
                    style = MaterialTheme.typography.title2,
                    textAlign = TextAlign.Center,
                    color = SpotifyGreen
                )
            }
            item {
                Spacer(Modifier.height(4.dp))
                SearchInput(
                    query = query,
                    onQueryChange = { query = it }
                )
            }
            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val trimmedQuery = query.trim()
                        if (trimmedQuery.isNotEmpty()) {
                            viewModel.performSearch(trimmedQuery)
                        }
                    },
                    enabled = query.isNotBlank()
                ) {
                    Text("Search")
                }
            }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenLibrary,
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text("Library")
                }
            }
            if (isSearchLoading) {
                item {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
            errorMessage?.let { message ->
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colors.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.caption1,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!isSearchLoading && searchResults.isEmpty() && errorMessage == null) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Search for music",
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.caption1
                    )
                }
            }
            items(searchResults) { result ->
                Spacer(Modifier.height(2.dp))
                SearchResultCard(
                    result = result,
                    onClick = {
                        viewModel.playTrack(result)
                        onTrackSelected()
                    }
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = { TimeText(timeTextStyle = TimeTextDefaults.timeTextStyle(color = TextGray)) }
    ) {
        val listState = androidx.wear.compose.foundation.lazy.rememberScalingLazyListState()
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.title3,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }

            currentTrack?.let { track ->
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = track.title,
                        color = MaterialTheme.colors.onBackground,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onBackground
                        )
                    )
                }
                item {
                    Text(
                        text = track.channelName,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.body2
                    )
                }
            } ?: run {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Choose a song",
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body1
                    )
                }
            }

            if (isBuffering) {
                item {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }

            if (duration > 0) {
                item {
                    Spacer(Modifier.height(8.dp))
                    ProgressArc(progress = progress)
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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
            }

            errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colors.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.caption1,
                        maxLines = 2
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    modifier = Modifier.size(72.dp),
                    onClick = { viewModel.togglePlayPause() },
                    colors = ButtonDefaults.primaryButtonColors(backgroundColor = SpotifyGreen)
                ) {
                    Text(
                        text = if (isPlaying) "||" else "\u25B6",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            currentTrack?.let { track ->
                item {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
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
                        colors = ButtonDefaults.secondaryButtonColors()
                    ) {
                        Text(if (isTrackSaved) "Remove from Library" else "Save to Library")
                    }
                }
            }

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
private fun ProgressArc(progress: Float) {
    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val arcSize = size.minDimension - strokeWidth
            drawArc(
                color = Color(0xFF333333),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )
            drawArc(
                color = SpotifyGreen,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )
        }
    }
}

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onTrackSelected: () -> Unit,
    onBack: () -> Unit
) {
    val savedTracks by viewModel.savedTracks.collectAsState()
    val listState = androidx.wear.compose.foundation.lazy.rememberScalingLazyListState()

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
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.title2,
                    textAlign = TextAlign.Center
                )
            }
            if (savedTracks.isEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No saved songs yet",
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            items(savedTracks) { track ->
                Spacer(Modifier.height(2.dp))
                SavedTrackCard(
                    track = track,
                    onClick = {
                        viewModel.playTrack(track.toSearchResult())
                        onTrackSelected()
                    },
                    onDelete = { viewModel.deleteTrack(track) }
                )
            }
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
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colors.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        singleLine = true,
        textStyle = TextStyle(
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                if (query.isBlank()) {
                    Text(
                        text = "Song or artist",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Text(
                text = result.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "${result.channelName} \u2022 ${result.formatDuration()}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextGray,
                style = MaterialTheme.typography.caption2
            )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = track.channelName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextGray,
                    style = MaterialTheme.typography.caption2
                )
            }
            Text(
                text = "\u2716",
                color = TextGray,
                modifier = Modifier.clickable(onClick = onDelete).padding(4.dp),
                fontSize = 12.sp
            )
        }
    }
}

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
