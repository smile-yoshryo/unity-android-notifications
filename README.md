## 元のREADME
https://github.com/Agasper/unity-android-notifications/blob/master/README.md

---

## 補足情報
### Android
元が抜き身のjarで出力しているためOBSOLETEな形式ではあるが、  
エンバグを考慮してしばらくこの形で合わせる意向。  

プラグイン内にActivityを設け、通知からの起動を検知するようにしたため、  
受け側のUnityプロジェクトのAndroidManifestでは以下のように追記すること。  

```
....
    </activity>

    <!-- 通知からの起動アクティビティ-->
    <activity android:name="net.agasper.unitynotification.UnityNotificationActivity"  android:parentActivityName="com.unity3d.player.UnityPlayerActivity" >
      <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
        <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
      </intent-filter>
    </activity>
```
