package com.example.eventdayfinal.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ListView;
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

import java.text.NumberFormat;
import java.util.ArrayList;

import java.util.List;


public class MyEventsListFragment extends Fragment{

    private ListView myEventsListView;
    private TextView noResultsTextView;
    private ArrayList<Event> eventsArray = new ArrayList<>();

    private Event event = new Event();


    //create event dialog
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private View view;
    TextView criador_evento;
    private EditText nameEvent;
    private EditText dateEvent;
    private EditText ticketEvent;
    private EditText descriptionEvent;
    private EditText hourEvent;
    private ImageView newButtonAddPhoto;
    private ImageView photo_event;
    private Button editEvents;
    private Button editDataEvents;
    private ImageView editEvent;
    private ImageView deleteEvent;
    private int idDelete;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    private int num;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_my_events, container, false);

        myEventsListView = view.findViewById(R.id.events_mylistview);
        noResultsTextView = view.findViewById(R.id.no_result_textview);

        if (eventsArray != null) {
            fetchEvents();
        } else {
            noResultsTextView.setVisibility(View.VISIBLE);
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
    public void onViewCreated(View view, Bundle savedInstanceState) {super.onViewCreated(view, savedInstanceState);}

    //load event
    private void loadEvent(final Event event){
        builder = new AlertDialog.Builder(getContext());
        view = getLayoutInflater().inflate(R.layout.dialog_my_event,null);

        //buttons
        deleteEvent = view.findViewById(R.id.deleteEvent);
        editEvent = view.findViewById(R.id.editEvent);


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

        editDataEvents = view.findViewById(R.id.buttonEditDataEvent);
        nameEvent = view.findViewById(R.id.editNameEvent);
        descriptionEvent = view.findViewById(R.id.editDescriptionEvent);

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


        editDataEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDataEvents();
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

        db.collection("events").document(event.getIdEvent())
                .update("nameEvent",event.getNameEvent(),
                        "hourEvent",event.getHourEvent(),
                        "dateEvent", event.getDateEvent(),
                        "ticketEvent",event.getTicketEvent(),
                        "descriptionEvent", event.getDescriptionEvent())
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
                                    noResultsTextView.setVisibility(View.GONE);
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
                        Toast.makeText(getContext(), "evento deletado com sucesso", Toast.LENGTH_SHORT).show();
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

    private boolean isEmpty() {
        if (nameEvent.getText().toString().isEmpty() ||
                dateEvent.getText().toString().isEmpty() ||
                hourEvent.getText().toString().isEmpty() ||
                ticketEvent.getText().toString().isEmpty() ||
                descriptionEvent.getText().toString().isEmpty()) {
            return true;

        }else return false;
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
