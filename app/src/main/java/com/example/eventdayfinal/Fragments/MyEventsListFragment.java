package com.example.eventdayfinal.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventdayfinal.Activities.MainActivity;
import com.example.eventdayfinal.Adapters.MyEventsAdapter;
import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.R;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


public class MyEventsListFragment extends Fragment{

    private ListView myEventsListView;
    private TextView noResultsTextView;
    private ArrayList<Event> eventsArray = new ArrayList<>();

    private Event event = new Event();


    //create event dialog
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private View view;
    private EditText nameEvent;
    private EditText dateEvent;
    private EditText ticketEvent;
    private EditText descriptionEvent;
    private EditText hourEvent;
    private ImageView photo_event;
    private ImageButton editPhotoEvent;
    private Button buttonSaveEvent;
    private ImageView editEvent;
    private ImageView deleteEvent;
    private TextView noEvent;

    private Uri selectedUri;
    private String photo;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    private boolean validPhoto = false;

    private ProgressBar progressBar;

    private int num;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_my_events, container, false);

        myEventsListView = view.findViewById(R.id.events_mylistview);
        noEvent = view.findViewById(R.id.noEvents);
        noEvent.setVisibility(View.INVISIBLE);

        if (eventsArray != null) {
            fetchEvents();
        } else {
            noEvent.setVisibility(View.VISIBLE);
        }

        if (eventsArray.isEmpty() == true){
            noEvent.setVisibility(View.VISIBLE);
        }

        myEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                event = eventsArray.get(position);
                loadEvent(event);
                num = position;
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            selectedUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedUri);
                photo_event.setImageDrawable(new BitmapDrawable(bitmap));
                savePhoto();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {super.onViewCreated(view, savedInstanceState);}

    //load event
    private void loadEvent(final Event event){
        builder = new AlertDialog.Builder(getContext());
        view = getLayoutInflater().inflate(R.layout.dialog_my_event,null);

        //buttons
        deleteEvent = view.findViewById(R.id.deleteEvent);
        editEvent = view.findViewById(R.id.editEvent);

        photo_event = view.findViewById(R.id.PhotoEvent);
        if (event.getUrlPhoto() != null){
            Picasso.get()
                    .load(event.getUrlPhoto())
                    .into(photo_event);
        }

        dateEvent = view.findViewById(R.id.DateEvent);
        nameEvent = view.findViewById(R.id.NameEvent);
        hourEvent = view.findViewById(R.id.HourEvent);
        ticketEvent = view.findViewById(R.id.TicketEvent);
        descriptionEvent = view.findViewById(R.id.DescriptionEvent);

        nameEvent.setText(event.getNameEvent());
        dateEvent.setText(event.getDateEvent());
        hourEvent.setText(event.getHourEvent());
        ticketEvent.setText(event.getTicketEvent());
        descriptionEvent.setText(event.getDescriptionEvent());

        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

        deleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deleteEvents();
               Intent it = new Intent(getActivity(), MainActivity.class);
               startActivity(it);
               dialog.hide();
               showSnackbar(R.color.colorGreen,R.string.event_deleted);
            }
        });

        editEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               editEvent(event);
            }
        });
    }

    //create event
    private void editEvent(final Event event){
        builder = new AlertDialog.Builder(getContext());
        view = getLayoutInflater().inflate(R.layout.dialog_edit_event,null);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        buttonSaveEvent = view.findViewById(R.id.buttonSaveEvent);
        nameEvent = view.findViewById(R.id.editNameEvent);
        descriptionEvent = view.findViewById(R.id.editDescriptionEvent);

        //carrega a foto do evento caso exista
        photo_event = view.findViewById(R.id.newPhotoEvent);
        if (event.getUrlPhoto() != null){
            Picasso.get()
                    .load(event.getUrlPhoto())
                    .into(photo_event);
        }

        editPhotoEvent = view.findViewById(R.id.buttonEditPhoto);
        editPhotoEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });

        dateEvent = view.findViewById(R.id.editDateEvent);
        SimpleMaskFormatter smfd = new SimpleMaskFormatter("NN/NN/NNNN");
        MaskTextWatcher mtwd = new MaskTextWatcher(dateEvent, smfd);
        dateEvent.addTextChangedListener(mtwd);

        hourEvent = view.findViewById(R.id.editHourEvent);
        SimpleMaskFormatter smfh = new SimpleMaskFormatter("NN:NN");
        MaskTextWatcher mtwh = new MaskTextWatcher(hourEvent, smfh);
        hourEvent.addTextChangedListener(mtwh);

        ticketEvent = view.findViewById(R.id.editTicketEvent);
        ticketEvent.addTextChangedListener(new MascaraMonetaria(ticketEvent));

        nameEvent.setText(event.getNameEvent());
        dateEvent.setText(event.getDateEvent());
        hourEvent.setText(event.getHourEvent());
        ticketEvent.setText(event.getTicketEvent());
        descriptionEvent.setText(event.getDescriptionEvent());


        buttonSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEmpty()) {
                        if (checkDateFormat(dateEvent.getText().toString())) {
                            if (checkHourFormat(hourEvent.getText().toString())) {
                                if (checkNameFormat(nameEvent.getText().toString())) {
                                    if (checkMoneySize(ticketEvent.getText().toString())){
                                        updateDataEvents();
                                    }else
                                        ticketEvent.setError("Valor Invalido");
                                } else
                                    nameEvent.setError("Nome Invalido");
                            } else
                                hourEvent.setError("Hora Invalida");
                        } else
                            dateEvent.setError("Data Invalida");
                } else {
                    Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }

    //mostra uma mensagem
    private void showSnackbar(int color, int text) {
        Snackbar snackbar = Snackbar.make(getView(), text, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(getContext(), color));
        snackbar.show();
    }


    private void updateDataEvents(){
        event.setNameEvent(nameEvent.getText().toString());
        event.setTicketEvent(ticketEvent.getText().toString());
        event.setDescriptionEvent(descriptionEvent.getText().toString());
        event.setHourEvent(hourEvent.getText().toString());
        event.setDateEvent(dateEvent.getText().toString());
        event.setUrlPhoto(photo);


        db.collection("events").document(event.getIdEvent())
                .update("nameEvent",event.getNameEvent(),
                        "hourEvent",event.getHourEvent(),
                        "dateEvent", event.getDateEvent(),
                        "ticketEvent",event.getTicketEvent(),
                        "descriptionEvent", event.getDescriptionEvent(),
                        "urlPhoto", event.getUrlPhoto())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadEvent(event);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "nao ocorreu o update", Toast.LENGTH_SHORT).show();
            }
        });
        Intent it = new Intent(getActivity(),MainActivity.class);
        startActivity(it);
    }


    //select photo for event
    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }


    private void savePhoto() {
        String fileName = UUID.randomUUID().toString();
        progressBar.setVisibility(View.VISIBLE);
        validPhoto = false;
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/events/" + fileName);
        ref.putFile(selectedUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                photo = uri.toString();
                                validPhoto = true;
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                });
    }

    //tras o eventos do banco e coloca todos no mapa
    private void fetchEvents() {
        db.collection("events").whereEqualTo("idUser",firebaseUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot doc : docs) {
                            Event event = doc.toObject(Event.class);

                            if (event != null) {
                                    eventsArray.add(event);
                                    noEvent.setVisibility(View.GONE);
                            }
                            myEventsListView.setAdapter(new MyEventsAdapter(getContext(), eventsArray));
                        }
                    }
                });
    }

    //deleta os eventos
    private void deleteEvents() {
        db.collection("events").document(event.getIdEvent())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.hide();
                        showSnackbar(R.color.colorGreen, R.string.event_deleted);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public boolean isUserAuth() {
        return firebaseUser != null;
    }

    public boolean checkNameFormat(String nameEvent){
        if (nameEvent.length() > 25){
            return false;
        }
        else return true;
    }

    public boolean checkMoneySize(String moneyEvent){
        if (moneyEvent.length() < 9){
            return true;
        }
        else return false;
    }

    public boolean checkDateFormat(String dateEvent) {
        if(dateEvent.length() > 9) {
            Date date = null;
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            try {
                format.setLenient(false);
                date = format.parse(dateEvent);
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
        else return false;
    }

    public boolean checkHourFormat(String dateHour){
        if (dateHour.length() > 4){
            String time[] = dateHour.split(":");
            int h = Integer.parseInt(time[0]);
            int m = Integer.parseInt(time[1]);
            if ((h < 0 || h > 23) || (m < 0 || m > 59)){
                return false;
            }
            else {
                return true;
            }
        }else
            return false;
    }

    private boolean isEmpty() {
        if (nameEvent.getText().toString().isEmpty() ||
                dateEvent.getText().toString().isEmpty() ||
                hourEvent.getText().toString().isEmpty() ||
                ticketEvent.getText().toString().isEmpty() ||
                descriptionEvent.getText().toString().isEmpty()) {
            return true;

        } else return false;
    }

    private class MascaraMonetaria implements TextWatcher {

        final EditText campo;

        public MascaraMonetaria(EditText campo) {
            super();
            this.campo = campo;
        }

        private boolean isUpdating = false;

        private NumberFormat nf = NumberFormat.getCurrencyInstance();

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int after) {
            {
                if (isUpdating) {
                    isUpdating = false;
                    return;
                }

                isUpdating = true;
                String str = s.toString();
                // Verifica se já existe a máscara no texto.
                boolean hasMask = ((str.indexOf("R$") > -1 || str.indexOf("$") > -1) &&
                        (str.indexOf(".") > -1 || str.indexOf(",") > -1));
                // Verificamos se existe máscara
                if (hasMask) {
                    // Retiramos a máscara.
                    str = str.replaceAll("[R$]", "").replaceAll("[,]", "")
                            .replaceAll("[.]", "");
                }
                try {
                    // Transformamos o número que está escrito no EditText em
                    // monetário.
                    str = nf.format(Double.parseDouble(str) / 100);
                    campo.setText(str);
                    campo.setSelection(campo.getText().length());
                } catch (NumberFormatException e) {
                    s = "";
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
