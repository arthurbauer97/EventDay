package com.example.eventdayfinal.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventdayfinal.Fragments.ListFragment;
import com.example.eventdayfinal.Fragments.MapsFragment;
import com.example.eventdayfinal.Fragments.MyEventsListFragment;
import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.Models.User;
import com.example.eventdayfinal.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.github.rtoshiro.util.format.MaskFormatter;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.pattern.MaskPattern;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Firebase comands
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Components
    private NavigationView navigationView;
    private View navigationViewHeader;
    private Menu menu;
    private DrawerLayout drawer;
    private TextView navHeaderUserName;
    private TextView navHeaderUserEmail;
    private ImageView navHeaderPhoto;
    private ImageButton drawerMenuButton;
    private BottomNavigationView bottomMenu;

    private boolean validPhoto = false;

    private Uri selectedUri;

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
    private Button selectLocationMap;

    private ProgressBar progressBar;

    //event object
    private Event event = new Event();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //menu lateral
        navigationView = findViewById(R.id.nav_view);

        //fica ouvindo o click no menu lateral
        navigationView.setNavigationItemSelectedListener(this);

        navigationViewHeader = navigationView.getHeaderView(0);
        menu = navigationView.getMenu();

        //activity main
        drawer = findViewById(R.id.drawer_layout);

        // dados do menu lateral
        navHeaderUserName = navigationViewHeader.findViewById(R.id.menu_user_name);
        navHeaderUserEmail = navigationViewHeader.findViewById(R.id.menu_user_email);
        navHeaderPhoto = navigationViewHeader.findViewById(R.id.menu_user_photo);

        //menu inferior
        bottomMenu = findViewById(R.id.bottom_nav);

        //fica ouvindo o click no menu inferior
        bottomMenu.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        //botao para abrir o menu lateral
        drawerMenuButton = findViewById(R.id.drawer_menu_icon);

        //fica ouvindo o click no menu inferior
        drawerMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNavigationDrawer();
            }
        });

        //carrega os dados do usuario logado
        updateUserUIInfo();

        //altera opcoes do menu conforme o usuario esta logado ou nao
        updateMenuOptions();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame,
                        new MapsFragment()).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (authStateListener != null) {
            firebaseAuth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    //startActivityForResult Callback
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Isso precisa ser declarado para usar este mesmo método no fragmento
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        fragment.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 777) {
            if (resultCode == RESULT_OK) {
                final com.google.android.gms.location.places.Place locationData = PlacePicker.getPlace(this, data);

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                insertEventIntoDatabase(locationData);
            }
        }

        if (requestCode == 123){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(this, "Usuario logado!", Toast.LENGTH_SHORT).show();
            }
        }

        //photo callback
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            selectedUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedUri);
                photo_event.setImageDrawable(new BitmapDrawable(bitmap));
                savePhoto();
            } catch (IOException e) {
            }
        }

    }

    //criar evento
    private void dialogCreateEvent() {
        builder = new AlertDialog.Builder(MainActivity.this);
        view = getLayoutInflater().inflate(R.layout.dialog_new_event, null);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        selectLocationMap = view.findViewById(R.id.buttonSelectLocationMap);

        criador_evento = view.findViewById(R.id.nameCreator);
        photo_event = view.findViewById(R.id.newPhotoEvent);
        newButtonAddPhoto = view.findViewById(R.id.newButtonAddPhoto);
        nameEvent = view.findViewById(R.id.newNameEvent);
        descriptionEvent = view.findViewById(R.id.newDescriptionEvent);
        criador_evento.setText(firebaseUser.getDisplayName());


        dateEvent = view.findViewById(R.id.newDateEvent);
        SimpleMaskFormatter smfd = new SimpleMaskFormatter("NN/NN/NNNN");
        MaskTextWatcher mtwd = new MaskTextWatcher(dateEvent, smfd);
        dateEvent.addTextChangedListener(mtwd);

        hourEvent = view.findViewById(R.id.newHourEvent);
        SimpleMaskFormatter smfh = new SimpleMaskFormatter("NN:NN");
        MaskTextWatcher mtwh = new MaskTextWatcher(hourEvent, smfh);
        hourEvent.addTextChangedListener(mtwh);

        ticketEvent = view.findViewById(R.id.newTicketEvent);
        ticketEvent.addTextChangedListener(new MascaraMonetaria(ticketEvent));

        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        newButtonAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });

        selectLocationMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserAuth()) {
                    if (!isEmpty()) {
                            if (checkDateFormat(dateEvent.getText().toString())) {
                                if (checkHourFormat(hourEvent.getText().toString())) {
                                    if (checkNameFormat(nameEvent.getText().toString())) {
                                        if (checkMoneySize(ticketEvent.getText().toString())) {
                                            showEventPicker();
                                        }else
                                            ticketEvent.setError("Valor Invalido");
                                    } else
                                        nameEvent.setError("Nome Invalido");
                                } else
                                    hourEvent.setError("Hora Invalida");
                            } else
                                dateEvent.setError("Data Invalida");
                    } else {
                        Toast.makeText(MainActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    }
                } else showLoginDialogCreateEvent();
            }
        });
        dialog.show();
    }

    public boolean checkNameFormat(String nameEvent){
        if (nameEvent.length() > 25){
            return false;
        }
        else return true;
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


    public boolean checkMoneySize(String moneyEvent){
        if (moneyEvent.length() < 9){
            return true;
        }
        else return false;
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


    //mostra uma mensagem
    private void showSnackbar(int color, int text) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.viewSnack), text, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, color));
        snackbar.show();
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
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/events/" + fileName);
        ref.putFile(selectedUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                event.setUrlPhoto(uri.toString());
                                progressBar.setVisibility(View.GONE);

                            }
                        });
                    }
                });

    }
    //--------------------------------------DATABASE METHODS----------------------------------------

    //insere uma event no firebase
    public void insertEventIntoDatabase(com.google.android.gms.location.places.Place locationData) {

        event.setAdress(locationData.getAddress().toString());
        event.setLatitude(locationData.getLatLng().latitude);
        event.setLogintude(locationData.getLatLng().longitude);

        event.setNameEvent(nameEvent.getText().toString());
        event.setTicketEvent(ticketEvent.getText().toString());
        event.setDescriptionEvent(descriptionEvent.getText().toString());
        event.setHourEvent(hourEvent.getText().toString());
        event.setDateEvent(dateEvent.getText().toString());

        DocumentReference ref = db.collection("events").document();
        event.setIdEvent(ref.getId());
        event.setIdUser(firebaseAuth.getCurrentUser().getUid());
        ref.
                set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showSnackbar(R.color.colorGreen, R.string.event_sucess);
                        dialog.hide();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", event.getIdEvent());
                    }
                });
    }

    // insere usuario no firebase
    private void insertNewUserIntoDatabase() {
        final User userData = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail());

        DocumentReference ref = db.collection("users").document(firebaseAuth.getCurrentUser().getUid());
        ref.
                set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("Success", userData.getUserID());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", userData.getUserID());
                    }
                });
    }
    //-------------------------------------AUTH METHODS---------------------------------------------

    public boolean isUserAuth() {
        return firebaseUser != null;
    }

    //Mensagem de alerta para que se usuario queira fazer isso precisa estar logado
    public void showLoginDialogCreateEvent() {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.do_login_alert_message)
                .setPositiveButton(R.string.do_login_alert_option_1, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        auth();
                    }
                })
                .setNegativeButton(R.string.do_login_alert_option_2, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false);
        final AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    public void showLoginDialogMyEvents() {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.do_login_my_events)
                .setPositiveButton(R.string.do_login_alert_option_1, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        auth();
                    }
                })
                .setNegativeButton(R.string.do_login_alert_option_2, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false);
        final AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    //Firebase UI autenticacao
    private void auth() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //se tiver usuario logado, mostra a opcao para adicionar Evento
                    firebaseUser = user;
                    insertNewUserIntoDatabase();
                    updateUserUIInfo();
                    updateMenuOptions();
                } else {
                    //Else, mostra as opcoes do gmail para login
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Collections.singletonList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            123);
                }
            }
        };
        //Isso sera chamado apos a primeira verificaçao do usuario autenticado, para aplicar as opçoes
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void logout() {
        //desloga
        firebaseUser = null;
        firebaseAuth.signOut();
        AuthUI.getInstance().signOut(MainActivity.this);

        //atualiza
        updateUserUIInfo();
        updateMenuOptions();

        //avisa que foi deslogado
        Toast.makeText(this, "Logoff realizado!", Toast.LENGTH_SHORT).show();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void changeAccount() {
        firebaseUser = null;
        firebaseAuth.signOut();
        AuthUI.getInstance().signOut(MainActivity.this);
        firebaseAuth.removeAuthStateListener(authStateListener);
        Toast.makeText(this, "Trocando de conta!", Toast.LENGTH_SHORT).show();
        auth();
    }

    //------------------------------------- UI Methods----------------------------------------------
    //abre a activity main
    private void openNavigationDrawer() {
        drawer.openDrawer(Gravity.START);
    }

    //muda as opcoes do menu lateral caso o usuario esteja logado ou nao
    private void updateMenuOptions() {
        if (!isUserAuth()) {
            menu.findItem(R.id.nav_logout).setVisible(false);
            menu.findItem(R.id.nav_change_account).setVisible(false);
            menu.findItem(R.id.nav_login).setVisible(true);
        } else {
            menu.findItem(R.id.nav_logout).setVisible(true);
            menu.findItem(R.id.nav_change_account).setVisible(true);
            menu.findItem(R.id.nav_login).setVisible(false);
        }
    }

    //Atualiza informaçoes do usuario atualmente
    private void updateUserUIInfo() {
        if (isUserAuth()) {
            navHeaderUserName.setText(firebaseUser.getDisplayName());
            navHeaderUserEmail.setText(firebaseUser.getEmail());
            new ProfileImageHandler(navHeaderPhoto)
                    .execute(Objects.requireNonNull(firebaseUser.getPhotoUrl()).toString());
        } else {
            //se o usuario estiver desconectado, limpe as informaçoes anteriores e defina as abaixo
            navHeaderUserName.setText(R.string.nav_header_title);
            navHeaderUserEmail.setText(R.string.nav_header_subtitle);
            navHeaderPhoto.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    @Override
    //switch case para as opcoes do menu lateral
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_new_event:
                if (isUserAuth()) {
                    dialogCreateEvent();
                } else showLoginDialogCreateEvent();
                break;

            case R.id.nav_my_events:
                if (isUserAuth()) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame,
                                    new MyEventsListFragment()).addToBackStack(null).commit();
                } else showLoginDialogMyEvents();
                break;

            case R.id.nav_login:
                auth();
                break;

            case R.id.nav_logout:
                logout();
                break;

            case R.id.nav_change_account:
                changeAccount();
                break;
        }
        return true;
    }

    //Mostra o mapas para selecionar o local e aguarda resposta no retorno de chamada onActivityResult
    public void showEventPicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), 777);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    // menu inferior itens selecionados
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_nav_maps:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame,
                                    new MapsFragment()).addToBackStack(null).commit();
                    return true;
                case R.id.bottom_nav_list:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame,
                                    new ListFragment()).addToBackStack(null).commit();
                    return true;
                case R.id.bottom_nav_new_event:
                    if (isUserAuth()) {
                        dialogCreateEvent();
                    } else showLoginDialogCreateEvent();
                    break;
            }
            return false;
        }
    };


    //classe interna que manipula exclusivamente o download e a configuração da imagem do perfil do usuário
    @SuppressLint("StaticFieldLeak")
    private class ProfileImageHandler extends AsyncTask<String, Void, Bitmap> {

        ImageView imageView;

        private ProfileImageHandler(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("ProfileImage error :: ", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    //mascara do dinheiro
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
