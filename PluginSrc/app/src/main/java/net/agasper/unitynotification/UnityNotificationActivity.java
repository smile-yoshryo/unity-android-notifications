package net.agasper.unitynotification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;

public class UnityNotificationActivity extends UnityPlayerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("localpush", "onCreate: called");
        // UnityPlayerActivity.onCreate() を呼び出す
        super.onCreate(savedInstanceState);

    }

    @Override
    protected  void onStart() {
        super.onStart();

        Intent intent = this.getIntent();
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String soundName = intent.getStringExtra("soundName");
        Log.d("localpush", "onStart: called, title="+title+", message="+message+", soundName="+soundName);

        // ここでUnity処理を呼びだす

    }
}
