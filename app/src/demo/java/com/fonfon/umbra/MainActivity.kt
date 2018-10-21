package com.fonfon.umbra

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import kotlinx.android.synthetic.demo.activity_main.*


class MainActivity : LocationActivity() {

    lateinit var presenter: PresenterMain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        content = content_main
        presenter = PresenterMain(this)

        presenter.died = {
            val backgroundColorAnimator = ObjectAnimator.ofObject(
                content_main, "backgroundColor",
                ArgbEvaluator(), Color.BLACK, Color.RED
            )
            backgroundColorAnimator.duration = 100
            backgroundColorAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    val anim = ObjectAnimator.ofObject(
                        content_main, "backgroundColor",
                        ArgbEvaluator(), Color.RED, Color.BLACK
                    )
                    anim.duration = 1000
                    anim.start()
                    appName.animate().alpha(1f).setDuration(1000).setListener(null).start()
                    button_start.animate().alpha(1f).setDuration(1000).setListener(null).start()
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }

            })
            backgroundColorAnimator.start()
        }

        presenter.win = {

            val backgroundColorAnimator = ObjectAnimator.ofObject(
                content_main, "backgroundColor",
                ArgbEvaluator(), Color.BLACK, Color.GREEN
            )
            backgroundColorAnimator.duration = 100
            backgroundColorAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    val anim = ObjectAnimator.ofObject(
                        content_main, "backgroundColor",
                        ArgbEvaluator(), Color.WHITE, Color.GREEN
                    )
                    anim.duration = 1000
                    anim.start()
                    appName.animate().alpha(1f).setDuration(1000).setListener(null).start()
                    button_start.animate().alpha(1f).setDuration(1000).setListener(null).start()
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }

            })
            backgroundColorAnimator.start()
        }

        appName.typeface = Typeface.createFromAsset(assets, "fonts/font.ttf")

        button_start.setOnClickListener {
            appName.animate().alpha(0f).setDuration(500).start()
            button_start.animate().alpha(0f).setDuration(500).setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    presenter.gameStart()
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {

                }

            }).start()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }
}
