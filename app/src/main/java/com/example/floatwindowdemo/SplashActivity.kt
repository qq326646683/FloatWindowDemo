package com.example.floatwindowdemo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SplashActivity : AppCompatActivity() {

    val timer = Timer("schedule", true);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "splash页面"
        setContentView(textView)

        timer.schedule(object : TimerTask() {
            override fun run() {
                finish()

                if (FloatWindowHelper.instance.needJumpToMain) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }
            }

        }, 1000)
    }
}