package com.example.floatwindowdemo

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ImageView
import org.greenrobot.eventbus.EventBus
import java.util.*

class FloatWindowHelper : AppFrontBackHelper.OnAppStatusListener{

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { FloatWindowHelper() }
    }

    var dragFloatWrapper: DragFloatWrapper<View>? = null

    var isAppInBg: Boolean = false
    private var lastWindowX = -1
    private var lastWindowY = -1
    private var fixScreenHeight = 0

    var needJumpToMain: Boolean = true
    val timer = Timer("schedule", true);



    fun showFloatWindow(context: Context) {
        DragFloatWrapper<View>(context).apply {
            customView = getContentView(context, this)
        }.let {
            AppFrontBackHelper.instance.registerAppStatusListener(this)
            it.moveZoneRect = Rect(
                dp2px(context, 4f),
                dp2px(context, 46f),
                0,
                dp2px(context,48f)
            )
            initDragFloatWindowParams(context, it)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(it.context)) {
                getWindowManager(context).addView(it, it.windowParams)
                dragFloatWrapper = it
            }
        }
    }

    fun closeFloatWindow(context: Context, exitFloatActivity: Boolean = true) {
        AppFrontBackHelper.instance.unregisterAppStatusListener(this)
        //关闭浮窗
        if (dragFloatWrapper != null) {
            dragFloatWrapper?.let {
                lastWindowX = it.windowParams.x
                lastWindowY = it.windowParams.y
                if (it.isAttachedToWindow) {
                    getWindowManager(context).removeView(it)
                }
                dragFloatWrapper = null
            }

        }
        if (exitFloatActivity) {
            //退出activity
            EventBus.getDefault().post(FinishFloatWindowActEvent())
        }
    }


    private fun getContentView(
        context: Context,
        root: ViewGroup
    ): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_float, root, false)
            .apply {
                findViewById<ImageView>(R.id.close).setOnClickListener {
                    closeFloatWindow(context)
                }

                val extraMargin = dp2px(context, 14f)
                val contentEdge = dp2px(context, 60f) + extraMargin
                layoutParams.width = contentEdge
                layoutParams.height = contentEdge
                findViewById<ImageView>(R.id.gameView).apply {
                    this.scaleType = ImageView.ScaleType.CENTER_CROP
                    setOnClickListener {
                        // 先启动app,默认standard路由栈
                        Log.i("nell-isAppInBg", isAppInBg.toString())
                        if (isAppInBg) {
                            Log.i("nell-FloatWindowHelper", "启动app")
                            context.let {
                                val intent = it.packageManager.getLaunchIntentForPackage(it.packageName)
                                it.startActivity(intent)
                                // 告诉splash不需要跳转到主页,然后再恢复需要跳转
                                needJumpToMain = false
                                timer.schedule(object : TimerTask() {
                                    override fun run() {
                                        needJumpToMain = true
                                    }

                                }, 1200)

                            }
                            isAppInBg = false
                        }
                        // 将singleinstance路由栈拉到前台
                        context?.let {
                            var intent = Intent(it, FloatWindowActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (isAppInBg) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            }
                            it.startActivity(intent)
                        }

                    }
                }
            }

    }


    fun dp2px(context: Context?, dipValue: Float): Int {
        if (context == null) {
            return dipValue.toInt()
        }
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun getWindowManager(context: Context): WindowManager {
        return context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun initDragFloatWindowParams(context: Context, window: DragFloatWrapper<View>) {
        val layoutParams = window.windowParams
        layoutParams.type = getFloatWindowType()
        // 显示图片格式
        layoutParams.format = PixelFormat.TRANSPARENT
        // 设置交互模式
        layoutParams.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        // 设置对齐方式为左上
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.width = window.windowWidth
        layoutParams.height = window.windowHeight

        if (lastWindowX >= 0) {
            layoutParams.x = lastWindowX
        } else {
            layoutParams.x = (DragFloatWrapper.getScreenWidthPixels(context)
                    - window.moveZoneRect.right
                    - window.windowWidth)
        }
        if (lastWindowY >= 0) {
            layoutParams.y = lastWindowY
        } else {
            layoutParams.y = (DragFloatWrapper.getScreenHeightPixels(context)
                    - dp2px(context, 10f)
                    - window.moveZoneRect.bottom
                    - window.windowHeight)
        }
    }

    /**
     * @return 悬浮窗窗口类型
     */
    private fun getFloatWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    override fun onBack() {
        Log.i("nell-floatHelper", "onBack")

        isAppInBg = true
    }

    override fun onFront() {
    }


}

class FinishFloatWindowActEvent {}
