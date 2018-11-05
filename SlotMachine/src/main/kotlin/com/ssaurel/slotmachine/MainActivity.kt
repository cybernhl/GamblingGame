package com.ssaurel.slotmachine

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.util.*

class MainActivity : AppCompatActivity() {
    private var msg: TextView? = null
    private var img1: ImageView? = null
    private var img2: ImageView? = null
    private var img3: ImageView? = null
    private var wheel1: Wheel? = null
    private var wheel2: Wheel? = null
    private var wheel3: Wheel? = null
    private var btn: Button? = null
    private var isStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        img1 = findViewById<View>(R.id.img1) as ImageView
        img2 = findViewById<View>(R.id.img2) as ImageView
        img3 = findViewById<View>(R.id.img3) as ImageView
        btn = findViewById<View>(R.id.btn) as Button
        msg = findViewById<View>(R.id.msg) as TextView

        btn!!.setOnClickListener {
            if (isStarted) {
                wheel1!!.stopWheel()
                wheel2!!.stopWheel()
                wheel3!!.stopWheel()

                if (wheel1!!.currentIndex == wheel2!!.currentIndex && wheel2!!.currentIndex == wheel3!!.currentIndex) {
                    msg!!.text = "You win the big prize"
                } else if (wheel1!!.currentIndex == wheel2!!.currentIndex || wheel2!!.currentIndex == wheel3!!.currentIndex
                    || wheel1!!.currentIndex == wheel3!!.currentIndex
                ) {
                    msg!!.text = "Little Prize"
                } else {
                    msg!!.text = "You lose"
                }

                btn!!.text = "Start"
                isStarted = false

            } else {

                wheel1 = Wheel(
                    Wheel.WheelListener { img -> runOnUiThread { img1!!.setImageResource(img) } },
                    200,
                    randomLong(0, 200)
                )

                wheel1!!.start()

                wheel2 = Wheel(
                    Wheel.WheelListener { img -> runOnUiThread { img2!!.setImageResource(img) } },
                    200,
                    randomLong(150, 400)
                )

                wheel2!!.start()

                wheel3 = Wheel(
                    Wheel.WheelListener { img -> runOnUiThread { img3!!.setImageResource(img) } },
                    200,
                    randomLong(150, 400)
                )

                wheel3!!.start()

                btn!!.text = "Stop"
                msg!!.text = ""
                isStarted = true
            }
        }
    }

    companion object {

        val RANDOM = Random()

        fun randomLong(lower: Long, upper: Long): Long {
            return lower + (RANDOM.nextDouble() * (upper - lower)).toLong()
        }
    }
}
