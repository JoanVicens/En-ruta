package joan.demo_rutes_bicicas;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class CarrilsBici implements Runnable {
    
    private Context context;
    private ArrayList<String> linies;

    private static ArrayList<PolylineOptions> polyLinesCarrils;

    public CarrilsBici(Context context ){
        this.context = context;

        this.linies = new ArrayList<>();
        this.polyLinesCarrils = new ArrayList<>();

    }

    @Override
    public void run() {

        try {
            InputStream is = context.getAssets().open("Ways.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "Windows-1252"));
            String line;

            while ((line = reader.readLine()) != null) {
                linies.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<LatLng[]> carrils = parsejarWays();
        afegirCarrils(carrils);

    }

    public static ArrayList<PolylineOptions> getPolyLinesCarrils() {
        return polyLinesCarrils;
    }

    private void afegirCarrils(ArrayList<LatLng[]> carrils) {

        PolylineOptions polylineOptions;

        for(LatLng[] tramCarril: carrils) {
            ArrayList<LatLng> puntsPolyLine = new ArrayList<>(Arrays.asList(tramCarril));

            polylineOptions = new PolylineOptions()
                    .clickable(false)
                    .color(context.getResources().getColor(R.color.complementari_logo_fosc))
                    .width(4)
                    .addAll(puntsPolyLine);

            this.polyLinesCarrils.add(polylineOptions);

        }
    }

    private ArrayList<LatLng[]> parsejarWays() {

        ArrayList<LatLng[]> carrils = new ArrayList<>();

        for(String linia: linies) {
            if(linia.startsWith("Way")) {
                String[] info = linia.split(" ");
                if(info.length > 1) {
                    LatLng[] puntsPolyLine = new LatLng[info.length -1];
                    for(int i = 1; i < info.length; i++) { // La primera linea es "Way"
                        //Log.i("PUNTS", info[i]);
                        String[] coordenades = info[i].split("/");
                        //Log.i("PUNTS", coordenades[0]);
                        puntsPolyLine[i -1] = new LatLng(Double.parseDouble(coordenades[0]), Double.parseDouble(coordenades[1]));
                    }

                    carrils.add(puntsPolyLine);
                }
            }
        }

        return carrils;
    }


}
