package com.example.usagetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;


public class MainActivity extends AppCompatActivity {

    Context context;
    FirebaseFirestore db;
    ExtendedFloatingActionButton letsGoBtn;
    showUsage showUsage;
    public String android_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        letsGoBtn = findViewById(R.id.letsGo);

        context = getApplicationContext();
        db = FirebaseFirestore.getInstance();


        letsGoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkUsageStatePermission()) {

                    showUsageStats();
                    startActivity(new Intent(context, showUsage.class));

                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Permission required.")
                            .setMessage("Click below to open settings.")
                            .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                                }
                            })
                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.this.finish();
                                    System.exit(0);
                                }
                            });
                    builder.show();

                }
            }
        });


    }

    private void showUsageStats() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar cal = Calendar.getInstance();
        List<UsageStats> queryUsageState;

        long start = 0;
        long stop = System.currentTimeMillis();

        cal.add(Calendar.DAY_OF_MONTH, -1);
        start = cal.getTimeInMillis();

        queryUsageState = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, stop);

        String stats_date = "";

        Log.d("TAG", "showUsageStats: queryUsageState.size()" + queryUsageState.size());

        for (int i = 0; i < queryUsageState.size(); i++) {

//            stats_date = stats_date + "PackageName : " + queryUsageState.get(i).getPackageName() + '\n' +
//                    "Last Time used : " + convertTime(queryUsageState.get(i).getLastTimeUsed()) + '\n' +
//                    "Describe Content : " + queryUsageState.get(i).describeContents() + '\n' +
//                    "First Time Stamp : " + convertTime(queryUsageState.get(i).getFirstTimeStamp()) + '\n' +
//                    "Last Time Stamp : " + convertTime(queryUsageState.get(i).getLastTimeStamp()) + '\n' +
//                    "Total time in Foreground  : " + convertTime(queryUsageState.get(i).getTotalTimeInForeground()) + "\n\n";

//                        "Total time in ScreenTime  : "+convertTime(queryUsageState.get(i).getTotalTimeVisible())+"\n\n";

//            stats_date = stats_date + "PackageName : " + queryUsageState.get(i).getPackageName() + '\n' +
//                    "Last Time used : " + queryUsageState.get(i).getLastTimeUsed() + '\n' +
//                    "First Time Stamp : " + queryUsageState.get(i).getFirstTimeStamp() + '\n' +
//                    "Last Time Stamp : " + queryUsageState.get(i).getLastTimeStamp() + '\n' +
//                    "Total time in Foreground  : " + queryUsageState.get(i).getTotalTimeInForeground() + "\n\n";

//            stats_date = stats_date + "PackageName : " + queryUsageState.get(i).getPackageName() + '\n' +
//                    "Contents : " + queryUsageState.get(i).describeContents()
//                    + "\nTotal Time used in 24hrs" +
//                    "\n " + (int) ((TimeInforground / (1000 * 60 * 60)) % 24) + "Hrs  "
//                    + (int) ((TimeInforground / (1000 * 60)) % 60) + "Min  " + (int) (TimeInforground / 1000) % 60 + "Secs\n\n\n";

            long totalTimeInForeground = queryUsageState.get(i).getTotalTimeInForeground();
//            long totalTimeForegroundServiceUsed = queryUsageState.get(i).getTotalTimeInForeground();
//            long firstTimeStamp = queryUsageState.get(i).getFirstTimeStamp();
//            long lastTimeStamp = queryUsageState.get(i).getLastTimeStamp();

            String packageName = queryUsageState.get(i).getPackageName() + "";

            int timeInHours = (int) ((totalTimeInForeground / (1000 * 60 * 60)) % 24);
            int timeInMin = (int) ((totalTimeInForeground / (1000 * 60)) % 60);
            int timeInSec = ((int) (totalTimeInForeground / 1000) % 60);

            String trimmedTime = timeInHours + " Hrs " + timeInMin + " Min and " + timeInSec + " Secs";

            Map<String, Object> user = new HashMap<>();
            user.put("pkgName", packageName + "");
            user.put("timeUsed", trimmedTime + "");
            user.put("appsCount", queryUsageState.size()+"");

            android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
//            Toast.makeText(context, ""+android_id, Toast.LENGTH_SHORT).show();
            Log.d("TAGID", "adroid_idhai: " + android_id);

            DocumentReference documentReference = db.collection(android_id).document(String.valueOf(i));

            documentReference.set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "onSuccess: document added" + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", "onFailure: document not added" + e);
                        }
                    });

//            String timeInFor = (int) ((totalTimeInForeground / (1000 * 60 * 60)) % 24) + ":" +
//                    (int) ((totalTimeInForeground / (1000 * 60)) % 60) + ":" +
//                    (int) (totalTimeInForeground / 1000) % 60 + "";
//
//            String fTimeStamp = (int) ((firstTimeStamp / (1000 * 60 * 60)) % 24) + ":" +
//                    (int) ((firstTimeStamp / (1000 * 60)) % 60) + ":" +
//                    (int) (firstTimeStamp / 1000) % 60 + "";
//
//            String lTimeStamp = (int) ((lastTimeStamp / (1000 * 60 * 60)) % 24) + ":" +
//                    (int) ((lastTimeStamp / (1000 * 60)) % 60) + ":" +
//                    (int) (lastTimeStamp / 1000) % 60 + "";

            stats_date = stats_date + "Pkg name : " + packageName +
                    "\nTotal time Used : " + trimmedTime + "\n\n";
//                    "\nfirstTS    : "+fTimeStamp+
//                    "\nlastTS     : "+lTimeStamp+"\n\n\n";


        }

    }

    private boolean checkUsageStatePermission() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == MODE_ALLOWED;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }
}


