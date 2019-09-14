package com.calyptus.musicplayer

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.calyptus.musicplayer.adapters.MusicListAdapter
import com.calyptus.musicplayer.data.Music
import com.calyptus.musicplayer.utilities.ResettableLazyManager
import com.calyptus.musicplayer.utilities.resettableLazy
import com.calyptus.musicplayer.viewmodels.MusicPlayerFragmentViewModel
import com.yarolegovich.discretescrollview.DSVOrientation
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.android.synthetic.main.fragment_music_player.*
import me.bogerchan.niervisualizer.NierVisualizerManager
import me.bogerchan.niervisualizer.renderer.IRenderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType1Renderer
import java.lang.Exception
import java.util.concurrent.TimeUnit

class MusicPlayerFragment : Fragment() , View.OnClickListener {

    private var startPlay: Boolean = false
    private var listOfMusic: List<Music>? = null
    private var nowPlayingNo = 0
    private val lazyManager = ResettableLazyManager()

    private val viewModel: MusicPlayerFragmentViewModel by lazy {
        ViewModelProviders.of(this).get(MusicPlayerFragmentViewModel::class.java)
    }

    private val mediaPlayer by lazy {
        MediaPlayer().apply {

                setOnCompletionListener {
                    stopPlayback()
                    showVisualizerPreview()
                }
            }

    }

    private val mVisualizerManager by lazy {
        NierVisualizerManager().apply {
            init(mediaPlayer.audioSessionId)
        }
    }


    private val animator by resettableLazy(lazyManager) {
        ValueAnimator.ofFloat(svWave.left.toFloat(), svWave.right.toFloat() - timeText.width).apply {
            duration = mediaPlayer.duration.toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { timeText.x = it.animatedValue as Float }
        }
    }

    private var mRenderers = arrayOf<Array<IRenderer>>(
        arrayOf(ColumnarType1Renderer(Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF8A80")
        }))
    )


    private var mCurrentStyleIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_music_player, container, false)
        return rootView.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSurfaceView()
        initButtons()
        generalInitialization()
        showVisualizerPreview()
        setMusicDataObservable()
    }

    private fun generalInitialization() {
        musicTitleText.isSelected = true
    }

    private fun setMusicDataObservable() {

        viewModel.apply {
            fetchAllMusic(context!!.contentResolver).observe(
                activity as MainActivity,
                androidx.lifecycle.Observer {
                    initMediaPlayerWithSong(it[nowPlayingNo].path)
                    setUpRecyclerView(it)
                    listOfMusic = it
                })
        }
    }

    private fun initMediaPlayerWithSong(path: Uri) {
        if(mediaPlayer.isPlaying){
            stopPlayback()
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        val file = context?.contentResolver?.openFileDescriptor(path, "r")?.fileDescriptor
        mediaPlayer.setDataSource(file)
        mediaPlayer.prepare()
        mediaPlayer.setOnPreparedListener {
            lazyManager.reset()
            resetRenderer()
            showVisualizerPreview()
            if(startPlay){
                startPlayback()
                startPlay = false
            }
        }
    }

    fun resetRenderer(){
        mRenderers = arrayOf<Array<IRenderer>>(
            arrayOf(ColumnarType1Renderer(Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FF8A80")
            }))
        )
    }

    private fun startAudioPlayingProgress() {
        Thread(Runnable {
            while (mediaPlayer.isPlaying) {
                (mRenderers[mCurrentStyleIndex][0] as ColumnarType1Renderer).setAudioProgress(
                    mediaPlayer.currentPosition,
                    mediaPlayer.duration
                )

                (context as Activity).runOnUiThread {
                    timeText.text = millisToMinute(mediaPlayer.currentPosition)
                }
            }
        }).start()
    }

    private fun setUpRecyclerView(musicList: List<Music>) {
                musicRecyclerView.apply {
                    setOrientation(DSVOrientation.HORIZONTAL)
                    adapter = MusicListAdapter(musicList)
                    setItemTransitionTimeMillis(150)
                    setItemTransformer(ScaleTransformer.Builder().setMinScale(0.8f).build())
                    addOnItemChangedListener {viewHolder , currentPosition ->
                        nowPlayingNo = currentPosition
                        musicTitleText.text = listOfMusic?.get(currentPosition)?.musicTitle
                        artistText.text = listOfMusic?.get(currentPosition)?.artist
                        initMediaPlayerWithSong(listOfMusic?.get(currentPosition)?.path!!)
                    }
                }
    }

    private fun showVisualizerPreview() {
        mVisualizerManager.start(svWave, mRenderers[mCurrentStyleIndex])
        Handler().postDelayed({
            if(!mediaPlayer.isPlaying){
                mVisualizerManager.pause()
            }
        }, 3000)
    }

    private fun initButtons() {
        playBtn.setOnClickListener(this)
        nextBtn.setOnClickListener(this)
        previousBtn.setOnClickListener(this)
    }

    private fun setUpSurfaceView() {
        svWave.setZOrderOnTop(true)
        svWave.holder.setFormat(PixelFormat.TRANSLUCENT)
    }



    override fun onClick(id: View?) {
        when (id) {
            playBtn -> {
                if (mediaPlayer.isPlaying) {
                    stopPlayback()
                } else startPlayback()
            }

            nextBtn -> {
                nowPlayingNo += 1
                if(nowPlayingNo>listOfMusic!!.size-1){
                    nowPlayingNo = 0
                }
                //initMediaPlayerWithSong(listOfMusic!![nowPlayingNo].path, true)
                //Log.d("simul", " music no - $nowPlayingNo")
                musicRecyclerView.smoothScrollToPosition(nowPlayingNo)
                startPlay = true
            }

            previousBtn -> {
                nowPlayingNo -= 1
                if(nowPlayingNo<0){
                    nowPlayingNo = listOfMusic!!.size - 1
                }
                //initMediaPlayerWithSong(listOfMusic!![nowPlayingNo].path, true)
                //Log.d("simul", " music no - $nowPlayingNo")*/
                musicRecyclerView.smoothScrollToPosition(nowPlayingNo)
                startPlay = true
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

    override fun onResume() {
        super.onResume()
    }

    companion object {
       fun getInstance() : Fragment {
           return MusicPlayerFragment()
       }
   }


}