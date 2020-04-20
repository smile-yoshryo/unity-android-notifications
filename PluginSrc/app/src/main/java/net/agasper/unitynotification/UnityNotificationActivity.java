package net.agasper.unitynotification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;


public class UnityNotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // UnityPlayerActivity.onCreate() を呼び出す
        super.onCreate(savedInstanceState);

        // ここでUnity処理を呼びだす.
        final UnityNotificationActivity self = this;
        Intent intent = self.getIntent();
        String message = intent.getStringExtra("message");
        String soundName = intent.getStringExtra("soundName");
        Log.d("localpush", "activity onCreate: message="+message+", soundName="+soundName);
        UnityPlayer.UnitySendMessage("LocalNotification", "OnForcusFromNotification", "message="+message+"\nsoundName="+soundName+"");

        Intent i = new Intent(this.getApplication(), UnityPlayerActivity.class);
        this.startActivity(i);
        this.finish();
    }
}
