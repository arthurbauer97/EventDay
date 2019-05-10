package com.example.eventdayfinal.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventdayfinal.Activities.MainActivity;
import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class NewEventFragment extends Fragment implements Serializable {

    private Context context;
    private View view;
    private Button selectLocationMap;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private Uri selectedUri;
    private Button button_photo;
    private ImageView photo_event;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText nameEvent;
    private EditText dateEvent;
    private EditText ticketEvent;
    private EditText descriptionEvent;
    private EditText hourEvent;
    private Event event = new Event();

    private DatabaseReference databaseEventsReference;
    private DatabaseReference databasePlacesReference;
    private FirebaseDatabase firebaseDatabase;

    TextView criador_evento;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_event, container, false);

        findView(view);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        criador_evento.setText(firebaseUser.getDisplayName());

//        firebaseDatabase = FirebaseDatabase.getInstance();
//        databaseEventsReference = firebaseDatabase.getInstance().getReference("Events");
//        databasePlacesReference = firebaseDatabase.getInstance().getReference("Places");


        button_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });

        selectLocationMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainActivity)getActivity()).isUserAuth()) {
                    ((MainActivity)getActivity()).showEventPicker();
                    createEvent();
                }
                else ((MainActivity)getActivity()).showLoginDialog();
            }

        });

        return view ;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0){
            selectedUri = data.getData();
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),selectedUri);
                photo_event.setImageDrawable(new BitmapDrawable(bitmap));
                button_photo.setAlpha(0);
            } catch (IOException e) {

            }

        }
    }

    private void createEvent() {
        DocumentReference ref = db.collection("events").document();
        event.setNameEvent(nameEvent.getText().toString());
        event.setDateEvent(dateEvent.getText().toString());
        event.setDescriptionEvent(descriptionEvent.getText().toString());
        event.setHourEvent(hourEvent.getText().toString());
        event.setTicketEvent(ticketEvent.getText().toString());
        event.setIdEvent(ref.getId());
        ref.
                set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("Success", event.getIdEvent());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Failure", e.getMessage(), e);
                    }
                });
    }

    private void selectPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }

//    private void updatePhoto(Uri selectedUri){
//        String fileName = UUID.randomUUID().toString();
//        final StorageReference ref = FirebaseStorage.getInstance().getReference("/events/" + fileName);
//        ref.putFile(selectedUri)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//                                String urlPhoto = uri.toString();
//
//                            }
//                        })
//                    }
//                })
//
//    }

//    private void insertEventIntoDatabase() {
//
//        String nomeEvento = nameEvent.getText().toString();
//        String dataEvento = dateEvent.getText().toString();
//        String horarioEvento = hourEvent.getText().toString();
//        String ingressoEvento = ticketEvent.getText().toString();
//        String descricaEvento = descriptionEvent.getText().toString();
//
//        Event event = new Event(nomeEvento,horarioEvento,dataEvento,ingressoEvento,descricaEvento);
//
//        if (!TextUtils.isEmpty(nomeEvento) && !TextUtils.isEmpty(dataEvento) && !TextUtils.isEmpty(horarioEvento) && !TextUtils.isEmpty(ingressoEvento) && !TextUtils.isEmpty(descricaEvento)) {
//            databaseEventsReference.child(event.getIdEvent()).setValue(event);
//        }
//
//
//    }

    public void findView(View view){
        selectLocationMap = view.findViewById(R.id.buttonSelectLocationMap);
        criador_evento = view.findViewById(R.id.nomeCriadorDoEvento);
        photo_event = view.findViewById(R.id.photo_event);
        button_photo = view.findViewById(R.id.button_photo);
        dateEvent = view.findViewById(R.id.dateEvent);
        nameEvent = view.findViewById(R.id.nameEvent);
        hourEvent = view.findViewById(R.id.hourEvent);
        ticketEvent = view.findViewById(R.id.ticketEvent);
        descriptionEvent = view.findViewById(R.id.descriptionEvent);

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
