package net.agasper.unitynotification;

import android.app.Activity;
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
        Log.d("localpush", "onStart: called");
        // ここでUnity処理を呼びだす
    }
}
