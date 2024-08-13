package com.yes.flashcamera.presentation.ui

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.LinearSnapHelper


class ScrollableSeekBar(
    context: Context, attrs: AttributeSet?,
) : RecyclerView(context, attrs) {
    private var onItemScrolled: ((itme: Int) -> Unit)? = null

    init {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                updatePaddingRelative(start = width / 2, end = width / 2)
                clipToPadding = false
            }
        })
        addOnScrollListener(
            object : OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    for (i in 0..recyclerView.childCount) {
                        val view = recyclerView.layoutManager?.getChildAt(i)
                        view?.let {
                            if (
                                (view.x + view.width ) > width / 2
                               // && (view.x - (view.width / 2)) < width / 2
                                && view.x   <= width / 2
                                ) {
                                adapter?.let {
                                    for (i in 0..it.itemCount) {
                                        if (findViewHolderForAdapterPosition(i)?.itemView == view) {
                                            onItemScrolled?.let {
                                                it(i)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
        val linearSnapHelper=LinearSnapHelper()
        linearSnapHelper.attachToRecyclerView(this)

    }

    fun setOnItemScrolledListener(onItemScrolled: (itme: Int) -> Unit) {
        this.onItemScrolled = onItemScrolled
    }

}