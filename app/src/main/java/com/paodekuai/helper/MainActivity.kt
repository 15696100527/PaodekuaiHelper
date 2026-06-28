// 跑的快 辅助工具 - 主界面
package com.paodekuai.helper

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_REQ = 1001
    private val STORAGE_PERMISSION_REQ = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tv_status)
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnStop = findViewById<Button>(R.id.btn_stop)
        val btnTest = findViewById<Button>(R.id.btn_test)

        btnStart.setOnClickListener {
            if (checkAllPermissions()) {
                startFloatingService()
            } else {
                requestAllPermissions()
            }
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, FloatingWindowService::class.java))
            Toast.makeText(this, "已停止辅助服务", Toast.LENGTH_SHORT).show()
            updateStatus(tvStatus)
        }

        btnTest.setOnClickListener {
            testGameEngine()
        }

        updateStatus(tvStatus)
    }

    private fun checkAllPermissions(): Boolean {
        val hasOverlay = Settings.canDrawOverlays(this)
        val hasStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        return hasOverlay && hasStorage
    }

    private fun requestAllPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请允许悬浮窗权限", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), STORAGE_PERMISSION_REQ)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQ)
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        startForegroundService(intent)
        Toast.makeText(this, "辅助服务已启动，返回游戏即可", Toast.LENGTH_LONG).show()
        updateStatus(findViewById(R.id.tv_status))
    }

    private fun updateStatus(tv: TextView) {
        val overlay = if (Settings.canDrawOverlays(this)) "✅" else "❌"
        tv.text = "悬浮窗权限: $overlay\n服务状态: 点击启动"
        tv.textSize = 14f
    }

    private fun testGameEngine() {
        // GameEngine 是 object 单例，直接调用
        val hand = listOf(
            Card("spade", 3), Card("spade", 4), Card("spade", 5), Card("spade", 6), Card("spade", 7),
            Card("spade", 8), Card("spade", 9), Card("spade", 10), Card("spade", 11), Card("spade", 12)
        )
        // 直接调用 object 的方法，不用实例化
        val result = GameEngine.analyze(hand, emptyList())
        Toast.makeText(this, result, Toast.LENGTH_LONG).show()
    }
}
