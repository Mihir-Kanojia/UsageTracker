package com.example.usagetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class showUsage extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    ExtendedFloatingActionButton deleteBtn;

    public String android_id;
    public PackageManager packageManager;
    FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_usage);

        fstore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        packageManager = getPackageManager();

        DocumentReference documentReference = fstore.collection("" + android_id).document("0");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Log.d("TAG", "Snapshot taken: ");
                    assert document != null;
                    if (document.exists()) {
                        int count = Integer.parseInt(document.getString("appsCount"));
                        Log.d("TAG", "getssItemCount: " + count);

                        recyclerAdapter = new RecyclerAdapter(android_id, packageManager, count);
                        recyclerView.setAdapter(recyclerAdapter);

                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(showUsage.this, DividerItemDecoration.VERTICAL);
                        recyclerView.addItemDecoration(dividerItemDecoration);
                    }
                }
            }
        });

        deleteBtn = findViewById(R.id.deleteBtn);
        deleteBtn.shrink();
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (deleteBtn.isExtended()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(showUsage.this);
                    builder.setMessage("Do you want to delete all data.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(showUsage.this, MainActivity.class));
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    deleteBtn.shrink();
                                }
                            }).show();

                }
                deleteBtn.extend();
            }
        });

//        recyclerView.setLayoutManager(new LinearLayoutManager());
        recyclerView.setAdapter(recyclerAdapter);

    }
}