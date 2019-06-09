package joan.demo_rutes_bicicas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class Preferencies implements Parcelable {

    public static final String SHARED_PREF = "opcions";

    public static final String MOSTRAR_BASES_BICICAS = "MOSTRAR BASES BICICAS";
    public static final String TIPUS_DE_MAPA = "TIPUS_DE_MAPA";
    public static final String BICI_PROPIA = "BICI_PROPIA";
    public static final String MIN_OCUPACIO = "MIN_OCUPACIO";
    private static final String RESETEJAR_SWITCH_DETALL = "RESETEJAR SWITCH DETALL";
    private static final String BASES_PREFERIDES = "BASES PREFERIDES";
    private static final String ESTACIONS_TRAM = "ESTACIONS TRAM";
    private static final String CARRILS_BICI = "CARRILS BICI";

    // Preferencies
    protected SharedPreferences settings;
    protected SharedPreferences.Editor editor;


    // Opcions disponibles
    private boolean mostrarBases, biciPropia, mostrarEstacionsTram, mostrarCarrilsBici, reset;
    private int tipoMapa, minOcupacio;
    private ArrayList<String> basesPreferides;

    public Preferencies(Context context) {
        settings = context.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        this.editor = settings.edit();
        actualitzarTotes();
    }

    protected Preferencies(Parcel in) {
        mostrarBases = in.readByte() != 0;
        biciPropia = in.readByte() != 0;
        reset = in.readByte() != 0;
        tipoMapa = in.readInt();
        minOcupacio = in.readInt();
    }

    public static final Creator<Preferencies> CREATOR = new Creator<Preferencies>() {
        @Override
        public Preferencies createFromParcel(Parcel in) {
            return new Preferencies(in);
        }

        @Override
        public Preferencies[] newArray(int size) {
            return new Preferencies[size];
        }
    };

    private void actualitzarTotes() {
        this.mostrarBases = settings.getBoolean(Preferencies.MOSTRAR_BASES_BICICAS, true);
        this.tipoMapa = settings.getInt(Preferencies.TIPUS_DE_MAPA, GoogleMap.MAP_TYPE_NORMAL);
        this.biciPropia = settings.getBoolean(Preferencies.BICI_PROPIA, false);
        this.minOcupacio = settings.getInt(Preferencies.MIN_OCUPACIO, 1);
        this.reset = settings.getBoolean(Preferencies.RESETEJAR_SWITCH_DETALL, false);
        this.mostrarEstacionsTram = settings.getBoolean(Preferencies.ESTACIONS_TRAM, false);
        this.mostrarCarrilsBici = settings.getBoolean(Preferencies.CARRILS_BICI, false);

        this.basesPreferides = new ArrayList<>();
    }

    public void guardar() {
        if(this.editor != null){
            editor.apply();
        }

    }

    public boolean isMostrarCarrilsBici() {
        return mostrarCarrilsBici;
    }

    public void setMostrarCarrilsBici(boolean mostrarCarrilsBici, boolean autoSave) {
        this.mostrarCarrilsBici = mostrarCarrilsBici;
        this.editor.putBoolean(CARRILS_BICI, mostrarCarrilsBici);
        if(autoSave) {
            editor.apply();
        }
    }

    public boolean isMostrarEstacionsTram() {
        return this.mostrarEstacionsTram;
    }

    public void setMostrarEstacionsTram(boolean mostrar, boolean autoSave) {
        this.mostrarEstacionsTram = mostrar;
        this.editor.putBoolean(ESTACIONS_TRAM, mostrar);
        if(autoSave) {
            editor.apply();
        }
    }

    private String arrayListToString(ArrayList<String> array) {
        String string = "";
        for(String str: array) {
            string += str + ",";
        }

        return string;
    }

    private ArrayList<String> stringToArrayList(String string) {
        String[] array = string.split(",");
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(array));
        return arrayList;
    }

    public void addBasePreferida(String nomBase) {
        if(!basesPreferides.contains(nomBase)) {
            this.basesPreferides.add(nomBase);
        } else {
            Log.i("BASES PREFERIDES", nomBase + " ja esta afegida");
        }
    }

    public void removeBasePreferida(String nomBase) {
        this.basesPreferides.remove(nomBase);
    }

    public void setBasesPreferides(ArrayList<String> basesPreferides) {
        this.basesPreferides = basesPreferides;
    }

    public void actualitzarBases() {
        this.editor.putString(Preferencies.BASES_PREFERIDES, arrayListToString(this.basesPreferides));
        this.editor.apply();

    }

    public ArrayList<String> getBasesPreferides() {
        String strParsejar = settings.getString(Preferencies.BASES_PREFERIDES, "");
        this.basesPreferides = stringToArrayList(strParsejar);
        return basesPreferides;
    }

    public boolean isResetTime() {
        return reset;
    }

    public void setReset(boolean resetTime, boolean autoSave) {
        this.reset = resetTime;
        this.editor.putBoolean(RESETEJAR_SWITCH_DETALL, resetTime);
        if(autoSave) {
            editor.apply();
        }
    }

    public int getMinOcupacio() {
        return minOcupacio;
    }

    public boolean MostrarBases() {
        return mostrarBases;
    }

    public boolean isBiciPropia() {
        return biciPropia;
    }

    public int getTipoMapa() {
        return tipoMapa;
    }

    public void setMinOcupacio(int ocupacio, boolean autoSave) {
        this.minOcupacio = ocupacio;
        this.editor.putInt(MIN_OCUPACIO, ocupacio);
        if(autoSave) {
            editor.apply();
        }

    }

    public void setMostrarBases(boolean mostrarBases, boolean autoSave) {
        this.mostrarBases = mostrarBases;
        this.editor.putBoolean(MOSTRAR_BASES_BICICAS, mostrarBases);
        if(autoSave) {
            editor.apply();
        }
    }

    public void setBiciPropia(boolean biciPropia, boolean autoSave) {
        this.biciPropia = biciPropia;
        this.editor.putBoolean(BICI_PROPIA, biciPropia);
        if(autoSave) {
            editor.apply();
        }
    }

    public void setTipoMapa(int tipoMapa, boolean autoSave) {
        this.tipoMapa = tipoMapa;
        this.editor.putInt(TIPUS_DE_MAPA, tipoMapa);
        if(autoSave) {
            editor.apply();
        }
    }

    public boolean mostrarBasesBaixaOcupacio() {
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (mostrarBases ? 1: 0));
        parcel.writeByte((byte) (biciPropia ? 1: 0));
        parcel.writeByte((byte) (reset ? 1: 0));
        parcel.writeInt(tipoMapa);
        parcel.writeInt(minOcupacio);
    }


}
