// Application 类
package com.paodekuai.helper

import android.app.Application

class PaoDeKuaiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化工作（后续可加 OCR 模型加载等）
    }
}
