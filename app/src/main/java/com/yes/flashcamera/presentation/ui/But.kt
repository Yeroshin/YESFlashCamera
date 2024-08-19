package com.yes.flashcamera.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.widget.RadioGroup
import androidx.appcompat.R


class But (
    context: Context,
    attrs: AttributeSet? = null,

) : androidx.appcompat.widget.AppCompatRadioButton(context, attrs) {

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_UP) {
            if (parent is RadioGroup) {
                if (isChecked){
                    (parent as RadioGroup).clearCheck()
                }else{
                    (parent as RadioGroup).check(id)
                }
            }
        }

        return true
    }
}