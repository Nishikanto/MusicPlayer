package me.bogerchan.niervisualizer.renderer.columnar

import android.graphics.*
import android.util.Log
import me.bogerchan.niervisualizer.renderer.IRenderer

/**
 * Created by BogerChan on 2017/11/26.
 */
class ColumnarType1Renderer : IRenderer {

    private var currentStepPosition: Int = 0
    private val mPaint: Paint
    private val mLastDrawArea = Rect()
    private lateinit var mRenderColumns: Array<RectF>
    // per column' width equals to twice of gap
    private val mGapRatio = 0.7F
    private val mRadius = 10F
    private var mHalfHeight = 0F

    constructor(paint: Paint) {
        mPaint = paint
    }

    constructor() {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.color = Color.BLUE
    }

    override fun onStart(captureSize: Int) {
        mRenderColumns = Array(Math.min(80, captureSize)) { RectF(0F, -5F, 0F, 5F) }
        mLastDrawArea.set(0, 0, 0, 0)
    }

    override fun onStop() {

    }

    override fun getInputDataType() = IRenderer.DataType.WAVE

    override fun calculate(drawArea: Rect, data: ByteArray) {
        if (drawArea != mLastDrawArea) {
            calculateRenderData(drawArea)
            mLastDrawArea.set(drawArea)
        }
        updateWave(data)
    }

    private fun transformWaveValue(value: Byte, rectF: RectF) {
        rectF.bottom = ((value.toInt() and 0xFF).toFloat() - 128F) / 128F * mHalfHeight
        rectF.bottom = if (rectF.bottom == 0F) 5F else rectF.bottom
        rectF.top = - rectF.bottom
    }

    private fun updateWave(data: ByteArray) {
        if (mRenderColumns.size >= data.size) {
            data.forEachIndexed { index, byte ->
                transformWaveValue(byte, mRenderColumns[index])
            }
        } else {
            val step = data.size / mRenderColumns.size
            mRenderColumns.forEachIndexed { index, rectF ->
                transformWaveValue(data[index * step], rectF)
            }
        }
    }

    private fun calculateRenderData(drawArea: Rect) {
        mHalfHeight = drawArea.height() / 1F
        val perGap = drawArea.width().toFloat() / (mRenderColumns.size * (mGapRatio + 1) + 1)
        mRenderColumns.forEachIndexed { index, rect ->
            rect.left = ((index + 1) * (1 + mGapRatio) - mGapRatio) * perGap
            rect.right = rect.left + mGapRatio * perGap
        }
    }

    override fun render(canvas: Canvas) {
        canvas.save()
        canvas.translate(mLastDrawArea.left.toFloat(), (mLastDrawArea.top + mLastDrawArea.bottom) / 2F)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.BLUE

        var i = 0
        mRenderColumns.forEach {
            if(i<currentStepPosition){
                canvas.drawRoundRect(it, mRadius, mRadius, paint)
            }else
                canvas.drawRoundRect(it, mRadius, mRadius, mPaint)
            i++
        }
        canvas.restore()
    }

    fun setAudioProgress(currentPosition: Int, inputHigh: Int) {
        val step = inputHigh/mRenderColumns.size
        currentStepPosition = currentPosition/step + 1
    }
}