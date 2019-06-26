package com.example.eventdayfinal.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.eventbus.Subscribe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.eventdayfinal.Adapters.InfoAdapter;
import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.R;
import com.example.eventdayfinal.Utils.CustomSnackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.ui.email.CheckEmailFragment.TAG;

public class MapsFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        Serializable,
        GoogleMap.OnInfoWindowLongClickListener {

    //Constants
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 777;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 776;
    private static final int SEND_LOCATION_REQUEST_CODE = 666;
    private static final float DEFAULT_ZOOM = 16f;
    private static final int MOVE_CAMERA = 1;
    private static final long DEFAULT_DELAY_TIME = 1400;


    //Maps
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    public static Event currentLocation = new Event();
    private Marker lastMarker;


    //Firebase
    private DatabaseReference databaseUsersReference;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    //Components
    private EditText searchEditText;
    private View view;
    private CoordinatorLayout snackbarPlacer;
    private CustomSnackbar gettingConnSnackbar;
    private CustomSnackbar locationObtainedSnackbar;


    //Variables
    public static Map<String, Marker> mapMarkers = new HashMap<>();
    private String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
    private boolean locationObtained;
    private boolean locationPermissionsGranted;
    private Event lastSearchedPlace = new Event();
    private long lastTimeClicked;
    private Event eventFromBundle;

    final ArrayList<Event> arrayListEvents = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);

        getLocationPermission();

        snackbarPlacer = getActivity().findViewById(R.id.viewSnack);

        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseUsersReference = FirebaseDatabase.getInstance().getReference("Users");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapView mapView = view.findViewById(R.id.map_view);

        gettingConnSnackbar = new CustomSnackbar(getActivity());
        locationObtainedSnackbar = new CustomSnackbar(getActivity());

        lastTimeClicked = 0;

        searchEditText = getView().findViewById(R.id.searchLocation_EditText);
        searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSuggestions();
            }
        });

        ImageButton getLocationButton = getView().findViewById(R.id.getLocation_Button);
        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastTimeClicked < 1000) {
                    return;
                }
                lastTimeClicked = SystemClock.elapsedRealtime();
                mountLocationRequest();
                sendLocationRequest();
                getCurrentLocation();
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                getCurrentLocation();
                locationObtainedSnackbar.make(R.string.snackbar_maps_sucess, Snackbar.LENGTH_LONG,
                        R.color.colorSnackbarSucess).show();
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
        };

        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    //Opens PlaceAutocomplete fragment from EdiText
    private void getSuggestions() {
        try {
            if (!searchEditText.getText().toString().isEmpty()) searchEditText.setText("");
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setBoundsBias(googleMap.getProjection().getVisibleRegion().latLngBounds)
                            .build(getActivity());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
        } catch (GooglePlayServicesNotAvailableException e) {
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PLACE_AUTOCOMPLETE_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
                    com.google.android.gms.location.places.Place place = PlaceAutocomplete.getPlace(getContext(), data);
                    registerLastSearchData(place);
                }
                break;
            }

            case SEND_LOCATION_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
                    if (!locationObtained) {
                        gettingConnSnackbar.make(R.string.snackbar_maps_getting_gps,
                                Snackbar.LENGTH_INDEFINITE,
                                R.color.colorSnackbarInfo)
                                .show();
                    }
                    getCurrentLocation();
                } else {
                    showCustomActiveGpsSnackbar();
                }
            }
        }
    }

    //Callback for when map is ready
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        fetchEvents();

        // personaliza o mapa com o tom preto de fundo
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.mapstyle));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        getSelectedLocationOnMaps();

        Handler alertDelay = new Handler();
        alertDelay.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!locationObtained) {
                    showCustomActiveGpsSnackbar();
                }
            }
        }, DEFAULT_DELAY_TIME);

        map.setOnInfoWindowClickListener(this);
        map.setInfoWindowAdapter(new InfoAdapter(getContext()));
    }

    //--------------------------------LOCATION AND MAP DATA METHODS---------------------------------

    //adiciona a marka no mapa, com todos os eventos que foram adicionado
    private Marker addMarkerIntoMaps(double latitude, double longitude,
                                     String title,String date,String hour, String description) {
        LatLng latLng1 = new LatLng(latitude, longitude);

        Event event = new Event();
        event.setLatitude(latitude);
        event.setLogintude(longitude);
        event.setNameEvent(title);
        event.setDateEvent(date);
        event.setHourEvent(hour);
        event.setDescriptionEvent(description);

        MarkerOptions options = new MarkerOptions()
                .position(latLng1)
                .title(title)
                .snippet(hour + "\n\n" + date)
                .draggable(false)
                .icon(BitmapDescriptorFactory
                        .fromBitmap(getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_maps_marker)));
        return googleMap.addMarker(options);
    }

    //tras o eventos do banco e coloca todos no mapa
    private void fetchEvents() {
        db.collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot doc:docs) {
                            Event event = doc.toObject(Event.class);
                            if(validDate(event.getDateEvent())) {
                                arrayListEvents.add(event);
                                mapMarkers.put(event.getIdEvent(),
                                        addMarkerIntoMaps(event.getLatitude(),
                                                event.getLogintude(),
                                                event.getNameEvent(),
                                                event.getDateEvent(),
                                                event.getHourEvent(),
                                                event.getDescriptionEvent()));
                            }
                        }
                    }
                });
    }

    private boolean validDate(String dateEvent){
        Date date = new Date();
        String dateDay = getFormattedDate(date);
        String dateDaySplit[] = dateDay.split("/");
        String dateEventSplit[] = dateEvent.split("/");

        long date1 = Long.parseLong(dateDaySplit[2] + dateDaySplit[1] + dateDaySplit[0]);
        long date2 = Long.parseLong(dateEventSplit[2] + dateEventSplit[1] + dateEventSplit[0]);

        if(date2 < date1) {
            return false;
        } else {
            return true;
        }
    }

    private String getFormattedDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return simpleDateFormat.format(date);
    }

    //Moves the camera to the location, accordingly with the option selected (animate or just move)
    private void moveCamera(double latitude, double longitude, float zoom, int option) {
        LatLng latLng1 = new LatLng(latitude, longitude);
        if (option == 0) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1, zoom));
        else googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng1, zoom));
    }

    // mensagem que aparece ao clicar na informacao de algum evento
    @Override
    public void onInfoWindowClick(final Marker marker) {
    }

    // mensagem que aparece ao segurar na informacao de algum evento
    @Override
    public void onInfoWindowLongClick(final Marker marker) {
    }

    // pega localizacao do usuario
    private void getCurrentLocation() {
        if (locationPermissionsGranted) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getContext());
            try {
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    currentLocation.setLatitude(location.getLatitude());
                                    currentLocation.setLogintude(location.getLongitude());
                                    locationObtained = true;
                                    googleMap.setMyLocationEnabled(true);
                                    googleMap.getUiSettings().setRotateGesturesEnabled(false);
                                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                                    moveCamera(location.getLatitude(), location.getLongitude(), DEFAULT_ZOOM, MOVE_CAMERA);

                                    if (currentUser != null) {
                                        updateUserLastLocation(currentUser);
                                    }

                                } else {
                                    locationObtained = false;
                                    fusedLocationProviderClient
                                            .requestLocationUpdates(locationRequest, locationCallback, null);
                                }
                            }
                        });

            } catch (SecurityException e) {
            }
        }
    }

    //atualiza a ultima localizacao do usuario
    private void updateUserLastLocation(FirebaseUser firebaseUser) {
        databaseUsersReference
                .child(firebaseUser.getUid())
                .child("LastLocation")
                .setValue(currentLocation);
    }

    //pega o local selecionado no mapa
    private void getSelectedLocationOnMaps() {
        try {
            locationObtained = true;
            eventFromBundle = (Event) getArguments().getSerializable("event");
            if (eventFromBundle != null) {
                moveCamera(eventFromBundle.getLatitude(), eventFromBundle.getLogintude(), DEFAULT_ZOOM, MOVE_CAMERA);
                eventFromBundle = null;
            } else {
                getCurrentLocation();
            }
        } catch (NullPointerException e) {
            getCurrentLocation();
        }
    }

    private void registerLastSearchData(com.google.android.gms.location.places.Place place) {
        if (lastMarker != null) {
            lastMarker.remove();
            lastMarker = null;
        }

        lastSearchedPlace.setIdEvent(place.getId());
        lastSearchedPlace.setNameEvent(place.getName().toString());
        lastSearchedPlace.setLatitude(place.getLatLng().latitude);
        lastSearchedPlace.setLogintude(place.getLatLng().longitude);

        if (mapMarkers.get(place.getId()) == null) {
            MarkerOptions options = new MarkerOptions()
                    .position(place.getLatLng())
                    .draggable(false)
                    .snippet("Clique aqui para adicionar o local")
                    .title(place.getName().toString());

            lastMarker = googleMap.addMarker(options);
        }

        moveCamera(place.getLatLng().latitude, place.getLatLng()
                .longitude, DEFAULT_ZOOM, 0);

        searchEditText.setText(place.getName());
    }

    //----------------------------------PERMISSIONS AND REQUESTS------------------------------------

    // pede ao usuario a permissao para usar sua localizacao
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                        COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionsGranted = true;
        } else {
            requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    //Permission answer callback
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionsGranted = true;
                }
            }
        }
    }

    //Checks if GPS is activated, if not, asks the user to turn it on
    protected void sendLocationRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(getActivity());

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            }
        });
        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(),
                                SEND_LOCATION_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException sendEx) {
                    }
                }
            }
        });
    }

    private void mountLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5500);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //-----------------------------------UTILS METHODS----------------------------------------------

    // Comparar dois DateTimes, um do firebase registrado, outro do android system,
    // para ver se eles combinam escolha intervalo de tempo, se o fizerem, retorne verdadeiro, senão falso.
    public Bitmap getBitmapFromVectorDrawable(Activity context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        if (drawable != null) drawable = (DrawableCompat.wrap(drawable)).mutate();

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void showCustomActiveGpsSnackbar() {
        Snackbar noGpsSnackbar = Snackbar.make(snackbarPlacer, R.string.snackbar_maps_no_gps, Snackbar.LENGTH_INDEFINITE);
        noGpsSnackbar.getView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorSnackbarFail));
        noGpsSnackbar.setActionTextColor(Color.WHITE);
        noGpsSnackbar.setAction(R.string.snackbar_maps_no_gps_action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastTimeClicked < 1000) {
                    return;
                }
                lastTimeClicked = SystemClock.elapsedRealtime();
                mountLocationRequest();
                sendLocationRequest();
                getCurrentLocation();
            }
        });
        noGpsSnackbar.show();
    }
}