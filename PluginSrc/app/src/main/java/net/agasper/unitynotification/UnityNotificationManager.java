package net.agasper.unitynotification;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class UnityNotificationManager extends BroadcastReceiver
{
    private static Set<String> channels = new HashSet<>();

    public static void CreateChannel(String identifier, String name, String description, int importance, String soundName, int enableLights, int lightColor, int enableVibration, long[] vibrationPattern, String bundle) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        channels.add(identifier);

        NotificationManager nm = (NotificationManager) UnityPlayer.currentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(identifier, name, importance);
        channel.setDescription(description);
        if (soundName != null) {
            Resources res = UnityPlayer.currentActivity.getResources();
            int id = res.getIdentifier("raw/" + soundName, null, UnityPlayer.currentActivity.getPackageName());
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            channel.setSound(Uri.parse("android.resource://" + bundle + "/" + id), audioAttributes);
        }
        channel.enableLights(enableLights == 1);
        channel.setLightColor(lightColor);
        channel.enableVibration(enableVibration == 1);
        if (vibrationPattern == null)
            vibrationPattern = new long[] { 1000L, 1000L };
        channel.setVibrationPattern(vibrationPattern);
        nm.createNotificationChannel(channel);
    }

    @TargetApi(24)
    private static void createChannelIfNeeded(String identifier, String name, String soundName, boolean enableLights, boolean enableVibration, String bundle) {
        if (channels.contains(identifier))
            return;
        channels.add(identifier);

        CreateChannel(identifier, name, identifier + " notifications", NotificationManager.IMPORTANCE_DEFAULT, soundName, enableLights ? 1 : 0, Color.GREEN, enableVibration ? 1 : 0, null, bundle);
    }

    public static void SetNotification(int id, long delayMs, String title, String message, String ticker, int sound, String soundName, int vibrate,
                                       int lights, String largeIconResource, String smallIconResource, int bgColor, String bundle, String channel,
                                       ArrayList<NotificationAction> actions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (channel == null)
                channel = "default";
            createChannelIfNeeded(channel, title, soundName, lights == 1, vibrate == 1, bundle);
        }

        Activity currentActivity = UnityPlayer.currentActivity;
        AlarmManager am = (AlarmManager)currentActivity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(currentActivity, UnityNotificationManager.class);
        intent.putExtra("ticker", ticker);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("id", id);
        intent.putExtra("color", bgColor);
        intent.putExtra("sound", sound == 1);
        intent.putExtra("soundName", soundName);
        intent.putExtra("vibrate", vibrate == 1);
        intent.putExtra("lights", lights == 1);
        intent.putExtra("l_icon", largeIconResource);
        intent.putExtra("s_icon", smallIconResource);
        intent.putExtra("bundle", bundle);
        intent.putExtra("channel", channel);
        Bundle b = new Bundle();
        b.putParcelableArrayList("actions", actions);
        intent.putExtra("actionsBundle", b);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, PendingIntent.getBroadcast(currentActivity, id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        else
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, PendingIntent.getBroadcast(currentActivity, id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void SetRepeatingNotification(int id, long delayMs, String title, String message, String ticker, long rep, int sound, String soundName, int vibrate, int lights,
                                                String largeIconResource, String smallIconResource, int bgColor, String bundle, String channel, ArrayList<NotificationAction> actions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (channel == null)
                channel = "default";
            createChannelIfNeeded(channel, title, soundName, lights == 1, vibrate == 1, bundle);
        }

        Activity currentActivity = UnityPlayer.currentActivity;
        AlarmManager am = (AlarmManager)currentActivity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(currentActivity, UnityNotificationManager.class);
        intent.putExtra("ticker", ticker);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("id", id);
        intent.putExtra("color", bgColor);
        intent.putExtra("sound", sound == 1);
        intent.putExtra("soundName", soundName);
        intent.putExtra("vibrate", vibrate == 1);
        intent.putExtra("lights", lights == 1);
        intent.putExtra("l_icon", largeIconResource);
        intent.putExtra("s_icon", smallIconResource);
        intent.putExtra("bundle", bundle);
        intent.putExtra("channel", channel);
        Bundle b = new Bundle();
        b.putParcelableArrayList("actions", actions);
        intent.putExtra("actionsBundle", b);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, rep, PendingIntent.getBroadcast(currentActivity, id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void SetCalendarNotification(int id, int year, int month, int day, int hour, int minute, int repType,
                                               String title, String message, String ticker, int sound, String soundName, int vibrate, int lights,
                                               String largeIconResource, String smallIconResource, int bgColor, String bundle, String channel, ArrayList<NotificationAction> actions)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (channel == null) {
            channel = "default";
        }
        createChannelIfNeeded(channel, title, soundName, lights == 1, vibrate == 1, bundle);

        Activity currentActivity = UnityPlayer.currentActivity;
        AlarmManager am = (AlarmManager)currentActivity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(currentActivity, UnityNotificationManager.class);
        intent.putExtra("ticker", ticker);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("id", id);
        intent.putExtra("color", bgColor);
        intent.putExtra("sound", sound == 1);
        intent.putExtra("soundName", soundName);
        intent.putExtra("vibrate", vibrate == 1);
        intent.putExtra("lights", lights == 1);
        intent.putExtra("l_icon", largeIconResource);
        intent.putExtra("s_icon", smallIconResource);
        intent.putExtra("bundle", bundle);
        intent.putExtra("channel", channel);
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", day);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        intent.putExtra("type_repeat", repType);
        Bundle b = new Bundle();
        b.putParcelableArrayList("actions", actions);
        intent.putExtra("actionsBundle", b);

        // カレンダーで発火時間を作成.
        Calendar calendar = new Calendar.Builder()
                                        .setDate(year, month-1, day)  // monthは0始まり
                                        .setTimeOfDay(hour, minute, 0)
                                        .build();
        DateFormat format =  new SimpleDateFormat("yyyy年MM月dd日(E) HH:mm");
        Log.d("localpush", "SetCalendarNotification: "+format.format(calendar.getTime()));

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent.getBroadcast(currentActivity, id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private enum RepeatType
    {
        None,
        Day,
        Week
    }

    public void onReceive(final Context context, Intent intent)
    {
        Log.d("localpush", "onReceive: ");

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        String ticker = intent.getStringExtra("ticker");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String s_icon = intent.getStringExtra("s_icon");
        String l_icon = intent.getStringExtra("l_icon");
        int color = intent.getIntExtra("color", 0);
        String bundle = intent.getStringExtra("bundle");
        Boolean sound = intent.getBooleanExtra("sound", false);
        String soundName = intent.getStringExtra("soundName");
        Boolean vibrate = intent.getBooleanExtra("vibrate", false);
        Boolean lights = intent.getBooleanExtra("lights", false);
        int id = intent.getIntExtra("id", 0);
        String channel = intent.getStringExtra("channel");
        Bundle b = intent.getBundleExtra("actionsBundle");
        ArrayList<NotificationAction> actions = null;
        if (b != null && b.containsKey("actions")) {
            actions = b.getParcelableArrayList("actions");
        }

        Resources res = context.getResources();

        Intent resultIntent = new Intent(context, UnityNotificationActivity.class);
        // Activity側で検知できるように必要情報の入れ直し.
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("soundName", soundName);
//        Intent notificationIntent = context.getPackageManager().getLaunchIntentForPackage(bundle);  // bundle由来のintentは使用せず独自作成しているintentを使用することで通知からの起動を検知する.

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
//        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (channel == null)
            channel = "default";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel);

        builder.setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.setColor(color);

        if (ticker != null && ticker.length() > 0)
            builder.setTicker(ticker);

        if (s_icon != null && s_icon.length() > 0)
            builder.setSmallIcon(res.getIdentifier(s_icon, "drawable", context.getPackageName()));

        if (l_icon != null && l_icon.length() > 0)
            builder.setLargeIcon(BitmapFactory.decodeResource(res, res.getIdentifier(l_icon, "drawable", context.getPackageName())));

        if (sound) {
            if (soundName != null) {
                int identifier = res.getIdentifier("raw/" + soundName, null, context.getPackageName());
                builder.setSound(Uri.parse("android.resource://" + bundle + "/" + identifier));
            } else
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        if (vibrate)
            builder.setVibrate(new long[] {
                    1000L, 1000L
            });

        if (lights)
            builder.setLights(Color.GREEN, 3000, 3000);

        if (actions != null) {
            for (int i = 0; i < actions.size(); i++) {
                NotificationAction action = actions.get(i);
                int icon = 0;
                if (action.getIcon() != null && action.getIcon().length() > 0)
                    icon = res.getIdentifier(action.getIcon(), "drawable", context.getPackageName());
                builder.addAction(icon, action.getTitle(), buildActionIntent(action, i, context));
            }
        }

        Notification notification = builder.build();
        notificationManager.notify(id, notification);

        // APILevel23以上でカレンダー設定で繰り返しスケジュールを行なっている場合はレシーブ段階で次のリピート.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int repType = intent.getIntExtra("type_repeat", 0);
            if(repType > 0){
                AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

                int year = intent.getIntExtra("year", -1);
                int month = intent.getIntExtra("month", -1);
                int day = intent.getIntExtra("day", -1);
                int hour = intent.getIntExtra("hour", -1);
                int minute = intent.getIntExtra("minute", -1);
                Calendar calendar = new Calendar.Builder()
                                                .setDate(year, month-1, day)
                                                .setTimeOfDay(hour, minute, 0)
                                                .build();
                calendar.add(Calendar.DAY_OF_MONTH, repType == RepeatType.Week.ordinal() ? 7 : 0);
                calendar.add(Calendar.DAY_OF_MONTH, repType == RepeatType.Day.ordinal() ? 1 : 0);
                // debug
                DateFormat format =  new SimpleDateFormat("yyyy年MM月dd日(E) HH:mm");
                Log.d("localpush", "reschedule: "+format.format(calendar.getTime()));

                // intent設定し直し
                intent.putExtra("year", calendar.get(Calendar.YEAR));
                intent.putExtra("month", calendar.get(Calendar.MONTH)+1);
                intent.putExtra("day", calendar.get(Calendar.DAY_OF_MONTH));
                intent.putExtra("hour", calendar.get(Calendar.HOUR));
                intent.putExtra("minute", calendar.get(Calendar.MINUTE));

                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
    }

    private static PendingIntent buildActionIntent(NotificationAction action, int id,Context context) {
        Intent intent = new Intent(context, UnityNotificationActionHandler.class);
        intent.putExtra("id", id);
        intent.putExtra("gameObject", action.getGameObject());
        intent.putExtra("handlerMethod", action.getHandlerMethod());
        intent.putExtra("actionId", action.getIdentifier());
        intent.putExtra("foreground", action.isForeground());
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void CancelPendingNotification(int id)
    {
        Activity currentActivity = UnityPlayer.currentActivity;
        AlarmManager am = (AlarmManager)currentActivity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(currentActivity, UnityNotificationManager.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(currentActivity, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pendingIntent);
    }

    public static void ClearShowingNotifications()
    {
        Activity currentActivity = UnityPlayer.currentActivity;
        NotificationManager nm = (NotificationManager)currentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }
}
