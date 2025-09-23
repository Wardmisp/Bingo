import android.content.Context
import android.media.SoundPool

class SoundPlayer(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(1).build()
    private var soundId: Int = 0

    init {
        // Load the sound file into memory
        soundId = soundPool.load(context, com.example.myapplication.R.raw.new_number, 1)
    }

    fun playSuccessSound() {
        // Play the sound after it's loaded
        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun release() {
        // Release resources when done to prevent memory leaks
        soundPool.release()
    }
}