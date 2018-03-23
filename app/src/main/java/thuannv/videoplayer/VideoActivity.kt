package thuannv.videoplayer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerView

class VideoActivity : AppCompatActivity() {

    private val state = PlayerState(0, 0, true, MediaSourceType.PLAY_LIST)

    private lateinit var playerHolder: PlayerHolder

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var mediaSessionConnector: MediaSessionConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        val playerView = findViewById<PlayerView>(R.id.player_view)
        playerHolder = PlayerHolder(this, playerView, state)

        mediaSession = MediaSessionCompat(this, packageName)

        mediaSessionConnector = MediaSessionConnector(mediaSession)

        mediaSessionConnector.setQueueNavigator(object: TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(windowIndex: Int): MediaDescriptionCompat {
                return MediaCatalog.list.get(windowIndex)
            }
        })
    }

    override fun onStart() {
        super.onStart()

        playerHolder.start()

        mediaSessionConnector.setPlayer(playerHolder.player, null)

        mediaSession.isActive = true
    }

    override fun onStop() {
        playerHolder.stop()

        mediaSessionConnector.setPlayer(null, null)

        mediaSession.isActive = false

        super.onStop()
    }

    override fun onDestroy() {

        mediaSession.release()

        playerHolder.release()

        super.onDestroy()
    }
}
