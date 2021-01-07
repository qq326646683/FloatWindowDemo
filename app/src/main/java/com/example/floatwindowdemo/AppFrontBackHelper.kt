package com.example.floatwindowdemo

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

class AppFrontBackHelper {
    private val mOnAppStatusListeners: MutableSet<OnAppStatusListener> = HashSet()


    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }


    private val activityLifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks {
            //打开的Activity数量统计
            private var activityStartCount = 0
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {
            }

            override fun onActivityStarted(activity: Activity) {
                activityStartCount++
                //数值从0变到1说明是从后台切到前台
                if (activityStartCount == 1) {
                    //从后台切到前台
                    for (listener in mOnAppStatusListeners) {
                        listener.onFront()
                    }

                }
            }

            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                activityStartCount--
                //数值从1到0说明是从前台切到后台
                if (activityStartCount == 0) {
                    //从前台切到后台
                    for (listener in mOnAppStatusListeners) {
                        listener.onBack()
                    }

                }
            }

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle
            ) {
            }

            override fun onActivityDestroyed(activity: Activity) {}
        }


    fun registerAppStatusListener(listener: OnAppStatusListener) {
        mOnAppStatusListeners.add(listener)

    }

    fun unregisterAppStatusListener(listener: OnAppStatusListener?) {
        mOnAppStatusListeners.remove(listener)

    }

    interface OnAppStatusListener {
        fun onFront()
        fun onBack()
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppFrontBackHelper() }
    }
}