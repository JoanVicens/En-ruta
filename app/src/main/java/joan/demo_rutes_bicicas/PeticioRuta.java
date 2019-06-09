package joan.demo_rutes_bicicas;

import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class PeticioRuta implements Runnable {

    private static final String MQ_KEY = "KVxAkokKwkj6BKqwfAoXigIEAocuwZOm";
    private static final String TAG = "PeticioRuta";
    private static RoadManager roadManager;
    private static ArrayList<GeoPoint> wayPoints;
    private static Road road;
    private static PolylineOptions pOptions;
    private static LatLng puntMig;
    private static String tipoRuta;

    public PeticioRuta(Object origen, Object desti, String tipoRuta) {
        this.roadManager = new MapQuestRoadManager(MQ_KEY);
        this.roadManager.addRequestOption(String.format("routeType=%s", tipoRuta));
        wayPoints = new ArrayList<>();

        this.tipoRuta = tipoRuta;

        LatLng positionOrigen = null;
        LatLng positionDesti = null;

        if(origen instanceof Marker) {
            positionOrigen = ((Marker) origen).getPosition();
        } else if(origen instanceof Base) {
            positionOrigen = ((Base) origen).getPosicio();
        }
        if(desti instanceof  Marker) {
            positionDesti = ((Marker) desti).getPosition();
        } else if(desti instanceof Base) {
            positionDesti = ((Base) desti).getPosicio();
        }

        if(positionOrigen != null && positionDesti != null) {
            wayPoints.add(new GeoPoint(positionOrigen.latitude, positionOrigen.longitude));
            wayPoints.add(new GeoPoint(positionDesti.latitude, positionDesti.longitude));
        } else {
            Log.i(TAG, positionDesti+"");
        }

    }

    public static ArrayList<LatLng> getRutes() {
        ArrayList<LatLng> nodes = new ArrayList<>();

        for (RoadNode node : road.mNodes){
            GeoPoint point = node.mLocation;
            nodes.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }

        return nodes;
    }



    @Override
    public void run() {
        if (wayPoints.size() < 2 ){
            return;
        }

        try {
            road = roadManager.getRoad(wayPoints); //Petició

        } catch (java.lang.IndexOutOfBoundsException ex) {

        }

        if(road == null) {
            return;

        }

        ArrayList<LatLng> nodes = new ArrayList<>();

        for (RoadNode node : road.mNodes){
            GeoPoint point = node.mLocation;
            nodes.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }

        if(tipoRuta.equals(MapsActivity.A_PEU)) {
            pOptions = new PolylineOptions()
                    .clickable(false)
                    .color(Color.GRAY)
                    .addAll(nodes);
        } else {
            pOptions = new PolylineOptions()
                    .clickable(false)
                    .color(Color.RED)
                    .addAll(nodes);
        }
        Log.i("POSICIONS", String.valueOf(pOptions.getPoints()));

        if(nodes.isEmpty())
            return;

        LatLng puntInici = nodes.get(0);
        LatLng puntFinal = nodes.get(nodes.size() - 1);

        puntMig = new LatLng((puntInici.latitude + puntFinal.latitude)/2, (puntInici.longitude + puntFinal.longitude)/2);

    }

    public static PolylineOptions getRuta() {
        return pOptions;
    }

    public static LatLng getPuntMig() { return puntMig; }
}
