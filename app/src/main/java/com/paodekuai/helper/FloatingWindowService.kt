// 悬浮窗服务 - 在游戏界面上显示辅助信息
package com.paodekuai.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat

class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var tvResult: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnClose: ImageButton

    companion object {
        const val CHANNEL_ID = "paodekuai_helper_channel"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_NAME = "跑的快辅助服务"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        createFloatingWindow()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
            channel.description = "跑的快辅助悬浮窗服务"
            channel.setShowBadge(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("跑的快辅助")
            .setContentText("辅助服务运行中，返回游戏查看悬浮窗")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.END
        params.x = 20
        params.y = 100

        windowManager.addView(floatingView, params)
        initFloatingView()
    }

    private fun initFloatingView() {
        tvResult = floatingView.findViewById(R.id.tv_result)
        btnRefresh = floatingView.findViewById(R.id.btn_refresh)
        btnClose = floatingView.findViewById(R.id.btn_close)
        val btnMinimize = floatingView.findViewById<Button>(R.id.btn_minimize)

        btnClose.setOnClickListener { stopSelf() }

        btnRefresh.setOnClickListener { analyzeCurrentScreen() }

        btnMinimize.setOnClickListener {
            val cardBody = floatingView.findViewById<CardView>(R.id.card_body)
            if (cardBody.visibility == View.VISIBLE) {
                cardBody.visibility = View.GONE
                btnMinimize.text = "展开"
            } else {
                cardBody.visibility = View.VISIBLE
                btnMinimize.text = "收起"
            }
        }

        val dragArea = floatingView.findViewById<LinearLayout>(R.id.layout_drag)
        dragArea.setOnTouchListener(DragTouchListener())

        tvResult.text = "跑的快辅助已启动\n\n点击「分析」开始\n或手动输入手牌测试"
    }

    private fun analyzeCurrentScreen() {
        tvResult.text = "正在分析...\n（当前为测试版，请手动输入手牌）"
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed({
            val testHand = listOf(
                Card("spade", 3), Card("spade", 4), Card("spade", 5),
                Card("heart", 6), Card("club", 7), Card("diamond", 8),
                Card("spade", 9), Card("heart", 10), Card("club", 11),
                Card("diamond", 12)
            )
            val result = GameEngine.analyze(testHand, emptyList())
            tvResult.text = result
        }, 500)
    }

    inner class DragTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    return true
                }
            }
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized && floatingView.isAttachedToWindow) {
            windowManager.removeView(floatingView)
        }
    }
}
