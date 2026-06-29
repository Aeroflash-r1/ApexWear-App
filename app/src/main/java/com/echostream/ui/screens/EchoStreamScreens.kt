package com.echostream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.echostream.data.db.TrackEntity
import com.echostream.data.model.SearchResult
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

    EchoStreamList {
        item { TimeText() }
        item { ScreenTitle("EchoStream") }
        item {
            SearchInput(
                query = query,
                onQueryChange = { query = it }
            )
        }
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val trimmedQuery = query.trim()
                    if (trimmedQuery.isNotEmpty()) {
                        viewModel.performSearch(trimmedQuery)
                    }
                },
                label = { Text("Search music") },
                secondaryLabel = { Text(if (query.isBlank()) "Type above, then tap" else query) }
            )
        }
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenLibrary,
                label = { Text("Library") },
                secondaryLabel = { Text("Saved songs") }
            )
        }
        errorMessage?.let { message ->
            item { StatusText(message, isError = true) }
        }
        if (isSearchLoading) {
            item { CircularProgressIndicator() }
        }
        if (!isSearchLoading && searchResults.isEmpty() && errorMessage == null) {
            item { StatusText("Search for music") }
        }
        items(searchResults) { result ->
            SearchResultChip(
                result = result,
                onClick = {
                    viewModel.playTrack(result)
                    onTrackSelected()
                }
            )
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

    EchoStreamList {
        item { TimeText() }
        item { ScreenTitle("Now Playing") }

        currentTrack?.let { track ->
            item {
                Text(
                    text = track.title,
                    color = MaterialTheme.colors.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.title3
                )
            }
            item {
                Text(
                    text = track.channelName,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.caption1
                )
            }
        } ?: run {
            item { StatusText("Choose a song from search") }
        }

        if (isBuffering) {
            item { CircularProgressIndicator() }
        }

        // Progress bar
        if (duration > 0) {
            item {
                Text(
                    text = "${formatMs(currentPosition)} / ${formatMs(duration)}",
                    color = MaterialTheme.colors.onBackground,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption2
                )
            }
        }

        errorMessage?.let { message ->
            item { StatusText(message, isError = true) }
        }

        // Play/Pause
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.togglePlayPause() },
                label = { Text(if (isPlaying) "Pause" else "Play") },
                secondaryLabel = if (isBuffering) { { Text("Loading...") } } else null
            )
        }

        // Save/Unsave track
        currentTrack?.let { track ->
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (isTrackSaved) {
                            // Find and delete the saved track
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
                    label = { Text(if (isTrackSaved) "Remove from Library" else "Save to Library") }
                )
            }
        }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBack,
                label = { Text("Back") }
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

    EchoStreamList {
        item { TimeText() }
        item { ScreenTitle("Library") }
        if (savedTracks.isEmpty()) {
            item { StatusText("No saved songs yet. Search and save songs to build your library.") }
        }
        items(savedTracks) { track ->
            SavedTrackChip(
                track = track,
                onClick = {
                    viewModel.playTrack(track.toSearchResult())
                    onTrackSelected()
                }
            )
        }
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBack,
                label = { Text("Back") }
            )
        }
    }
}

@Composable
private fun EchoStreamList(content: ScalingLazyListScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
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
            .background(MaterialTheme.colors.surface, MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        singleLine = true,
        textStyle = TextStyle(
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                if (query.isBlank()) {
                    Text(
                        text = "Song or artist",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun SearchResultChip(
    result: SearchResult,
    onClick: () -> Unit
) {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        label = {
            Text(
                text = result.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            Text(
                text = "${result.channelName} • ${result.formatDuration()}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun SavedTrackChip(
    track: TrackEntity,
    onClick: () -> Unit
) {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        label = {
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            Text(
                text = track.channelName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun ScreenTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.title3,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun StatusText(text: String, isError: Boolean = false) {
    Text(
        text = text,
        color = if (isError) MaterialTheme.colors.error else Color.White,
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.caption1
    )
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
