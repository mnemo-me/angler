package com.mnemo.angler.data.firebase_database;


import androidx.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class AnglerFirebaseDatabase {

    // Listener interface
    public interface OnSyncTimeStampsListener{
        void timestampSynced(long trialTimestamp, boolean isTrialInitialized);
    }

    public AnglerFirebaseDatabase() {
    }


    public void syncTimestamps(String accountId, long timestamp, OnSyncTimeStampsListener listener){

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(accountId);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Long trialTimestamp = (Long)dataSnapshot.child("trial_timestamp").getValue();

                                boolean isTrialInitialized = false;
                                Log.e("%%%%%%%%%%%", String.valueOf(trialTimestamp));
                                if (trialTimestamp == null){

                                    trialTimestamp = timestamp;
                                    databaseReference.child("trial_timestamp").setValue(trialTimestamp);

                                    isTrialInitialized = true;
                                }

                                listener.timestampSynced(trialTimestamp, isTrialInitialized);

                                firebaseAuth.signOut();
                                firebaseDatabase.goOffline();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
    }


}
