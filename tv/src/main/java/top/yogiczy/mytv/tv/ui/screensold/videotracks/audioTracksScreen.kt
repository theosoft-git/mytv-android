package top.yogiczy.mytv.tv.ui.screensold.videotracks

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import top.yogiczy.mytv.tv.ui.material.Drawer
import top.yogiczy.mytv.tv.ui.material.DrawerPosition
import top.yogiczy.mytv.tv.ui.screensold.components.rememberScreenAutoCloseState
import top.yogiczy.mytv.tv.ui.screensold.videoplayer.player.VideoPlayer
import top.yogiczy.mytv.tv.ui.screensold.videotracks.components.VideoTrackItemList
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme
import top.yogiczy.mytv.tv.ui.tooling.PreviewWithLayoutGrids
import top.yogiczy.mytv.tv.ui.utils.backHandler

@Composable
fun VideoTracksScreen(
    modifier: Modifier = Modifier,
    trackListProvider: () -> List<VideoPlayer.Metadata.Video> = { emptyList() },
    currentTrackProvider: () -> VideoPlayer.Metadata.Video? = { VideoPlayer.Metadata.Video() },
    onTrackChanged: (VideoPlayer.Metadata.Video) -> Unit = {},
    onClose: () -> Unit = {},
) {
    val screenAutoCloseState = rememberScreenAutoCloseState(onTimeout = onClose)

    Drawer(
        modifier = modifier.backHandler { onClose() },
        onDismissRequest = onClose,
        position = DrawerPosition.End,
        header = { Text("视轨") },
    ) {
        VideoTrackItemList(
            modifier = Modifier.width(268.dp),
            trackListProvider = trackListProvider,
            currentTrackProvider = currentTrackProvider,
            onSelected = onTrackChanged,
            onUserAction = { screenAutoCloseState.active() },
        )
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun VideoTracksScreenPreview() {
    MyTvTheme {
        PreviewWithLayoutGrids {
            VideoTracksScreen()
        }
    }
}