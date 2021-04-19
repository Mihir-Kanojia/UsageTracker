package com.example.usagetracker;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.ContentValues.TAG;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private static final String TAG = "RecyclerAdapter";
    private final String android_id;
    private final PackageManager packageManager;
    private final int AppsCount;

    FirebaseFirestore fstore;

    public RecyclerAdapter(String android_id, PackageManager packageManager, int AppsCount) {
        this.android_id = android_id;
        this.packageManager = packageManager;
        this.AppsCount = AppsCount;
        Log.d(TAG, "RecyclerAdapter: " + AppsCount);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        Log.d(TAG, "onCreateViewHolder: called");
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Log.d(TAG, "android_id: " + android_id);
        DocumentReference documentReference = fstore.collection("" + android_id).document("" + position);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Log.d(TAG, "Snapshot taken: ");
                    assert document != null;
                    if (document.exists()) {
                        String pkgName = document.getString("pkgName");
                        try {
                            Drawable d = packageManager.getApplicationIcon(pkgName + "");
                            holder.imageView.setBackgroundDrawable(d);
                        } catch (PackageManager.NameNotFoundException e) {
                            return;
                        }

                        holder.tv_pkgName.setText(pkgName);
                        holder.tv_time.setText(document.getString("timeUsed"));
                    } else {
                        Log.d(TAG, "No such document: ");
                    }

                } else {
                    Log.d(TAG, "get fialed with : " + task.getException());
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return AppsCount;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        ImageView imageView;
        TextView tv_pkgName, tv_time;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fstore = FirebaseFirestore.getInstance();
            imageView = itemView.findViewById(R.id.iconImage);
            tv_pkgName = itemView.findViewById(R.id.packageName);
            tv_time = itemView.findViewById(R.id.time);

            Log.d(TAG, "ViewHolder: ");
            itemView.setOnLongClickListener(this);
        }


        @Override
        public boolean onLongClick(View v) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("" + android_id).document("" + getAdapterPosition())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: Data Deleted");
                            notifyItemRemoved(getAdapterPosition());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onSuccess: Data not Deleted");
                        }
                    });
            notifyItemRemoved(getAdapterPosition());

            return true;
        }
    }

}
