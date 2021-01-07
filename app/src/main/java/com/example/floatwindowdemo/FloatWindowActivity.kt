package com.example.floatwindowdemo

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FloatWindowActivity : AppCompatActivity(), AppFrontBackHelper.OnAppStatusListener {
    var count: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_floatwindow)

        var countView = findViewById<TextView>(R.id.count)
        countView.text = count.toString()

        var addBtn = findViewById<Button>(R.id.add)
        addBtn.setOnClickListener {
            countView.text = (++count).toString()
        }

        var small = findViewById<ImageView>(R.id.small)
        small.setOnClickListener {
            var noPermission = checkPermi()
            if (noPermission) return@setOnClickListener
            moveTaskToBack(true)
            FloatWindowHelper.instance.showFloatWindow(this)

        }
        EventBus.getDefault().register(this)
        AppFrontBackHelper.instance.registerAppStatusListener(this)

    }

    fun checkPermi(): Boolean {
        val noPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)

        if (noPermission) {
            Toast.makeText(this, "无权限，请授权", Toast.LENGTH_LONG).show()
            startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")), 1)
        }

        return noPermission
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFinishFloatWindowActEvent(event: FinishFloatWindowActEvent) {
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        FloatWindowHelper.instance.closeFloatWindow(this, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        AppFrontBackHelper.instance.unregisterAppStatusListener(this)
    }

    override fun onBack() {

    }

    override fun onFront() {
        Log.i("nell-floatAct", "onFront")

        // 如果不在小窗中，将singleinstance路由栈拉回前台
        if (FloatWindowHelper.instance.dragFloatWrapper == null) {
            Log.i("nell-floatAct", "如果不在小窗中")


            var intent = Intent(this, FloatWindowActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

    }


}

