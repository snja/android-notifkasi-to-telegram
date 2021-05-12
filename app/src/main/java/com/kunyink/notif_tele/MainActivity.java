package com.kunyink.notif_tele;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    //    private TextView txtView;
    private TextInputLayout telegramBotToken, telegramChatId, textHidden, textRequired, textFooter, textRepeat;
    private Switch enabledService;
    private ScrollView scrollView;
    private CheckBox checkTitle;
    //    private NotificationReceiver nReceiver;
    private SharedPreferences pref;

    //    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = this.getSharedPreferences("kunyink", MODE_PRIVATE);

//        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        telegramBotToken = findViewById(R.id.telegramBotToken);
        telegramChatId = findViewById(R.id.telegramChatId);
        textHidden = findViewById(R.id.textHidden);
        textRequired = findViewById(R.id.textRequired);
        textFooter = findViewById(R.id.textFooter);
        checkTitle = findViewById(R.id.checkTitle);
        textRepeat = findViewById(R.id.txtRepeat);
        enabledService = findViewById(R.id.serviceEnable);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Set<String> packs = NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext());
        boolean readNotiPermissions = packs.contains(getPackageName());
        if (!readNotiPermissions) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(intent);
                }
            });
            alertDialogBuilder.setMessage("please allow necessary permissions");
            alertDialogBuilder.show();
        }


        String telegramBotToken_ = pref.getString("telegramBotToken", "");
        String telegramChatId_ = pref.getString("telegramChatId", "");
        String textHidden_ = pref.getString("textHidden", "");
        String textRequired_ = pref.getString("textRequired", "");
        String textFooter_ = pref.getString("textFooter", "");
        boolean checkTitle_ = pref.getBoolean("use_title", true);
        int textRepeat_ = pref.getInt("textRepeat", 1);
        boolean enabled_ = pref.getBoolean("enabledService", true);


        telegramBotToken.getEditText().setText(telegramBotToken_);
        telegramChatId.getEditText().setText(telegramChatId_);
        textHidden.getEditText().setText(textHidden_);
        textRequired.getEditText().setText(textRequired_);
        textFooter.getEditText().setText(textFooter_);
        checkTitle.setChecked(checkTitle_);
        textRepeat.getEditText().setText(textRepeat_);
        enabledService.setChecked(enabled_);
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePref();
    }

    void savePref() {
        String telegramBotToken_ = telegramBotToken.getEditText().getText().toString();
        String telegramChatId_ = telegramChatId.getEditText().getText().toString();
        String textHidden_ = textHidden.getEditText().getText().toString().trim();
        String textRequired_ = textRequired.getEditText().getText().toString().trim();
        String textFooter_ = textFooter.getEditText().getText().toString().trim();
        String textRepeat_ = textRepeat.getEditText().getText().toString().trim();
        boolean checkTitle_ = checkTitle.isChecked();
        boolean enable_ = enabledService.isChecked();
        SharedPreferences.Editor pe = pref.edit();

        pe.putString("telegramBotToken", telegramBotToken_)
                .putString("telegramChatId", telegramChatId_)
                .putString("textHidden", textHidden_)
                .putString("textRequired", textRequired_)
                .putString("textFooter", textFooter_)
                .putInt("textRepeat", Integer.parseInt(textRepeat_))
                .putBoolean("use_title", checkTitle_)
                .putBoolean("enabledService", enable_)
                .apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("kunyink", "kunyink", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("kunyink");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        ComponentName cn = new ComponentName(this, com.kunyink.notif_tele.NotificationService.class);
        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


    //    @RequiresApi(api = Build.VERSION_CODES.O)
    public void buttonClicked(View v) {
        savePref();
//        toggleNotificationListenerService();
    }

}

