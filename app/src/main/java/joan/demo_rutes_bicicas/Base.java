package joan.demo_rutes_bicicas;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Base implements Parcelable {

    private LatLng posicio;
    private String nom;
    private int id;
    private int capacitat;
    private int ocupacio;
    private boolean visible;
    private boolean preferida;

    public Base(LatLng posicio, int id, String nom, int capacitat, int ocupacio) {
        this.posicio = posicio;
        this.id = id;
        this.nom = nom;
        this.capacitat = capacitat;
        this.ocupacio = ocupacio;
        this.visible = true;
    }

    protected Base(Parcel in) {
        posicio = in.readParcelable(LatLng.class.getClassLoader());
        nom = in.readString();
        id = in.readInt();
        capacitat = in.readInt();
        ocupacio = in.readInt();
        visible = in.readByte() != 0;
    }


    public static final Creator<Base> CREATOR = new Creator<Base>() {
        @Override
        public Base createFromParcel(Parcel in) {
            return new Base(in);
        }

        @Override
        public Base[] newArray(int size) {
            return new Base[size];
        }
    };

    public void setPreferida(boolean esPreferida) {
        this.preferida = esPreferida;
    }

    public boolean isPreferida() {
        return this.preferida;
    }

    public int getId() {
        return id;
    }

    public LatLng getPosicio() {
        return posicio;
    }

    public void setPosicio(LatLng posicio) {
        this.posicio = posicio;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getCapacitat() {
        return capacitat;
    }

    public void setCapacitat(int capacitat) {
        this.capacitat = capacitat;
    }

    public int getOcupacio() {
        return ocupacio;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOcupacio(int ocupacio) {
        this.ocupacio = ocupacio;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(nom);
        parcel.writeDouble(posicio.latitude);
        parcel.writeDouble(posicio.longitude);

        parcel.writeInt(capacitat);
        parcel.writeInt(ocupacio);

    }

    private void readFromParcel(Parcel in) {

        id = in.readInt();
        nom = in.readString();
        double lat = in.readDouble();
        double lng = in.readDouble();
        posicio = new LatLng(lat, lng);
        capacitat = in.readInt();
        ocupacio = in.readInt();
    }

    public double calcularDistancia(LatLng posicio) {
        // Calcula la distància Manhatan
        double x = this.posicio.latitude - posicio.latitude;
        double y = this.posicio.longitude - posicio.longitude;

        return Math.abs(x) + Math.abs(y);
    }
}
