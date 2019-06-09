package joan.demo_rutes_bicicas;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HelperFunctions {

    private static final int NOM = 0;
    private static final int LAT = 1;
    private static final int LNG = 2;

    public static void ocultarMarkers(ArrayList<Marker> bases) {
        for(Marker bs: bases) {
            bs.setVisible(false);
        }
    }

    public static void mostrarMarkers(ArrayList<Marker> bases) {
        for(Marker bs: bases) {
            bs.setVisible(true);
        }
    }

    public static void cambiarIcono(ArrayList<Marker> llistaMarkers, int id) {
        Marker mk = llistaMarkers.get(id);
        mk.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
    }

    public static boolean esPreferida(Preferencies pref, Base base) {
        ArrayList<String> basesPreferides = pref.getBasesPreferides();

        return basesPreferides.contains(base.getNom());
    }

    public static void refrescarPreferides(ArrayList<Base> bases, Preferencies mPreferencies) {
        ArrayList<String> nomPreferides = mPreferencies.getBasesPreferides();

        if(nomPreferides.size() > 0 ){
            for(Base base: bases) {
                if(nomPreferides.contains(base.getNom())) {
                    base.setPreferida(true);
                } else {
                    base.setPreferida(false);
                }
            }
        }

    }

    public static List<String> llegirMarkersEstacionsTram(Context context) {

        String ruta ="estacions_tram.txt";
        List<String> linies = new ArrayList<>();

        AssetManager am = context.getAssets();

        try {
            InputStream is = context.getAssets().open("estacions_tram.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "Windows-1252"));
            String line;

            while ((line = reader.readLine()) != null)
                linies.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return linies;
    }

    public static MarkerOptions crearMarkerEstacionsTram(MarkerOptions markerOptionsIcono, String linia) {

        String[] info = linia.split(",");

        LatLng posicio = new LatLng(Double.parseDouble(info[LAT]), Double.parseDouble(info[LNG]));

        MarkerOptions marker =  markerOptionsIcono;
        marker.position(posicio).title(info[NOM]);


        return marker;
    }
}
