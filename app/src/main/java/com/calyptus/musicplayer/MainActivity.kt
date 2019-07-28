package com.calyptus.musicplayer

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.yarolegovich.discretescrollview.DSVOrientation
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.android.synthetic.main.activity_main.*
import me.bogerchan.niervisualizer.NierVisualizerManager
import me.bogerchan.niervisualizer.renderer.IRenderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType1Renderer
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {


    private val svWave by lazy { findViewById<SurfaceView>(R.id.svWave) }
    private val parentLayout by lazy { findViewById<ConstraintLayout>(R.id.parentLayout) }
    private val timeText by lazy { findViewById<TextView>(R.id.timeText) }

    private val musicRecyclerView by lazy {
        findViewById<DiscreteScrollView>(R.id.musicRecyclerView)
    }

    private val playBtn by lazy { findViewById<ImageButton>(R.id.playBtn) }

    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            resources.openRawResourceFd(R.raw.demo_audio).apply {
                setDataSource(fileDescriptor, startOffset, length)
                prepare()
                setOnCompletionListener {
                    stopPlayback()
                }
            }
        }
    }

    private val mVisualizerManager by lazy {
        NierVisualizerManager().apply {
            init(mediaPlayer.audioSessionId)
        }
    }


    private val animator by lazy {
        ValueAnimator.ofFloat(svWave.left.toFloat(), svWave.right.toFloat() - timeText.width).apply {
            duration = mediaPlayer.duration.toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { timeText.x = it.animatedValue as Float }
        }
    }

    private val mRenderers = arrayOf<Array<IRenderer>>(
        arrayOf(ColumnarType1Renderer(Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF8A80")
        }))
    )


    private var mCurrentStyleIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        changeNavigationBarColor()
        changeStatusBarColor()
        setContentView(R.layout.activity_main)

        setUpSurfaceView()
        initButtons()
        showVisualizerPreview()
        setUpRecyclerView()

    }

    private fun startAudioPlayingProgress() {
        Thread(Runnable {
            while (mediaPlayer.isPlaying) {
                (mRenderers[mCurrentStyleIndex][0] as ColumnarType1Renderer).setAudioProgress(
                    mediaPlayer.currentPosition,
                    mediaPlayer.duration
                )

                runOnUiThread {
                    timeText.text = millisToMinute(mediaPlayer.currentPosition)
                }
            }
        }).start()
    }

    private fun setUpRecyclerView() {
        musicRecyclerView.apply {
            setOrientation(DSVOrientation.HORIZONTAL)
            adapter = MusicListAdapter(getData())
            setItemTransitionTimeMillis(150)
            setItemTransformer(ScaleTransformer.Builder().setMinScale(0.8f).build())
        }
    }

    private fun showVisualizerPreview() {
        mVisualizerManager.start(svWave, mRenderers[mCurrentStyleIndex])
    }

    private fun initButtons() {
        playBtn.setOnClickListener(this)
    }

    private fun setUpSurfaceView() {
        svWave.setZOrderOnTop(true)
        svWave.holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.white, theme)
        }
    }

    private fun changeNavigationBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.white, theme)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }


    override fun onClick(id: View?) {
        when (id) {
            playBtn -> {
                if (mediaPlayer.isPlaying) {
                    stopPlayback()
                } else startPlayback()
            }
        }
    }

    private fun startPlayback() {
        mediaPlayer.start()
        mVisualizerManager.resume()
        playBtn.setImageResource(R.drawable.ic_pause)
        startAudioPlayingProgress()
        startTimeAnimation()
    }

    private fun startTimeAnimation() {
        if (animator.isStarted) {
            animator.resume()
        } else
            animator.start()
    }

    private fun stopPlayback() {
        mediaPlayer.pause()
        mVisualizerManager.pause()
        playBtn.setImageResource(R.drawable.ic_play)
        stopTimeAnimation()
    }

    private fun stopTimeAnimation() {
        if(animator.isRunning){
            animator.pause()
        }
    }

    private fun getData(): List<Item> {
        return Arrays.asList(
            Item(1, "Everyday Candle", "$12.00 USD", R.drawable.ic_launcher_background),
            Item(2, "Small Porcelain Bowl", "$50.00 USD", R.drawable.ic_launcher_background),
            Item(3, "Favourite Board", "$265.00 USD", R.drawable.ic_launcher_background),
            Item(4, "Earthenware Bowl", "$18.00 USD", R.drawable.ic_launcher_background),
            Item(5, "Porcelain Dessert Plate", "$36.00 USD", R.drawable.ic_launcher_background),
            Item(6, "Detailed Rolling Pin", "$145.00 USD", R.drawable.ic_launcher_background)
        )
    }

    private fun durationToX(eventX: Int): Float {
        val inputLow = 0f
        val inputHigh = (mediaPlayer.duration).toFloat()
        val outputHigh = (svWave.width).toFloat() - timeText.width
        val outputLow = 0f
        Log.d(
            "simul",
            "${(((eventX - inputLow) / (inputHigh - inputLow) * (outputHigh - outputLow) + outputLow))} width ${svWave.width}"
        )
        return (((eventX - inputLow) / (inputHigh - inputLow) * (outputHigh - outputLow) + outputLow))
    }

    private fun millisToMinute(currentPosition: Int): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(currentPosition.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(currentPosition.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    currentPosition.toLong()
                )
            )
        )
    }

}
