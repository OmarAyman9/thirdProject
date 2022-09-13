package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var urlUsed: String? = null
    private var selectedOption = 0
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        val loadingBtn: LoadingButton = findViewById(R.id.customBtn)

        radioGroup.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.glideRadioBtn -> urlUsed = GlideURL
                R.id.retrofitRadioBtn -> urlUsed = RertrofitURL
                R.id.loadAppRadioBtn -> urlUsed = URL
            }
        }
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        notificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        createChannel()
        loadingBtn.setOnClickListener {
            selectedOption = radioGroup.checkedRadioButtonId
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (context != null) {
                notificationManager.sendNotification(
                    getString(R.string.notification_description),
                    context
                )
                customBtn.changeState(ButtonState.Completed)
            }
        }
    }

    private fun download() {
        if (urlUsed == null) {
            Toast.makeText(this, "No File Selected", Toast.LENGTH_LONG).show()
            return
        }
        customBtn.changeState(ButtonState.Loading)
        val request =
            DownloadManager.Request(Uri.parse(urlUsed))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GlideURL =
            "https://github.com/bumptech/glide"
        private const val RertrofitURL =
            "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "channelId"
    }

    private fun NotificationManager.sendNotification(msgBody: String, context: Context) {
        val NOTIFICATION_ID = 0
        val contentIntent = Intent(context, DetailActivity::class.java).apply {
            putExtra("radioId", selectedOption)
        }
        pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        action = NotificationCompat.Action.Builder(
            R.drawable.ic_assistant_black_24dp,
            context.getString(R.string.notification_button),
            pendingIntent
        ).build()

        val builder = NotificationCompat.Builder(
            context,
            context.getString(R.string.load_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(msgBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(action)
        notify(NOTIFICATION_ID, builder.build())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.load_notification_channel_id)
            val channelName = getString(R.string.load_notification_channel_name)
            val notificationChannel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}