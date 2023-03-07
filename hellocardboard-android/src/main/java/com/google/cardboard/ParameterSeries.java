package com.google.cardboard;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class ParameterSeries implements Parcelable {
    private static final String TAG = "ParameterSeries";
    private int nbMesures;
    private int sensBarre;
    private int sensFond;
    private int mode;
    private float vitesseFond;

    public ParameterSeries(int nbMesures, int mode, int sensBarre, int sensFond, float vitesseFond) {
        this.nbMesures = nbMesures;
        this.sensBarre = sensBarre; //0 = droite (sens horaire) ; 1 = gauche (sens antihoraire)
        this.sensFond = sensFond; //0 = droite (sens horaire) ; 1 = gauche (sens antihoraire)
        this.mode = mode; //0 = vvs simple ; 1 = vvs dynamique
        this.vitesseFond = vitesseFond;
    }


    protected ParameterSeries(Parcel in) {
        nbMesures = in.readInt();
        sensBarre = in.readInt();
        sensFond = in.readInt();
        mode = in.readInt();
        vitesseFond = in.readFloat();
    }

    public static final Creator<ParameterSeries> CREATOR = new Creator<ParameterSeries>() {
        @Override
        public ParameterSeries createFromParcel(Parcel in) {
            return new ParameterSeries(in);
        }

        @Override
        public ParameterSeries[] newArray(int size) {
            return new ParameterSeries[size];
        }
    };

    public void setnbMesures(int nbMesures) {
        this.nbMesures = nbMesures;
    }

    public void setSensBarre(int sensBarre) {
        this.sensBarre = sensBarre;
    }

    public void setsensFond(int sensFond) {
        this.sensFond = sensFond;
    }

    public void setmode(int mode) {
        this.mode = mode;
    }

    public void setVitesseFond(float vitesse) {
        this.vitesseFond = vitesse;
    }


    public int getNbMesures() {
        return nbMesures;
    }

    public int getSensBarre() {
        return sensBarre;
    }

    public int getSensFond() {
        return sensFond;
    }

    public int getMode() { return mode; }

    public float getVitesseFond() { return vitesseFond; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(nbMesures);
        parcel.writeInt(sensBarre);
        parcel.writeInt(sensFond);
        parcel.writeInt(mode);
        parcel.writeFloat(vitesseFond);

    }

    @Override
    public String toString() {
        return nbMesures + "l" + sensBarre + "l" + sensFond + "l" + mode + "l" + vitesseFond;
    }

    public static ParameterSeries fromString(String s) {
        Log.d(TAG,s);
        String[] parts = s.split("l");
        int nbMesures = Integer.parseInt(parts[0]);
        int sensBarre = Integer.parseInt(parts[1]);
        int sensFond = Integer.parseInt(parts[2]);
        int mode = Integer.parseInt(parts[3]);
        float vitesseFond = Float.parseFloat(parts[4]);
        return new ParameterSeries(nbMesures, mode, sensBarre, sensFond, vitesseFond);
    }
}
