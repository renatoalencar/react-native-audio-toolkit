package com.futurice.rctaudiotoolkit;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.util.Log;
import android.media.MediaPlayer;
import android.view.View;
import android.graphics.Bitmap;

import com.facebook.react.bridge.ReactApplicationContext;

public class PlayerNotification {
  public String BROADCAST_PLAYPAUSE = "com.futurice.rctaudiotoolkit.PLAYPAUSE";

  private ReactApplicationContext mContext;
  private NotificationCompat.Builder mBuilder;
  private MediaPlayer mPlayer;

  private String appName;
  private String title;
  private String imageUrl;

  private Bitmap mBitmap;

  public PlayerNotification(ReactApplicationContext context, MediaPlayer player) {
    mContext = context;
    mPlayer = player;
  }

  public PlayerNotification build(String appName, String title, String imageUrl, boolean isPlaying) {
    this.appName = appName;
    this.title = title;
    this.imageUrl = imageUrl;

    RemoteViews remoteViews = new RemoteViews(
      mContext.getPackageName(), R.layout.player_notification
    );
    remoteViews.setTextViewText(R.id.song_name_notification, appName);
    remoteViews.setTextViewText(R.id.album_name_notification, title);
    remoteViews.setOnClickPendingIntent(
      R.id.btn_streaming_notification_play,
      PendingIntent.getBroadcast(mContext, 0, new Intent(BROADCAST_PLAYPAUSE), 0)
    );
    remoteViews.setOnClickPendingIntent(
      R.id.btn_streaming_notification_pause,
      PendingIntent.getBroadcast(mContext, 0, new Intent(BROADCAST_PLAYPAUSE), 0)
    );
    remoteViews.setImageViewBitmap(
      R.id.album_image_notification, getImageBitmap(imageUrl)
    );
    updateButtons(isPlaying, remoteViews);

    mBuilder = new NotificationCompat.Builder(mContext)
      .setSmallIcon(mContext.getResources().getIdentifier("com.futurice.exampleapp:mipmap/ic_launcher", null, null))
      .setContentText("")
      .setContent(remoteViews);
    Intent resultIntent = new Intent(mContext, mContext.getCurrentActivity().getClass());
    resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    mBuilder.setContentIntent(contentIntent);

    return this;
  }

  public PlayerNotification present() {
    NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

    registerIntentFilters();
    mNotificationManager.notify(1, mBuilder.build());

    return this;
  }

  public void onPlayPause() {
    boolean isPlaying = mPlayer.isPlaying();

    if (isPlaying) {
      Log.d(AudioPlayerModule.LOG_TAG, "Pause stream");
      mPlayer.pause();
    } else {
      Log.d(AudioPlayerModule.LOG_TAG, "Play stream");
      mPlayer.start();
    }

    build(appName, title, imageUrl, isPlaying).present();
  }

  private BroadcastReceiver buildBroadcastReceiver() {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BROADCAST_PLAYPAUSE)) {
          Log.d(AudioPlayerModule.LOG_TAG, "Received play/pause action");
          onPlayPause();
        }
      }
    };
  }

  private void registerIntentFilters() {
    IntentFilter mIntentFilter = new IntentFilter(BROADCAST_PLAYPAUSE);
    mContext.registerReceiver(buildBroadcastReceiver(), mIntentFilter);
  }

  private void updateButtons(boolean isPlaying, RemoteViews remoteViews) {
    if (isPlaying) {
      remoteViews.setInt(R.id.btn_streaming_notification_play, "setVisibility", View.VISIBLE);
      remoteViews.setInt(R.id.btn_streaming_notification_pause, "setVisibility", View.GONE);
    } else {
      remoteViews.setInt(R.id.btn_streaming_notification_pause, "setVisibility", View.VISIBLE);
      remoteViews.setInt(R.id.btn_streaming_notification_play, "setVisibility", View.GONE);
    }
  }

  private Bitmap getImageBitmap(String imageUrl) {
    if (mBitmap == null) {
      mBitmap = BitmapUtils.loadBitmap(imageUrl);
    }

    return mBitmap;
  }
}
