/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ozner.cup.Alarm;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.util.Log;

import com.ozner.cup.Device.SetupReplenTimeActivity;
import com.ozner.cup.R;


/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
 * activity.  Passes through Alarm ID.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * If the alarm is older than STALE_WINDOW, ignore.  It
     * is probably the result of a time or timezone change
     */
    private final static int STALE_WINDOW = 30 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Alarms.ALARM_KILLED.equals(intent.getAction())) {
            // The alarm has been killed, update the notification
            updateNotification(context, (Alarm)
                            intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA),
                    intent.getIntExtra(Alarms.ALARM_KILLED_TIMEOUT, -1));
            return;
        } else if (Alarms.CANCEL_SNOOZE.equals(intent.getAction())) {
            Alarms.saveSnoozeAlert(context, intent.getIntExtra(Alarms.ALARM_ID,-1), -1);
            return;
        } else if (!Alarms.ALARM_ALERT_ACTION.equals(intent.getAction())) {
            // Unknown intent, bail.
            return;
        }
        Alarm alarm = null;
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // Alarm object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Alarm.CREATOR.createFromParcel(in);
        }

        if (alarm == null) {
            Log.v("mdy", "Failed to parse the alarm from the intent");
            // Make sure we set the next alert if needed.
            Alarms.setNextAlert(context);
            return;
        }
        // Disable the snooze alert if this alarm is the snooze.
        Alarms.disableSnoozeAlert(context, alarm.id);
        // Disable this alarm if it does not repeat.
        if (!alarm.daysOfWeek.isRepeatSet()) {
            Alarms.enableAlarm(context, alarm.id, false);
        } else {
            // Enable the next alert if there is one. The above call to
            // enableAlarm will call setNextAlert so avoid calling it twice.
            Alarms.setNextAlert(context);
        }

        // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
        // pick it up.
        AlarmAlertWakeLock.acquireCpuWakeLock(context);
        /* Close dialogs and window shade */
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

        // Decide which activity to start based on the state of the keyguard.
        Class c = AlarmAlert.class;
        KeyguardManager km = (KeyguardManager) context.getSystemService(
                Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            // Use the full screen activity for security.
            c = AlarmAlertFullScreen.class;
        }
        // Play the alarm alert and vibrate the device.
        Intent playAlarm = new Intent(context, AlarmKlaxon.class);
//        playAlarm.setAction(Alarms.ALARM_ALERT_ACTION);
        playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        if (context == null) {
            try {
                context = SetupReplenTimeActivity.class.newInstance().getBaseContext();
                context.startService(playAlarm);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            context.startService(playAlarm);
        }
        // Trigger a notification that, when clicked, will show the alarm alert
        // dialog. No need to check for fullscreen since this will always be
        // launched from a user action.
        Intent notify = new Intent(context, AlarmAlert.class);
        notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        PendingIntent pendingNotify = PendingIntent.getActivity(context,
                alarm.id, notify, 0);
        // Use the alarm's label or the default label as the ticker text and
        // main text of the notification.
        Notification notification = new Notification.Builder(context)
                .setAutoCancel(true).setTicker("补水时间到了，亲")
                .setSmallIcon(R.mipmap.ozner)
                .setContentText("补水时间到了，亲").setWhen(alarm.time)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setContentIntent(pendingNotify).build();

        // NEW: Embed the full-screen UI here. The notification manager will
        // take care of displaying it if it's OK to do so.
        Intent alarmAlert = new Intent(context, c);
        alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        notification.fullScreenIntent = PendingIntent.getActivity(context, alarm.id, alarmAlert, 0);

        // Send the notification using the alarm id to easily identify the
        // correct notification.
        NotificationManager nm = getNotificationManager(context);
        nm.notify(alarm.id, notification);
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateNotification(Context context, Alarm alarm, int timeout) {
        NotificationManager nm = getNotificationManager(context);

        // If the alarm is null, just cancel the notification.
        if (alarm == null) {
            if (true) {
                Log.v("mdy", "Cannot update notification for killer callback");
            }
            return;
        }

        // Launch SetAlarm when clicked.
        Intent viewAlarm = new Intent(context, SetupReplenTimeActivity.class);
        viewAlarm.putExtra(Alarms.ALARM_ID, alarm.id);
        PendingIntent intent =
                PendingIntent.getActivity(context, alarm.id, viewAlarm, 0);

        // Update the notification to indicate that the alert has been
        // silenced.
        String label = alarm.getLabelOrDefault(context);
        Notification builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ozner)
                .setTicker("补水时间到了，亲")
                .setContentTitle("")//设置标题
                .setContentText("补水时间到了，亲")//消息内容
                .setWhen(alarm.time) //发送时间
                .setDefaults(Notification.DEFAULT_ALL) //设置默认的提示音，振动方式，灯光
                .setAutoCancel(true)//打开程序后图标消失
                .setContentIntent(intent)
                .build();
        // We have to cancel the original notification since it is in the
        // ongoing section and we want the "killed" notification to be a plain
        // notification.
        nm.cancel(alarm.id);
        nm.notify(alarm.id, builder);
    }
}
