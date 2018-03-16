package thuannv.videoplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * @author thuannv
 * @since 15/03/2018
 */

enum class MediaSourceType {
    LOCAL_AUDIO,
    LOCAL_VIDEO,
    HTTP_AUDIO,
    HTTP_VIDEO,
    PLAY_LIST;
}

val mediaMap = mapOf<MediaSourceType, Uri>(
        MediaSourceType.LOCAL_AUDIO to Uri.parse("asset:///audio/demo.mp3"),
        MediaSourceType.LOCAL_VIDEO to Uri.parse("asset:///video/big_buck_bunny.mp4"),
        MediaSourceType.HTTP_AUDIO to Uri.parse("http://www.kozco.com/tech/piano2-CoolEdit.mp3"),
        MediaSourceType.HTTP_VIDEO to Uri.parse("http://clips.vorwaerts-gmbh.de/VfE_html5.mp4")
)

data class PlayerState(var window: Int = 0,
                       var position: Long = 0,
                       var whenReady: Boolean = true,
                       var sourceType: MediaSourceType = MediaSourceType.LOCAL_VIDEO)


class PlayerHolder(val context: Context,
                   val playerView: PlayerView,
                   val playerState: PlayerState) : AnkoLogger {
    val player: ExoPlayer

    init {
        player = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
                .also {
                    playerView.player = it // bind to the view
                    info { "SimpleExoPlayer is created." }
                }
    }


    fun buildMediaSource(sourceType: MediaSourceType): MediaSource {
        when (sourceType) {
            MediaSourceType.PLAY_LIST -> {
                val playlist = DynamicConcatenatingMediaSource()
                playlist.addMediaSource(createExtractorMediaSource(MediaSourceType.LOCAL_AUDIO))
                playlist.addMediaSource(createExtractorMediaSource(MediaSourceType.LOCAL_VIDEO))
                playlist.addMediaSource(createExtractorMediaSource(MediaSourceType.HTTP_AUDIO))
                playlist.addMediaSource(createExtractorMediaSource(MediaSourceType.HTTP_VIDEO))
                return playlist
            }
            else -> {
                return createExtractorMediaSource(sourceType)
            }
        }
    }

    private fun createExtractorMediaSource(sourceType: MediaSourceType): MediaSource {
        val defaultDataSourceFactory = DefaultDataSourceFactory(context, "exoplayer");
        return ExtractorMediaSource.Factory(defaultDataSourceFactory).createMediaSource(mediaMap.get(sourceType))
    }

    fun start() {
        // Load media
        player.prepare(buildMediaSource(playerState.sourceType))

        with(playerState) {
            // Starts playback when media has buffered enough
            player.playWhenReady = true
            // Seeks to previous position after onStart()/onStop()
            player.seekTo(window, position)
        }

        info { "SimpleExoPlayer is started." }
    }

    fun stop() {
        with(player) {
            // Saves state
            with(playerState) {
                position = currentPosition
                window = currentWindowIndex
                whenReady = playWhenReady
            }

            // Stops the player
            stop()
        }

        info { "SimpleExoPlayer is stopped." }
    }

    fun release() {
        player.release()
        info { "SimpleExoPlayer is released." }
    }
}