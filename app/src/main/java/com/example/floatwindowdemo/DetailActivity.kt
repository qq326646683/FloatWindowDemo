package com.example.floatwindowdemo

import android.content.Intent
import android.icu.lang.UCharacter
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val linearLayout = LinearLayout(this).apply {
            orientation = VERTICAL
        }

        linearLayout.addView(TextView(this).apply {
            text = "我是详情页"
        })

        linearLayout.addView(Button(this).apply {
            text = "跳转FLoatWindow页"
            setOnClickListener {
                startActivity(Intent(this@DetailActivity, FloatWindowActivity::class.java))
            }
        })


        setContentView(linearLayout)
    }
}