package com.example.eventdayfinal.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventdayfinal.Fragments.ListFragment;
import com.example.eventdayfinal.Fragments.MapsFragment;
import com.example.eventdayfinal.Fragments.NewEventFragment;
import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.Models.Register;
import com.example.eventdayfinal.Models.User;
import com.example.eventdayfinal.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import static com.example.eventdayfinal.Utils.FirebaseUtils.currentUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

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


    //Variables
    private User userData;


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

        //firebase comands
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = firebaseDatabase.getReference();


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
        if(authStateListener != null) {
            firebaseAuth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }


    //startActivityForResult Callback
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //This needs to be declared in order to use this same method in fragment
        // Isso precisa ser declarado para usar este mesmo método no fragmento
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        fragment.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 777) {
            if (resultCode == RESULT_OK) {
                final com.google.android.gms.location.places.Place locationData = PlacePicker.getPlace(this, data);

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//
                insertEventIntoDatabase(locationData);
//
            }
        }
    }


    //--------------------------------------DATABASE METHODS----------------------------------------

    private void insertEventIntoDatabase(com.google.android.gms.location.places.Place locationData) {

        Register registerData =  new Register(new User(currentUser.getUid(),
                currentUser.getDisplayName(),
                currentUser.getEmail()),
                getCurrentDateTime());

        Event event = new Event(
                locationData.getId(),
                locationData.getName().toString(),
                locationData.getLatLng().latitude,
                locationData.getLatLng().longitude,
                locationData.getAddress().toString(),
                registerData);

        databaseReference.child("Eventos").child(event.getId()).setValue(event);

        Toast.makeText(getApplicationContext(), "Evento salvo no banco de dados", Toast.LENGTH_LONG).show();


        closeNavigationDrawer();

        //alerta que o local foi adicionado
        showSnackbar(R.color.colorSnackbarSucess, R.string.snackbar_success_save);

      unmarkMenuItem(R.id.nav_add);
    }

    //Get the current date and time, and return them in a string format,
    //because firebase handles it better that way.
    private String getCurrentDateTime() {
        String currentDateTime;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);

        return currentDateTime = simpleDateFormat.format(calendar.getTime());
    }

    // insere usuario no banco
    private void insertNewUserIntoDatabase() {
        userData  = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail());

        databaseReference.child("Users").child(userData.getUserID()).setValue(userData);
    }
    //-------------------------------------AUTH METHODS---------------------------------------------

    public boolean isUserAuth() {
        return firebaseUser != null;
    }
    //Mensagem de alerta para que se usuario queira fazer isso precisa estar logado
    public void showLoginDialog() {
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

    //Firebase UI autenticacao
    private void auth() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
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
                                    .setIsSmartLockEnabled(false, true)
                                    .setAvailableProviders(Collections.singletonList( //this is good for performance
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
    //fecha a activity main
    private void closeNavigationDrawer() {
        drawer.closeDrawer(Gravity.START);
    }

    //abre a activity main
    private void openNavigationDrawer() {
        drawer.openDrawer(Gravity.START);
    }

    //muda as opcoes do menu lateral caso o usuario esteja logado ou nao
    private void updateMenuOptions() {
        if(!isUserAuth()) {
            menu.findItem(R.id.nav_logout).setVisible(false);
            menu.findItem(R.id.nav_change_account).setVisible(false);
        } else {
            menu.findItem(R.id.nav_logout).setVisible(true);
            menu.findItem(R.id.nav_change_account).setVisible(true);
        }
    }

    //desmarca o item do menu lateral
    private void unmarkMenuItem(int resourceId) {
        menu.findItem(resourceId).setChecked(false);
    }

    //Atualiza informaçoes do usuario atualmente
    private void updateUserUIInfo() {
        if(isUserAuth()) {
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

    //mostra uma mensagem
    private void showSnackbar(int color, int text) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.viewSnack), text, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, color));
        snackbar.show();
    }


    @Override
    //switch case para as opcoes do menu lateral
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.nav_add:
                if(isUserAuth())
                    showEventPicker();
                else showLoginDialog();
                break;

            case R.id.nav_events:
                Toast.makeText(this, "To be implemented", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(this, "To be implemented", Toast.LENGTH_SHORT).show();
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


    //Show Maps EventPicker and awaits for response in the callback onActivityResult
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
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame,
                                    new NewEventFragment()).addToBackStack(null).commit();
                    return true;
            }
            return false;
        }
    };



    //inner class that exclusively handle the download and config of the user profile image
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

}
