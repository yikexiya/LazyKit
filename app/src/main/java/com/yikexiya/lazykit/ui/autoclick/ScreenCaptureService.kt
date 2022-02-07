package com.yikexiya.lazykit.ui.autoclick

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.app.MainActivity
import com.yikexiya.lazykit.util.getImageDir
import com.yikexiya.lazykit.util.logW
import java.io.File
import java.io.FileOutputStream

class ScreenCaptureService : Service() {

    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread("capture")
        thread.start()
        handler = Handler(thread.looper)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (val action = intent.getStringExtra(SERVICE_ACTION)) {
            ACTION_CAPTURE -> capture(intent)
            else -> logW("can't find action code: $action")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("WrongConstant")
    private fun capture(intent: Intent) {
        val requestCode = intent.getIntExtra("requestCode", 0)
        val data = intent.getParcelableExtra<Intent>("data") ?: return
        val builder = Notification.Builder(this, "test")
        val navIntent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notificationChannel = NotificationChannel("com.yikexiya.lazykit", "ss", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
        val activity = PendingIntent.getActivity(this, 0, navIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
        builder.setContentIntent(activity)
            .setChannelId("com.yikexiya.lazykit")
            .setContentTitle("test haha")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground))
            .setWhen(System.currentTimeMillis())
        val notification = builder.build()
        startForeground(100, notification)
        val mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
        val mediaProjection = mediaProjectionManager.getMediaProjection(requestCode, data)
        val displayMetrics = Resources.getSystem().displayMetrics
        val imageReader = ImageReader.newInstance(
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            PixelFormat.RGBA_8888,
            2
        )
        val virtualDisplay = mediaProjection.createVirtualDisplay(
            "test",
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )
        handler.postDelayed({
            val image = imageReader.acquireLatestImage()
            if (image != null) {
                val plane = image.planes[0]
                val buffer = plane.buffer
                val rowStride = plane.rowStride
                val pixelStride = plane.pixelStride
                val padding = rowStride - image.width * pixelStride
                val bitmap = if (padding > 0) {
                    val oldBitmap = Bitmap.createBitmap(rowStride / pixelStride, image.height, Bitmap.Config.ARGB_8888)
                    oldBitmap.copyPixelsFromBuffer(buffer)
                    val newBitmap = Bitmap.createScaledBitmap(oldBitmap, image.width, image.height, false)
                    oldBitmap.recycle()
                    newBitmap
                } else {
                    val bitmap = Bitmap.createBitmap(rowStride / pixelStride, image.height, Bitmap.Config.ARGB_8888)
                    bitmap.copyPixelsFromBuffer(buffer)
                    bitmap
                }
                FileOutputStream(File(getImageDir(this), "test.jpg")).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    bitmap.recycle()
                }
                image.close()
                virtualDisplay.release()
                mediaProjection.stop()
            }
        }, 1000)
    }

    override fun onDestroy() {
        handler.looper.quitSafely()
        super.onDestroy()
    }

    companion object {
        private const val SERVICE_ACTION = "SERVICE_ACTION"
        private const val ACTION_CAPTURE = "ACTION_CAPTURE"
        fun capture(context: Context, requestCode: Int, data: Intent) {
            val intent = Intent(context, ScreenCaptureService::class.java)
            intent.putExtra(SERVICE_ACTION, ACTION_CAPTURE)
            intent.putExtra("requestCode", requestCode)
            intent.putExtra("data", data)
            context.startForegroundService(intent)
        }
    }
}