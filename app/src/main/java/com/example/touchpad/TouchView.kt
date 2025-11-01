package com.example.touchpad

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.touchpad.Hid.getHover
import com.example.touchpad.Hid.getTouchDown
import com.example.touchpad.Hid.getTouchUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@SuppressLint("ViewConstructor")
class TouchView(context: Context?, private val viewModel: ConnectionViewModel, private val recorder: Recorder) : View(context) {
    private val paint = Paint()
    private var animationX = 0f
    private var animationY = 0f
    private var waveRadius = 0f
    private var isAnimating = false
    private var alpha = 255

    private var state: Int = MotionEvent.ACTION_UP
    var hoverMode = true


    private fun convertCoordinates(x:Float, y:Float): Pair<Int, Int>{
        return Pair((Hid.LOGICAL_SCREEN_MAX  * x/ width).toInt(), (Hid.LOGICAL_SCREEN_MAX * y/ height).toInt())
    }



    init {
        paint.color = -0x77000001 // semi-transparent white
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.alpha = alpha
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SCREEN))
    }


    private fun click(event:MotionEvent) {
        Log.d("TouchView", "Click ${event.x} ${event.y}")
        state = event.actionMasked
        val (x,y) = convertCoordinates(event.x, event.y)



        val report = when{
            hoverMode -> getHover(x, y)
            event.actionMasked == MotionEvent.ACTION_DOWN -> getTouchDown(x, y)
            event.actionMasked == MotionEvent.ACTION_UP -> getTouchUp(x, y)
            event.actionMasked == MotionEvent.ACTION_MOVE-> getTouchDown(x, y)
            else -> {
                Log.d("TouchView", "Unknown event ${event.actionMasked}")
                return
            }
        }

        if (recorder.active)
            recorder.add(event.actionMasked, x, y)

        viewModel.sendReport(report)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setBackgroundResource(R.drawable.controller_background)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.actionMasked != MotionEvent.ACTION_MOVE && event.actionMasked == state){
            return true
        }

        try {
            CoroutineScope(Dispatchers.Main).launch {
                click(event)
            }
        }catch (e:InvalidViewStateException){
            Toast.makeText(context, "Action failed. Try again.", Toast.LENGTH_SHORT).show()
            return false
        }

        if(event.actionMasked == MotionEvent.ACTION_UP){
            isAnimating = true
            animationX = event.x
            animationY = event.y
            waveRadius = 0F
        }

        invalidate()

        return true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isAnimating) {
            paint.alpha = alpha
            canvas.save()
            canvas.clipRect(RectF(0f, 0f, width.toFloat(), height.toFloat()))
            canvas.drawCircle(animationX, animationY, waveRadius, paint)
            canvas.restore()

            if (alpha > 0) {
                waveRadius += 5f
                alpha -= 5
                invalidate()
            } else {
                isAnimating = false
                waveRadius = 0f
                alpha = 255
                invalidate()
            }
        }
    }
}