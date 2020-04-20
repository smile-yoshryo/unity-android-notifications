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
        int id = intent.getIntExtra("identifier", 0);
        Log.d("localpush", "onStart: called, id="+id);

        // ここでUnity処理を呼びだす

    }
}
