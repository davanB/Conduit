package ca.uwaterloo.fydp.conduit.conduitview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.*
import ca.uwaterloo.fydp.conduit.ConduitMessage
import ca.uwaterloo.fydp.conduit.R
import android.R.attr.animation
import android.view.animation.AccelerateDecelerateInterpolator


class ConduitRecordView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyle, defStyleRes) {

    var recording = false
    val duration = 4000
//    var recordCallback : (()->Unit)? = null
    var recordStartCallback : (()->Unit)? = null
    var recordStopCallback : (()->Unit)? = null

    init{
        inflate(getContext(), R.layout.conduit_record_view, this)

        val imageView = findViewById<ImageView>(R.id.record_audio_imageview)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)


        fun startAnim() {

        }

        val progressAnimation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100) // see this max value coming back here, we animate towards that value
        progressAnimation.duration = duration.toLong() //in milliseconds
        progressAnimation.interpolator = LinearInterpolator()


        fun stopAnim() {
            progressBar.progress = 0
            progressAnimation.cancel()

            val scaleUpX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f)
            scaleUpX.interpolator = AccelerateDecelerateInterpolator()
            scaleUpX.duration = 250

            val scaleUpY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f)
            scaleUpY.interpolator = AccelerateDecelerateInterpolator()
            scaleUpY.duration = 250

            val animSetScaleUp = AnimatorSet()
            animSetScaleUp.playTogether(kotlin.collections.arrayListOf(scaleUpX, scaleUpY) as Collection<Animator>?)
            animSetScaleUp.start()

            animSetScaleUp.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {}
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationStart(p0: Animator?) {}

                override fun onAnimationEnd(p0: Animator?) {
                    if (recording) {
                        // interrupt
                        recording = false
                        recordStopCallback?.invoke()
                    }
                }
            })
        }


        progressAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}

            override fun onAnimationEnd(animation: Animator) {
                stopAnim()
            }
        })

        this.setOnClickListener {

            if (recording) {
                // interrupt
                recording = false
                recordStopCallback?.invoke()
                stopAnim()

            } else {

                progressBar.progress = 0

                val scaleDownX = ObjectAnimator.ofFloat(imageView, "scaleX", 0.5f)
                scaleDownX.interpolator = AccelerateDecelerateInterpolator()
                scaleDownX.duration = 750

                val scaleDownY = ObjectAnimator.ofFloat(imageView, "scaleY", 0.5f)
                scaleDownY.interpolator = AccelerateDecelerateInterpolator()
                scaleDownY.duration = 750

                val animSetScaleDown = AnimatorSet()
                animSetScaleDown.playTogether(kotlin.collections.arrayListOf(scaleDownX, scaleDownY) as Collection<Animator>?)
                animSetScaleDown.start()


                progressAnimation.start()

                recordStartCallback?.invoke()

                recording = true
            }

        }


    }
}