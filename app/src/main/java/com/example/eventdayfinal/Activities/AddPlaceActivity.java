package com.example.eventdayfinal.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.eventdayfinal.Fragments.NewEventFragment;
import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.Models.User;
import com.example.eventdayfinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;

public class AddPlaceActivity extends AppCompatActivity implements Serializable {

    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference database;
    private FirebaseAuth mAuth;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final User userData = new User(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail());

        final TextView eventTitle = findViewById(R.id.title_info_activity);
        final TextView eventAdress = findViewById(R.id.adress_info_activity);
        final TextView eventDateTimeRegistered = findViewById(R.id.reistered_time_info_activity);
        final Button reportPlaceButton = findViewById(R.id.report_button_info_window);

        final String titleToQuery = getIntent().getStringExtra("MARKER_TITLE");

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        DatabaseReference events = FirebaseDatabase.getInstance().getReference("Eventos");
        Query query = events.orderByChild("eventName").equalTo(titleToQuery.trim());
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = new Event();
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                     event = childSnapshot.getValue(Event.class);
                }

                eventTitle.setText(event.getEventName());
                eventAdress.setText(event.getAdress());

                final Event finalPlace = event;

                reportPlaceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        database.child("Eventos")
                                .setValue(finalPlace);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
