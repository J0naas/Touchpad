package com.example.touchpad

import android.view.MotionEvent

class Recorder {
    var active = false
    private val script: List<String> get() = _script
    private val _script = mutableListOf<String>()

    fun add(event:Int, x:Int, y:Int):Boolean{
        if (!active){
            return false
        }

        val coordinates = "$x, $y"
        _script.add(coordinates)
        when(event){
            MotionEvent.ACTION_DOWN -> {
                _script.add("down")
            }
            MotionEvent.ACTION_UP -> {
                _script.add("up")
            }
        }
        return true
    }

    fun getAsScriptString():String{
        return script.joinToString("; ")
    }

    fun reset(){
        active = false
        _script.clear()
    }
}