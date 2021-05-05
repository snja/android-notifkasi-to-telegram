package com.kunyink.notif_tele;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;
import android.widget.Toast;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class NotificationService extends NotificationListenerService {
    //    public static final String ACTION_INCOMING_MSG = "com.kunyink.notif_tele.INCOMING_MSG";
    private static SharedPreferences pref;
//    private String lastPost = "";

    @Override
    public void onCreate() {
        super.onCreate();
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onNotificationRemoved(sbn);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Notification noti = sbn.getNotification();
        Bundle extras = noti.extras;
        String title = null;
        String pack = sbn.getPackageName();
        String msg = null;
        String msg1 = (String) noti.tickerText;
        Object obj = extras.get(Notification.EXTRA_TEXT);
        String msg2 = null;

        try {
            SpannableString sp = (SpannableString) extras.get("android.title");
            title = Objects.requireNonNull(sp).toString();
        } catch (Exception e) {
            title = extras.getString(Notification.EXTRA_TITLE);
        }

        if (obj != null) {
            msg2 = obj.toString();
        }
        String msg3 = null;
        String msg4 = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            msg3 = extras.getString(Notification.EXTRA_BIG_TEXT);
        }

        String TAG = "NotificationService";
        try {
            SpannableString sp = (SpannableString) extras.get("android.text");
            Log.d(TAG, "title    " + title);
            Log.d(TAG, "pack     " + pack);
            Log.d(TAG, "ticker   " + msg1);
            Log.d(TAG, "text     " + msg2);
            Log.d(TAG, "big.text " + msg3);
            if (sp != null) {
                msg4 = sp.toString();
            }
            Log.d(TAG, "android.text " + msg4);
        } catch (Exception ignored) {
        }

        msg = msg1; // ticker text is default

        if (msg4 != null && msg4.length() > 0) msg = msg4;
        if (msg2 != null && msg2.length() > 0) msg = msg2;
        if (msg3 != null && msg3.length() > 0) msg = msg3;


        try {
            ApplicationInfo appi = this.getPackageManager().getApplicationInfo(pack, 0);
            Drawable icon = getPackageManager().getApplicationIcon(appi);
            pack = getPackageManager().getApplicationLabel(appi).toString();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        if (!sbn.isClearable()) return;

        if (title == null) title = pack;

        title = title.trim();
        if (title.endsWith(":")) {
            title = title.substring(0, title.lastIndexOf(":"));
        }

        if (msg == null) return;
        msg = replaceText(msg);
        title = replaceText(title);

        if (!pref.getBoolean("with_source", true)) pack = "";
        if (pack.equals("Telegram")) return;

        String[] textRequired = pref.getString("textRequired", "").trim().split("\n");
        String textFooter = pref.getString("textFooter", "");
        int conts = 0;
        for (String s : textRequired) {
            if (!title.contains(s) && !msg.contains(s)) {
                Log.d("textRequired", s);
                conts++;
            }
        }
        if (conts > 0) return;

        sendTelegram(title, msg, textFooter);
    }

    private void sendTelegram(String... strings) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String title = strings[0];
        String message = strings[1];
        String footer = strings[2];

        int chatId;
        try {
            chatId = Integer.parseInt(Objects.requireNonNull(pref.getString("telegramChatId", "")));
        } catch (NumberFormatException e) {
            Toast t = Toast.makeText(this, "telegram chat id must be a number", Toast.LENGTH_SHORT);
            t.show();
            return;
        }

        String botToken = pref.getString("telegramBotToken", "");
        boolean use_title = pref.getBoolean("use_title", false);
        if (Objects.requireNonNull(botToken).equals("")) {
            Toast t = Toast.makeText(this, "telegram information not entered", Toast.LENGTH_SHORT);
            t.show();
            return;
        }

        Random random = new Random();
        int r = random.nextInt(9999) + 1000;
        TelegramBot bot = new TelegramBot(botToken);
        footer = footer + " #" + r;

        message = (!use_title) ? message : title + ":\r\n" + message;
        SendMessage request = new SendMessage(chatId, message + "\r\n" + footer);
        bot.execute(request);
    }

    String replaceText(String txt) {
        String[] replacer = pref.getString("textHidden", "").trim().split("\n");
        for (String s : replacer) {
            txt = txt.replace(s, "");
            Log.d("REPLACER", txt);
        }
        txt = txt.replaceAll("#\\w+", "");
        return txt;
    }

}
