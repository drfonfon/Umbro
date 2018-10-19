package com.fonfon.umbra

import android.animation.Animator
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.demo.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var presenter: PresenterMain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = PresenterMain(this)

        appName.typeface = Typeface.createFromAsset(assets,"fonts/font.ttf")

        button_start.setOnClickListener {
            appName.animate().alpha(0f).setDuration(500).start()
            button_start.animate().alpha(0f).setDuration(500).setListener(object: Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {

                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                    presenter.gameStart()
                }

            }).start()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}
