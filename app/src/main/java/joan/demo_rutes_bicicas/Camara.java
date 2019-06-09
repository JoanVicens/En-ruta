package joan.demo_rutes_bicicas;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class Camara {

    public static final int ZOOM_INICIAL = 0;
    public static final int ZOOM_ACTUAL = 1;

    private static final LatLng centre = new LatLng(39.984224, -0.029112);
    // Bordes
    private static final LatLng bordeSuperior = new LatLng(40.045, -0.097);
    private static final LatLng bordeInferior = new LatLng(39.957, -0.048);

    private static GoogleMap map;

    private static LatLng ultimaPosicio;

    public Camara(GoogleMap map) {
        this.map = map;
    }

    public static void posicionarDalt(GoogleMap mMap, Marker marker) {
        ultimaPosicio = marker.getPosition();

        ultimaPosicio = new LatLng(ultimaPosicio.latitude-0.0040, ultimaPosicio.longitude);

        CameraPosition novaPosicio = new CameraPosition.Builder()
                .target(ultimaPosicio)
                .zoom(20)
                .tilt(0)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(novaPosicio));
    }

    public static void inicializar() {
        // Setup Camara
        map.moveCamera(CameraUpdateFactory.newLatLng(centre));

        LatLngBounds Castello = new LatLngBounds(bordeInferior ,bordeSuperior);
        map.setLatLngBoundsForCameraTarget(Castello);
        // Zoom
        map.setMinZoomPreference(12);
        map.setMaxZoomPreference(15);

        setCamaraPosition();

    }

    public static void centrarCamaraA(LatLng posicio) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(posicio)
                .zoom(15)
                .tilt(0)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public static void resetCamara(int zoom) {

        CameraPosition cameraPosition = null;
        switch (zoom) {
            case ZOOM_INICIAL:
                cameraPosition = new CameraPosition.Builder().target(centre)
                    .zoom(13)
                    .tilt(65)
                    .build();
                break;
            case ZOOM_ACTUAL:
                cameraPosition = new CameraPosition.Builder().target(centre)
                        .target(ultimaPosicio)
                    .zoom(20)
                    .tilt(0)
                    .build();
                break;
        }

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private static void setCamaraPosition() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(centre)
                .zoom(13)
                .tilt(65)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
