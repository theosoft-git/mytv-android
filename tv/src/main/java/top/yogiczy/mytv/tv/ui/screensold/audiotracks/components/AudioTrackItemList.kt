package top.yogiczy.mytv.tv.ui.screensold.audiotracks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.player.VideoPlayer
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme
import kotlin.math.max

@Composable
fun AudioTrackItemList(
    modifier: Modifier = Modifier,
    trackListProvider: () -> List<VideoPlayer.Metadata.Audio> = { emptyList() },
    currentTrackProvider: () -> VideoPlayer.Metadata.Audio? = { VideoPlayer.Metadata.Audio() },
    onSelected: (VideoPlayer.Metadata.Audio) -> Unit = {},
    onUserAction: () -> Unit = {},
) {
    val trackList = trackListProvider()

    val listState =
        rememberLazyListState(max(0, trackList.indexOf(currentTrackProvider()) - 2))

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { _ -> onUserAction() }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(trackList) { track ->
            AudioTrackItem(
                trackProvider = { track },
                isSelectedProvider = { track == currentTrackProvider() },
                onSelected = { onSelected(track) },
            )
        }
    }
}

@Preview
@Composable
private fun AudioTrackItemListPreview() {
    MyTvTheme {
        AudioTrackItemList(
            trackListProvider = {
                listOf(
                    VideoPlayer.Metadata.Audio(
                        channels = 2,
                        bitrate = 128000,
                    ),
                    VideoPlayer.Metadata.Audio(
                        channels = 10,
                        bitrate = 567000,
                    )
                )
            },
            currentTrackProvider = {
                VideoPlayer.Metadata.Audio(
                    channels = 10,
                    bitrate = 567000,
                )
            },
        )
    }
}