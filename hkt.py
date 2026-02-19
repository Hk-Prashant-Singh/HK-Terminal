import os

def create_project():
    # Base Directory Structure
    base_path = "HKTerminal/app/src/main"
    java_path = f"{base_path}/java/com/hk/hkterminal"
    res_path = f"{base_path}/res/layout"
    
    # List of directories to create
    dirs = [java_path, res_path, "HKTerminal/app/src/main/res/values"]
    for d in dirs:
        os.makedirs(d, exist_ok=True)

    # --- 1. MainActivity.java ---
    with open(f"{java_path}/MainActivity.java", "w") as f:
        f.write('''package com.hk.hkterminal;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    TextView outputView; EditText inputCommand; Button runButton; ScrollView scrollView; boolean rootMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputView = findViewById(R.id.outputView);
        inputCommand = findViewById(R.id.inputCommand);
        runButton = findViewById(R.id.runButton);
        scrollView = findViewById(R.id.scrollView);
        rootMode = RootUtils.isRootAvailable();
        outputView.setText(rootMode ? "ROOT MODE\\n" : "USER MODE\\n");
        runButton.setOnClickListener(v -> {
            String cmd = inputCommand.getText().toString();
            outputView.append("\\n$ " + cmd + "\\n");
            inputCommand.setText("");
            TerminalEngine.runCommand(cmd, rootMode, result -> {
                runOnUiThread(() -> {
                    outputView.append(result);
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                });
            });
        });
    }
}''')

    # --- 2. TerminalEngine.java ---
    with open(f"{java_path}/TerminalEngine.java", "w") as f:
        f.write('''package com.hk.hkterminal;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TerminalEngine {
    public interface CommandCallback { void onOutput(String output); }
    public static void runCommand(String command, boolean rootMode, CommandCallback callback) {
        new Thread(() -> {
            StringBuilder output = new StringBuilder();
            try {
                Process process = rootMode ? Runtime.getRuntime().exec(new String[]{"su","-c",command}) 
                                         : Runtime.getRuntime().exec(command);
                BufferedReader stdReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = stdReader.readLine()) != null) output.append(line).append("\\n");
                while ((line = errReader.readLine()) != null) output.append("[ERROR] ").append(line).append("\\n");
                process.waitFor();
            } catch (Exception e) { output.append("Exception: ").append(e.getMessage()); }
            callback.onOutput(output.toString());
        }).start();
    }
}''')

    # --- 3. RootUtils.java ---
    with open(f"{java_path}/RootUtils.java", "w") as f:
        f.write('''package com.hk.hkterminal;
public class RootUtils {
    public static boolean isRootAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            int exitCode = process.waitFor();
            process.destroy();
            return exitCode == 0;
        } catch (Exception e) { return false; }
    }
}''')

    # --- 4. activity_main.xml ---
    with open(f"{res_path}/activity_main.xml", "w") as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:background="#000000">
    <ScrollView android:id="@+id/scrollView" android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1">
        <TextView android:id="@+id/outputView" android:layout_width="match_parent" android:layout_height="wrap_content"
            android:textColor="#00FF00" android:textSize="14sp" android:typeface="monospace"/>
    </ScrollView>
    <EditText android:id="@+id/inputCommand" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:hint="Enter command" android:textColor="#FFFFFF"/>
    <Button android:id="@+id/runButton" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Run"/>
</LinearLayout>''')

    # --- 5. AndroidManifest.xml ---
    with open(f"HKTerminal/app/src/main/AndroidManifest.xml", "w") as f:
        f.write('''<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.hk.hkterminal">
    <uses-permission android:name="android.permission.INTERNET"/>
    <application android:allowBackup="true" android:theme="@style/Theme.AppCompat.NoActionBar">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
</manifest>''')

    # --- 6. build.gradle ---
    with open(f"HKTerminal/app/build.gradle", "w") as f:
        f.write('''plugins { id 'com.android.application' }
android {
    namespace 'com.hk.hkterminal'
    compileSdk 34
    defaultConfig { applicationId "com.hk.hkterminal"; minSdk 21; targetSdk 34; versionCode 1; versionName "1.0" }
}
dependencies { implementation 'androidx.appcompat:appcompat:1.6.1' }''')

    print("\\033[1;32m[SUCCESS] Prashant Bhai, poora project generate ho gaya!\\033[0m")

if __name__ == "__main__":
    create_project()
