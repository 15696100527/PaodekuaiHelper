// 悬浮窗服务 - 在游戏界面上显示辅助信息
package com.paodekuai.helper

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView

class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var tvResult: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnClose: ImageButton

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createFloatingWindow()
    }

    private fun createFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 加载悬浮窗布局
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null)

        // 设置 LayoutParams
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

        // 初始位置：右上角
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

        // 关闭按钮
        btnClose.setOnClickListener {
            stopSelf()
        }

        // 刷新按钮：触发分析
        btnRefresh.setOnClickListener {
            analyzeCurrentScreen()
        }

        // 最小化按钮
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

        // 拖动功能
        val dragArea = floatingView.findViewById<LinearLayout>(R.id.layout_drag)
        dragArea.setOnTouchListener(DragTouchListener())

        // 初始提示
        tvResult.text = "👋 跑的快辅助已启动\n\n点击「分析」开始\n或手动输入手牌测试"
    }

    private fun analyzeCurrentScreen() {
        tvResult.text = "🔍 正在分析...\n（当前为测试版，请手动输入手牌）"
        handler.postDelayed({
            // 测试：模拟分析
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
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        isRunning = false
    }
}
