package thuannv.videoplayer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.ui.PlayerView

class VideoActivity : AppCompatActivity() {

    lateinit var playerHolder: PlayerHolder

    val state = PlayerState(0, 0, true, MediaSourceType.PLAY_LIST)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        val playerView = findViewById<PlayerView>(R.id.player_view)
        playerHolder = PlayerHolder(this, playerView, state)
    }

    override fun onStart() {
        super.onStart()
        playerHolder.start()
    }

    override fun onStop() {
        playerHolder.stop()
        super.onStop()
    }

    override fun onDestroy() {
        playerHolder.release()
        super.onDestroy()
    }
}
