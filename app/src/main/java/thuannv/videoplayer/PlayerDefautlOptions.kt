package thuannv.videoplayer

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.support.v4.media.AudioAttributesCompat
import android.support.v4.media.MediaDescriptionCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import thuannv.videoplayer.AudioFocusWraper

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


        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val audioAttributes = AudioAttributesCompat.Builder()
                .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                .build()



//        player = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
//                .also {
//                    playerView.player = it // bind to the view
//                    info { "SimpleExoPlayer is created." }
//                }

        player = AudioFocusWraper(
                audioAttributes,
                audioManager,
                ExoPlayerFactory.newSimpleInstance(
                        context, DefaultTrackSelector())
                        .apply {
                            playerView.player = this
                        })

        info { "SimpleExoPlayer is created." }
    }



    private fun buildMediaSource(sourceType: MediaSourceType): MediaSource {
//        when (sourceType) {
//            MediaSourceType.PLAY_LIST -> {
//                val playlist = DynamicConcatenatingMediaSource()
//                playlist.addMediaSource(createExtractorMediaSource(MediaSourceType.LOCAL_AUDIO))
//                playlist.addMediaSource(createExtractorMediaSource(MediaSourceType.LOCAL_VIDEO))
//                playlist.addMediaSource(createExtractorMediaSource(MediaSourceType.HTTP_AUDIO))
//                playlist.addMediaSource(createExtractorMediaSource(MediaSourceType.HTTP_VIDEO))
//                return playlist
//            }
//            else -> {
//                return createExtractorMediaSource(sourceType)
//            }
//        }


        return when (sourceType) {
            MediaSourceType.PLAY_LIST -> buildMediaSourceForMediaSesssionDemo(context)
            else -> createExtractorMediaSource(sourceType)
        }
    }

    private fun createExtractorMediaSource(sourceType: MediaSourceType): MediaSource {
        val defaultDataSourceFactory = DefaultDataSourceFactory(context, "PlayerApp");
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

object MediaCatalog {

    val list = mutableListOf<MediaDescriptionCompat>()

    init {
        list.add(
                with(MediaDescriptionCompat.Builder()) {
                    setDescription("MP3 loaded over Assets")
                    setMediaId("1")
                    setMediaUri(Uri.parse("asset:///audio/demo.mp3"))
                    setTitle("Yêu là cùng nhìn về 1 hướng")
                    setSubtitle("MP3 music")
                    build()
                })

        list.add(
                with(MediaDescriptionCompat.Builder()) {
                    setDescription("Video loaded from Assets")
                    setMediaId("1")
                    setMediaUri(Uri.parse("asset:///video/big_buck_bunny.mp4"))
                    setTitle("Big Buck Bunny")
                    setSubtitle("MP4 Cartoon")
                    build()
                })

        list.add(
                with(MediaDescriptionCompat.Builder()) {
                    setDescription("MP3 loaded over HTTP")
                    setMediaId("1")
                    setMediaUri(Uri.parse("http://www.kozco.com/tech/piano2-CoolEdit.mp3"))
                    setTitle("Piano")
                    setSubtitle("MP3 music")
                    build()
                })

        list.add(
                with(MediaDescriptionCompat.Builder()) {
                    setDescription("MP3 loaded over HTTP")
                    setMediaId("1")
                    setMediaUri(Uri.parse("http://clips.vorwaerts-gmbh.de/VfE_html5.mp4"))
                    setTitle("Big Buck Bunny")
                    setSubtitle("MP4 Cartoon")
                    build()
                })

    }
}

fun createExtractorMediaSource(context: Context, uri: Uri): MediaSource {
    return ExtractorMediaSource.Factory(DefaultDataSourceFactory(context, "PlayerApp")).createMediaSource(uri)
}

fun buildMediaSourceForMediaSesssionDemo(context: Context): MediaSource {
    val mediaSources = mutableListOf<MediaSource>()
    MediaCatalog.list.forEach {
        mediaSources.add(createExtractorMediaSource(context, it.mediaUri!!))
    }
    return ConcatenatingMediaSource(*mediaSources.toTypedArray())
}

