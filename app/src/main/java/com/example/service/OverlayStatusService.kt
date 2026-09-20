package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class OverlayStatusService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    companion object {
        const val ACTION_START_OVERLAY = "com.example.service.ACTION_START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.example.service.ACTION_STOP_OVERLAY"
        const val EXTRA_GAME_NAME = "extra_game_name"
        const val NOTIFICATION_ID = 991
        const val CHANNEL_ID = "zero_shot_overlay_channel"

        var isOverlayRunning: Boolean = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_OVERLAY) {
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            isOverlayRunning = false
            return START_NOT_STICKY
        }

        val gameName = intent?.getStringExtra(EXTRA_GAME_NAME) ?: "Free Fire"
        startForeground(NOTIFICATION_ID, buildForegroundNotification(gameName))
        showMovableOverlay(gameName)
        isOverlayRunning = true
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ZERO SHOT Companion Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live ZERO SHOT training status indicator"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(gameName: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ZERO SHOT: ACTIVE")
            .setContentText("Training Mode Ready • Status: $gameName")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showMovableOverlay(gameName: String) {
        if (overlayView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 150
        }

        // Create sleek floating badge programmatically
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 16, 24, 16)
            gravity = Gravity.CENTER_VERTICAL

            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(0xE6080B10.toInt()) // Deep translucent black
                setStroke(3, 0xFF00E5FF.toInt()) // Cyber cyan border
            }
            background = backgroundDrawable
        }

        // Live pulsing dot
        val dot = View(this).apply {
            val dotParams = LinearLayout.LayoutParams(18, 18).apply {
                rightMargin = 16
            }
            layoutParams = dotParams
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF00E676.toInt()) // Matrix green
            }
        }
        container.addView(dot)

        // Text view for status
        val statusText = TextView(this).apply {
            text = "ZERO SHOT • $gameName"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        }
        container.addView(statusText)

        // Close icon
        val closeBtn = ImageView(this).apply {
            val closeParams = LinearLayout.LayoutParams(36, 36).apply {
                leftMargin = 18
            }
            layoutParams = closeParams
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(0x88FFFFFF.toInt())
            setOnClickListener {
                stopSelf()
            }
        }
        container.addView(closeBtn)

        // Touch listener for dragging
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager?.updateViewLayout(overlayView, params)
                    } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // If tap without move, open main app
                    if (Math.abs(event.rawX - initialTouchX) < 10 && Math.abs(event.rawY - initialTouchY) < 10) {
                        val openApp = Intent(this@OverlayStatusService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(openApp)
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager?.addView(container, params)
            overlayView = container
        } catch (_: Exception) {}
    }

    private fun removeOverlay() {
        try {
            if (overlayView != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        removeOverlay()
        isOverlayRunning = false
        super.onDestroy()
    }
}
