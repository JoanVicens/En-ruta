package joan.demo_rutes_bicicas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowLongClickListener, FragmentListener, NavigationView.OnNavigationItemSelectedListener, FavoridaListener {

    private static final String TAG = "MapsActivity";

    public static final String BASES = "bases";
    public static final String INDEX_BASE = "INDEX_BASE";
    public static final String NOM_BASE = "NOM_BASE";
    public static final String OCUPACIO_BASE = "OCUPACIO_BASE";
    public static final String PERILL_N_BASES = "PERILL NUMERO BASES";

    public static final String EN_BICI = "bicycle";
    public static final String A_PEU = "pedestrian";

    public static final String PUNT_PERSONALITZAT = " PUNT PERSONALITZAT ";
    public static final String PREFERENCIES = "PREFERENCIES";
    public static final String OCUPACIO_SELECCIONADA = "OCUPACIO SELECCIONADA";
    private static final String FAVORITA = "FAVORITA";

    private static final int SEEKBAR_OPT = 1;

    // Control 2 accions 1 boto
    boolean ocultar = false;


    // Mapa
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private GeoApiContext mGeoApiContext;

    private Camara camara;

    private Marker puntTemp;

    private Marker puntA;
    private Marker puntB;

    private ArrayList<Polyline> rutes;
    private ArrayList<Polyline> carrilsBici;

    private ArrayList<Marker> llistaMarkers;
    private ArrayList<Marker> markersUsuari;

    private int zoomDespresAnimacio = Camara.ZOOM_INICIAL;

    // Setup fragments info base
    private FragmentManager fragmentManager1;
    private FragmentTransaction fragmentTransaction1;
    ArrayList<BaseFragment> dBase = new ArrayList<>();

    // Bases
    private BitmapDescriptor iconBike;
    private ArrayList<Base> bases;
    private boolean baseseVisibles;

    // Estacions de tram
    private ArrayList<Marker> estacionsTram;

    // GeoLocalització
    private Marker markerPosicio;

    // Controls view
    private FloatingActionButton gpsPosition;
    private FloatingActionButton btnErarse;
    private FloatingActionButton btnTotesBases;
    private TextView tvInici;
    private TextView tvDesti;
    private Button btnToggle;

    private Thread consegirInfo;
    private int PERMIS_GPS = 10;
    private String POSICIO_ACTUAL = "La teva posicio";
    private LocationManager locationManager;

    // Menu
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private Menu menu;

    // Opcions rutes
    Preferencies mPreferencies;
    private LlistatBases fragment_llista;
    private BaseFragment baseFragment;
    private BitmapDescriptor icon_base_seleccionada;
    private Thread carregarCarrils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Obtenció dels carrils bici
        this.mPreferencies = new Preferencies(getApplicationContext());

        carrilsBici = new ArrayList<>();
        if(mPreferencies.isMostrarCarrilsBici()) {
            loadCarrils();
        }

        // Obtenció del JSON de les bases
        consegirInfo = new Thread(new BaseProvider());
        consegirInfo.start();

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.googleDirectionsApiKey)).build();
        }

        icon_base_seleccionada = createCustomIconFromDrawable(R.drawable.marker_base_negatiu);

        puntA = null;
        puntB = null;

        // Menu toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_clase);

        drawer.addDrawerListener(toggle);
        toggle.syncState();


        gpsPosition = findViewById(R.id.fab_activityMpas_gps);
        btnErarse = findViewById(R.id.btn_activityMaps_erarse);
        btnTotesBases = findViewById(R.id.fab_totes_bases);
        btnToggle = findViewById(R.id.button_toggle_nav);

        tvInici = findViewById(R.id.tv_activtyMaps_origen);
        tvDesti = findViewById(R.id.tv_activtyMaps_desti);

        // Navigation drawer
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();

        menu.findItem(R.id.switch_nav_satelit).setActionView(new Switch(this));
        menu.findItem(R.id.switch_nav_bases).setActionView(new Switch(this));
        menu.findItem(R.id.switch_nav_bici).setActionView(new Switch(this));
        menu.findItem(R.id.switch_nav_tram).setActionView(new Switch(this));
        menu.findItem(R.id.switch_nav_carrils).setActionView(new Switch(this));

        // Init grup de markers y punts
        llistaMarkers = new ArrayList<>();
        markersUsuari = new ArrayList<>();
        estacionsTram = new ArrayList<>();
        rutes = new ArrayList<>();
    }


    @SuppressLint("NewApi")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("SharedPreferences", mPreferencies.getTipoMapa()+"");
        mMap = googleMap;
        mMap.setMapType(mPreferencies.getTipoMapa());
        mUiSettings = mMap.getUiSettings();
        camara = new Camara(mMap);
        camara.inicializar();

        tvInici.setText("");
        tvDesti.setText("");

        btnErarse.setBackgroundDrawable(getResources().getDrawable(R.drawable.back));

        setUpMapa(mMap);


        // Listeners

        CompoundButton.OnCheckedChangeListener switchListener = (compoundButton, isChecked) -> {

            switch (compoundButton.getId()) {
                case R.id.switch_nav_satelit:
                    //Cambiar tipus de mapa
                    Log.i("NavigationListeners", "nav satelit");
                    if(isChecked) {
                        mPreferencies.setTipoMapa(GoogleMap.MAP_TYPE_HYBRID, true);
                    } else {
                        mPreferencies.setTipoMapa(GoogleMap.MAP_TYPE_NORMAL, true);
                    }

                    break;
                case R.id.switch_nav_bases:
                    //Mostrar/ocultar bases
                    mPreferencies.setMostrarBases(isChecked, true);

                    baseseVisibles = isChecked;
                    if(isChecked && dBase.isEmpty()) {
                        afegirMarkers();
                        dBase = crearFragments();
                    } else {
                        for(Marker marker: llistaMarkers) {
                            marker.setVisible(isChecked);
                        }
                    }
                    break;
                case R.id.switch_nav_bici:
                    mPreferencies.setBiciPropia(isChecked, true);
                    break;
                case R.id.switch_nav_tram:
                    mPreferencies.setMostrarEstacionsTram(isChecked, true);
                    if(isChecked && estacionsTram.isEmpty()) {
                        inicialitzarEstacionsTram();

                    } else {
                        for(Marker marker: estacionsTram) {
                            marker.setVisible(isChecked);
                        }
                    }

                    break;
                case R.id.switch_nav_carrils:

                    if(isChecked && carrilsBici.isEmpty()) {
                        loadCarrils();
                        mostrarCarrils();
                    } else {
                        for(Polyline polyline: carrilsBici) {
                            polyline.setVisible(isChecked);
                        }
                    }
                    mPreferencies.setMostrarCarrilsBici(isChecked, true);

            }

            actualitzarOpcions();
        };

        // Menu items
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_satelit).getActionView()).setChecked(mPreferencies.getTipoMapa() == View.INVISIBLE);
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_bases).getActionView()).setChecked(mPreferencies.MostrarBases());
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_bici).getActionView()).setChecked(mPreferencies.isBiciPropia());
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_tram).getActionView()).setChecked(mPreferencies.isMostrarEstacionsTram());
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_carrils).getActionView()).setChecked(mPreferencies.isMostrarCarrilsBici());
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_satelit).getActionView()).setOnCheckedChangeListener(switchListener);
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_bases).getActionView()).setOnCheckedChangeListener(switchListener);
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_bici).getActionView()).setOnCheckedChangeListener(switchListener);
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_tram).getActionView()).setOnCheckedChangeListener(switchListener);
        ((Switch) navigationView.getMenu().findItem(R.id.switch_nav_carrils).getActionView()).setOnCheckedChangeListener(switchListener);

        // Mapa
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
        mMap.setOnMapLongClickListener(pos -> {
            if(puntA != null && puntB != null) {
                markersUsuari.clear();
                puntA.remove();
                puntB.remove();
                puntA = null;
                puntB = null;
            }

            puntTemp =  mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(createCustomIconFromDrawable(R.drawable.marker_current_position))
                    .title(PUNT_PERSONALITZAT));

            markersUsuari.add(puntTemp);
            addPoint();
        });
        mMap.setOnInfoWindowLongClickListener(this);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                elementClicked(view.getId());
            }

            private void elementClicked(int id) {
                switch (id) {
                    case R.id.fab_activityMpas_gps:
                        activarGeolocalitzacio();
                        break;
                    case R.id.btn_activityMaps_erarse:
                        borrarPunts();
                        Toast.makeText(getApplicationContext(), "Manten presionat per a borrar les rutes", Toast.LENGTH_SHORT);

                        break;
                    case R.id.fab_totes_bases:
                        if(ocultar) {
                            aplicarCanvis();
                            fragmentTransaction1.detach(fragment_llista);
                            MapsActivity.super.onBackPressed();
                            setVisibilitatElementsPrincipal(View.VISIBLE);
                            btnTotesBases.setImageResource(R.drawable.ic_keyboard_arrow_up);
                        }else {
                            setVisibilitatElementsPrincipal(View.INVISIBLE);
                            mostrarTotesLesBases();
                            btnTotesBases.setImageResource(R.drawable.ic_keyboard_arrow_down);
                        }
                        ocultar = !ocultar;
                        break;
                    case R.id.tv_activtyMaps_desti:
                    case R.id.tv_activtyMaps_origen:
                        swapPoints();
                        break;
                    case R.id.button_toggle_nav:
                        drawer.openDrawer(Gravity.LEFT);
                }
            }
        };

        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                elementLongClicked(view.getId());
                return true;
            }

            private void elementLongClicked(int id) {
                switch (id) {
                    case R.id.btn_activityMaps_erarse:
                        borrarTot();
                        break;
                }
            }
        };

        gpsPosition.setOnClickListener(clickListener);
        btnTotesBases.setOnClickListener(clickListener);
        btnToggle.setOnClickListener(clickListener);
        btnErarse.setOnClickListener(clickListener);
        btnErarse.setOnLongClickListener(longClickListener);

        tvDesti.setOnClickListener(clickListener);
        tvInici.setOnClickListener(clickListener);


        btnErarse.setBackgroundDrawable(getDrawable(R.drawable.back));

        if(mPreferencies.isMostrarCarrilsBici()) {
            mostrarCarrils();
        }
    }



    private void mostrarCarrils() {
        try {
            carregarCarrils.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<PolylineOptions> carrilsOptions = CarrilsBici.getPolyLinesCarrils();

        carrilsBici = new ArrayList<>();
        for(PolylineOptions polylineOptions: carrilsOptions) {
            carrilsBici.add(mMap.addPolyline(polylineOptions));
        }

    }

    private void loadCarrils() {

        if(carregarCarrils == null) {
            carregarCarrils = new Thread(new CarrilsBici(getApplicationContext()));
            carregarCarrils.start();
        } else{
            mostrarCarrils();

        }
    }

    private void inicialitzarEstacionsTram() {
        ArrayList<MarkerOptions> markersEstacionsTram = new ArrayList<>();
        List<String> linies = HelperFunctions.llegirMarkersEstacionsTram(getApplicationContext());
        for(int i = 0; i < linies.size(); i++) {
            MarkerOptions markerOptions = HelperFunctions.crearMarkerEstacionsTram(setUpMarkerAmbIcono(R.drawable.marker_estacio_tram), linies.get(i));
            markersEstacionsTram.add(markerOptions);
        }
        afegirMarkersEstacionsTram(markersEstacionsTram);
    }

    private void afegirMarkersEstacionsTram(ArrayList<MarkerOptions> markerOptionsEstacions) {
        for(MarkerOptions markerOptions: markerOptionsEstacions) {
            Marker marker = mMap.addMarker(markerOptions);

            this.estacionsTram.add(marker);
        }
    }

    private void swapPoints() {
        Marker aux = puntB;
        puntB = puntA;
        puntA = aux;

        puntTemp = null;

        actualizarTextPunts();
        if(!rutes.isEmpty()) {
            for(Polyline ruta: rutes) {
                ruta.remove();
            }
        }
        recalcularRuta();
    }

    private void setVisibilitatElementsPrincipal(int visible) {
        tvInici.setVisibility(visible);
        tvDesti.setVisibility(visible);
        btnErarse.setVisibility(visible);
        gpsPosition.setVisibility(visible);

    }

    private void mostrarTotesLesBases() {
        fragment_llista = new LlistatBases();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BASES, bases);
        bundle.putParcelable(PREFERENCIES, mPreferencies);
        fragment_llista.setArguments(bundle);

        fragmentManager1 = getActivity().getSupportFragmentManager();
        fragmentManager1.executePendingTransactions();
        fragmentTransaction1 = fragmentManager1.beginTransaction();
        fragmentTransaction1.setCustomAnimations(R.anim.slide_up_animation, R.anim.slide_down_animation, R.anim.slide_up_animation, R.anim.slide_down_animation);
        fragmentTransaction1.replace(R.id.map, fragment_llista);
        fragmentTransaction1.commit();
        fragmentTransaction1.addToBackStack(null);


    }

    private void actualitzarOpcions() {

        // Tipo de mapa
        if(mPreferencies.getTipoMapa() != mMap.getMapType()) {
            // Se comprova antes perque cambiar el tipus de mapa és costos
            mMap.setMapType(mPreferencies.getTipoMapa());
        }

        // Visibilitat bases
        boolean basesPreferencies = mPreferencies.MostrarBases();
        if(baseseVisibles && !basesPreferencies) {
            HelperFunctions.ocultarMarkers(llistaMarkers);
        } else if(!baseseVisibles && basesPreferencies) {
            HelperFunctions.mostrarMarkers(llistaMarkers);
        }

        baseseVisibles = basesPreferencies;

    }

    private void borrarTot() {
        borrarPunts();
        if(!rutes.isEmpty()) {
            for(Polyline ruta: rutes) {
                ruta.remove();
            }
        }
    }





    private void borrarPunt(Marker marker) {
        if(marker != null) {
            if(estacionsTram.contains(marker)) {
                marker.setIcon(createCustomIconFromDrawable(R.drawable.marker_estacio_tram));
            } else if(marker.getTitle().equals(POSICIO_ACTUAL)) {
                marker.setIcon(createCustomIconFromDrawable(R.drawable.marker_current_position));
            } else {
                marker.setIcon(iconBike);
            }
        }
    }

    private void borrarPunts() {
        mPreferencies.setReset(true, true);

        borrarPunt(puntA);
        borrarPunt(puntB);
        borrarPunt(markerPosicio);

        puntA = null;
        puntB = null;
        puntTemp = null;
        actualizarTextPunts();

        // Elimina tots els markers dels usuaris
        for(Marker marker: markersUsuari) {
            marker.remove();
        }
        markersUsuari = new ArrayList<>();

        zoomDespresAnimacio = Camara.ZOOM_INICIAL;
        camara.resetCamara(zoomDespresAnimacio);
    }

    private void actualizarTextPunts() {

        if(puntA != null) {
            tvInici.setText(puntA.getTitle());
        } else {
            tvInici.setText("");
        }

        if(puntB != null) {
            tvDesti.setText(puntB.getTitle());
        } else {
            tvDesti.setText("");
        }
    }

    private void activarGeolocalitzacio() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Toast.makeText(this, "GPS ACTIVAT", Toast.LENGTH_LONG);
        }
        LocationListener locationListener = null;


        if (locationListener == null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    markerPosicio.remove();
                    markerPosicio = mMap.addMarker(new MarkerOptions()
                            .icon(createCustomIconFromDrawable(R.drawable.marker_current_position))
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title(POSICIO_ACTUAL));
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {
                    Intent intentGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intentGPS);
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //configurarPermisosGPS(locationManager, locationListener);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.INTERNET
                    }, PERMIS_GPS);
                    return;
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
            }

        }


        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        markerPosicio = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                .icon(createCustomIconFromDrawable(R.drawable.marker_current_position))
                .title(POSICIO_ACTUAL));

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void configurarPermisosGPS(LocationManager locationManager, LocationListener locationListener) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET
            }, PERMIS_GPS);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);

    }

    private ArrayList<BaseFragment> crearFragments() {
        ArrayList<BaseFragment> fragments = new ArrayList<>();
        BaseFragment fragmentBase;
        Bundle infoBase;
        for (Base base:bases) {
            fragmentBase = new BaseFragment();
            infoBase = new Bundle();
            infoBase.putInt(PERILL_N_BASES, mPreferencies.getMinOcupacio());
            infoBase.putBoolean(FAVORITA, base.isPreferida());
            infoBase.putInt(INDEX_BASE, base.getId());
            infoBase.putParcelableArrayList(BASES, bases);
            infoBase.putParcelable(PREFERENCIES, mPreferencies);

            fragmentBase.setArguments(infoBase);
            fragments.add(fragmentBase);
        }

        return fragments;
    }

    private ArrayList<Base> afegirBases(JSONArray bases) {
        ArrayList<Base> punts = new ArrayList<>();

        String nom;
        int id;
        int capacitat;
        int ocupacio;
        try {
            for (int i = 0; i < bases.length(); i++) {
                JSONObject base;

                base = bases.getJSONObject(i);

                double lat = Double.parseDouble((String) base.get("latitud"));
                double lng = Double.parseDouble((String) base.get("longitud"));

                nom = base.getString("punto");
                byte[] bytes = nom.getBytes("Windows-1252");
                nom = new String(bytes, "UTF-8").toUpperCase();

                id = Integer.parseInt((String) base.get("id"));

                capacitat = (int) base.get("puestos");
                ocupacio = (int) base.get("ocupados");

                Base novaBase = new Base(new LatLng(lat, lng), id, nom, capacitat, ocupacio);
                if(HelperFunctions.esPreferida(mPreferencies, novaBase)) {
                    novaBase.setPreferida(true);
                }
                punts.add(novaBase);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return punts;
    }

    // Costumitzacio mapa
    // Skin i botons visibles
    private void setUpMapa(GoogleMap mMap) {
        // Opcions del mapa
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.retro));

        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(adapter);


        // Bases bibicas
        try {

            consegirInfo.join();

            this.bases = afegirBases(BaseProvider.getResposta());

            HelperFunctions.refrescarPreferides(bases, mPreferencies);

            baseseVisibles = mPreferencies.MostrarBases();
            afegirMarkers();

            if(mPreferencies.MostrarBases()) {
                HelperFunctions.mostrarMarkers(llistaMarkers);
            } else {
                HelperFunctions.ocultarMarkers(llistaMarkers);
            }

            dBase = crearFragments();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Estacions tram
        if(mPreferencies.isMostrarEstacionsTram()) {
            inicialitzarEstacionsTram();
        }

    }

    @SuppressLint("DefaultLocale")
    private void afegirMarkers() {
        // Afegeix marcadors per a totes les bases de bicicas
        MarkerOptions estacio = setUpMarkerAmbIcono(R.drawable.marker_base_bicicas_negatiu);

        Base baseTemp;
        String descripcio;
        for (int i = 0; i < bases.size(); i++) {

            baseTemp = bases.get(i);
            descripcio = String.format("%d/%d", baseTemp.getOcupacio(), baseTemp.getCapacitat());

            estacio.position(baseTemp.getPosicio())
                    .title(baseTemp.getNom())
                    .snippet(descripcio);
            Marker m = mMap.addMarker(estacio);
            m.setTag(baseTemp.getId());
            llistaMarkers.add(m);
        }
    }

    private MarkerOptions setUpMarkerAmbIcono(int idDrawable) {
        // Crea el icono per als marcadors de les bases
        Drawable dr = getResources().getDrawable(idDrawable);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, false);
        iconBike = BitmapDescriptorFactory.fromBitmap(bitmap);

        return new MarkerOptions().icon(iconBike);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
//        TODO : https://developer.android.com/training/implementing-navigation/temporal#java
//        TODO : https://developer.android.com/reference/android/view/Window
        camara.posicionarDalt(mMap, marker);

        if(marker.getTitle().equals(POSICIO_ACTUAL)) {
            puntTemp = marker;
            addPoint();
        } else if (estacionsTram.contains(marker)) {
            puntTemp = marker;
            addPoint();
        }

        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        puntTemp = marker;
        addPoint();
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {

        if(marker.getTitle().equals(PUNT_PERSONALITZAT) || marker.getTitle().equals(POSICIO_ACTUAL)) { // El marker es una base de bicicas o el punt geolocalitzat
            return;
        }

        puntTemp = marker;

        mUiSettings.setAllGesturesEnabled(false);
        marker.hideInfoWindow();
        Camara.posicionarDalt(mMap, marker);

        marker.setIcon(createCustomIconFromDrawable(R.drawable.marker_base_negatiu));

        baseFragment = dBase.get((Integer) marker.getTag());

        fragmentManager1 = getActivity().getSupportFragmentManager();
        fragmentManager1.executePendingTransactions();
        fragmentTransaction1 = fragmentManager1.beginTransaction();
        fragmentTransaction1.setCustomAnimations(R.anim.slide_up_animation, R.anim.slide_down_animation, R.anim.slide_up_animation, R.anim.slide_down_animation);
        fragmentTransaction1.replace(R.id.map, baseFragment);
        fragmentTransaction1.commit();
        fragmentTransaction1.addToBackStack(null);

        btnTotesBases.setVisibility(View.INVISIBLE);
        btnErarse.setVisibility(View.INVISIBLE);
        gpsPosition.setVisibility(View.INVISIBLE);
    }

    public FragmentActivity getActivity() {
        return this;
    }

    @Override
    public void changeBackIcon() {
        puntTemp.setIcon(createCustomIconFromDrawable(R.drawable.marker_base_bicicas_negatiu));
    }

    @Override
    public void onFragmentDetached() {
        getFragmentManager().popBackStack();
        mUiSettings.setAllGesturesEnabled(true);
        setVisibilitatElementsPrincipal(View.VISIBLE);
        btnTotesBases.setVisibility(View.VISIBLE);
        if(puntA != null && puntB != null) {//Si els dos punts existeixen no te que cambiar la camara
            camara.resetCamara(zoomDespresAnimacio);
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            getFragmentManager().popBackStack();
            super.onBackPressed();
        }

        if(ocultar) {
            ocultar = false;
            setVisibilitatElementsPrincipal(View.VISIBLE);
            btnTotesBases.setImageResource(R.drawable.ic_keyboard_arrow_up);
        }

    }

    private BitmapDescriptor createCustomIconFromDrawable(int idDrawable) {
        Drawable dr = getResources().getDrawable(idDrawable);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
        return BitmapDescriptorFactory.fromBitmap(bitmap);

    }

    @Override
    public void addPoint(Base base) {
        String nom = base.getNom();
        for(Marker marker: llistaMarkers) {
            if(marker.getTitle().equals(nom)) {
                puntTemp = marker;
                break;
            }
        }
        addPoint();

    }

    @Override
    public void addPoint() {
        mPreferencies.setReset(false, true);

        if(puntA != null && puntB != null) {
            borrarTot();
            if(puntTemp != null) camara.centrarCamaraA(puntTemp.getPosition());
        }


        if (puntA == null) {
            puntA = puntTemp;
            puntA.setIcon(icon_base_seleccionada);
        } else if (puntB == null) {
            puntB = puntTemp;
            puntB.setIcon(icon_base_seleccionada);
        }

        if(puntA != null && puntB != null) {

            if(!mPreferencies.isBiciPropia()) {
                if(puntA.getTitle().equals(puntB.getTitle()) && puntA.getTitle().equals(PUNT_PERSONALITZAT)) {
                    calcularRutaDesdePosFinsPos();
                } else if(puntA.getTitle().equals(POSICIO_ACTUAL) || puntA.getTitle().equals(PUNT_PERSONALITZAT)) {
                    calcularRutaDesdePos();
                } else if (puntB.getTitle().equals(POSICIO_ACTUAL) || puntB.getTitle().equals(PUNT_PERSONALITZAT)) {
                    calcularRutaFinsPos();
                } else {
                    calcularRuta();
                }
            } else {
                calcularRuta();
            }

        }

        actualizarTextPunts();
    }

    private void recalcularRuta() {
        if(puntA != null && puntB != null) {

            if(!mPreferencies.isBiciPropia()) {
                if(puntA.getTitle().equals(puntB.getTitle()) && puntA.getTitle().equals(PUNT_PERSONALITZAT)) {
                    calcularRutaDesdePosFinsPos();
                } else if(puntA.getTitle().equals(POSICIO_ACTUAL) || puntA.getTitle().equals(PUNT_PERSONALITZAT)) {
                    calcularRutaDesdePos();
                } else if (puntB.getTitle().equals(POSICIO_ACTUAL) || puntB.getTitle().equals(PUNT_PERSONALITZAT)) {
                    calcularRutaFinsPos();
                } else {
                    calcularRuta();
                }
            } else {
                calcularRuta();
            }

        }
    }

    private Base calcuarBaseProxima(Marker posicio) {
        Base baseProxima = null;

        double distanciaMinima;
        double distanciaActual;

        distanciaMinima = Double.MAX_VALUE;

        Log.i("BASES [0]", bases.get(0).getNom());
        for(Base base: bases) {
            Log.i("BASES", base.getNom());
            distanciaActual = base.calcularDistancia(posicio.getPosition());
            Log.i("DISTANCIA", base.getNom() + " " + distanciaActual);
            if(distanciaActual < distanciaMinima) {
                distanciaMinima = distanciaActual;
                baseProxima = base;
            }
        }

        Log.i(TAG, baseProxima.getNom());
        return baseProxima;
    }

    private Base calcuarBaseProximaAmbMinim(Marker posicio) {
        Base baseProxima = null;

        double distanciaMinima;
        double distanciaActual;

        distanciaMinima = Double.MAX_VALUE;

        Log.i("BASES [0]", bases.get(0).getNom());
        for(Base base: bases) {
            Log.i("BASES", base.getNom());
            distanciaActual = base.calcularDistancia(posicio.getPosition());
            Log.i("DISTANCIA", base.getNom() + " " + distanciaActual);
            if(distanciaActual < distanciaMinima && base.getOcupacio() > mPreferencies.getMinOcupacio()) {
                    distanciaMinima = distanciaActual;
                    baseProxima = base;
            }
        }

        Log.i(TAG, baseProxima.getNom());
        return baseProxima;
    }

    private void calcularRutaFinsPos() {
        Base baseProxima = calcuarBaseProxima(puntB);

        consegirRuta(calcularRuta(puntA, baseProxima, EN_BICI));
        consegirRuta(calcularRuta(baseProxima, puntB, A_PEU));

    }

    private void calcularRutaDesdePos() {
        Base baseProxima = calcuarBaseProximaAmbMinim(puntA);

        consegirRuta(calcularRuta(puntA, baseProxima, A_PEU));
        consegirRuta(calcularRuta(baseProxima, puntB, EN_BICI));
    }

    private void calcularRutaDesdePosFinsPos() {
        Base baseProximaSortida = calcuarBaseProximaAmbMinim(puntA);
        Base baseProximaArribada = calcuarBaseProxima(puntB);

        consegirRuta(calcularRuta(puntA, baseProximaSortida, A_PEU));
        consegirRuta(calcularRuta(baseProximaSortida, baseProximaArribada, EN_BICI));
        consegirRuta(calcularRuta(baseProximaArribada, puntB, A_PEU));
    }

    public Thread calcularRuta(Object origen, Object desti, String tipoRuta) {
        Thread calculRuta = new Thread(new PeticioRuta(origen, desti, tipoRuta));
        calculRuta.start();
        return calculRuta;
    }

    public void consegirRuta (Thread peticio) {
        try {
            peticio.join();
            ArrayList<LatLng> ruta = PeticioRuta.getRutes();
            if(ruta != null){
                Log.i(TAG, "RUTA ACONSEGUIDA");

                Polyline lineaRuta= mMap.addPolyline(PeticioRuta.getRuta());
                if(peticio.equals(A_PEU)) {
                    lineaRuta.setColor(Color.GRAY);
                }
                rutes.add(lineaRuta);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void calcularRuta() {
        // https://www.mapquestapi.com/directions/v2/route?key=KEY&from=Denver%2C+CO&to=Boulder%2C+CO&outFormat=json&ambiguities=ignore&routeType=fastest&doReverseGeocode=false&enhancedNarrative=false&avoidTimedConditions=false
        //Thread calculRuta = new Thread(new PeticioRutaMQ(puntA, puntB, NavigationRouteServiceFactory.getNavigationRouteService(getApplicationContext(), PeticioRutaMQ.KEY)));
        Log.i("POSICIONS", "PUNT A " + String.valueOf(puntA.getPosition().latitude) + String.valueOf(puntA.getPosition().longitude));
        Log.i("POSICIONS", "PUNT B " + String.valueOf(puntB.getPosition().latitude) + String.valueOf(puntB.getPosition().longitude));
        Thread calculRuta = new Thread(new PeticioRuta(puntA, puntB, "bicycle"));
        calculRuta.start();
        try {
            calculRuta.join();
            ArrayList<LatLng> ruta = PeticioRuta.getRutes();
            if(ruta != null){
                Log.i(TAG, "RUTA ACONSEGUIDA");

                Polyline lineaRuta = mMap.addPolyline(PeticioRuta.getRuta());
                rutes.add(lineaRuta);

                //camara.centrarCamaraA(PeticioRuta.getPuntMig());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void removePoint() {
        // Descelecciona el punt prèviament seleccionat
        if (puntA != null && puntB != null) {
            if (puntA.equals(puntTemp)) {
                puntA.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.base));
                puntA = null;
            } else if (puntB.equals(puntTemp)) {
                puntB.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.base));
                puntB = null;
            }

            actualizarTextPunts();
        }
    }

    @Override
    public void fullViewFragment() {
        // El fragment_llista te que visualizarse totalment
        //fragmentTransaction1.show(fragmentManager.getPrimaryNavigationFragment());
    }

    @Override
    public void goTo(Base base) {
        if(puntTemp != null) {
            puntTemp.setIcon(iconBike);
        }
        if(baseFragment != null) {
            baseFragment.onDetach();
        }
        for(Marker marker: llistaMarkers) {
            String nom = marker.getTitle();
            if(nom.equals(base.getNom())){
                onInfoWindowLongClick(marker);
                break;
            }
        }
    }

    @Override
    public void zoomDespresAnimacio(int zoom) {
        this.zoomDespresAnimacio = zoom;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_nav_ocupacio:
                Intent intent_ocupacio = new Intent(this, SelectorOcupacio.class);
                intent_ocupacio.putExtra(OCUPACIO_SELECCIONADA, mPreferencies.getMinOcupacio());
                startActivityForResult(intent_ocupacio, SEEKBAR_OPT);

                break;
            case R.id.item_nav_instruccions:
                onBackPressed();
                Intent intent_instruccions = new Intent(this, InstruccionsActivity.class);
                startActivity(intent_instruccions);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEEKBAR_OPT) {
            if (resultCode == RESULT_OK) {
                int result = data.getExtras().getInt(SelectorOcupacio.SEEK_RESULT);
                mPreferencies.setMinOcupacio(result, true);
            }
        }
    }

    @Override
    public void addFavorita(String nom) {
        for (Base base: bases) {
            if(base.getNom().equals(nom)){
                base.setPreferida(true);
                break;
            }
        }
        mPreferencies.addBasePreferida(nom);
        //mPreferencies.actualitzarBases();

    }

    @Override
    public void removeFavorida(String nom) {
        for (Base base: bases) {
            if(base.getNom().equals(nom)){
                base.setPreferida(false);
                break;
            }
        }
        mPreferencies.removeBasePreferida(nom);
        //mPreferencies.actualitzarBases();

    }

    @Override
    public void aplicarCanvis() {
        mPreferencies.actualitzarBases();
    }
}
