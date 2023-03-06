package com.google.cardboard;

import android.os.Parcel;
import android.os.Parcelable;

public class ParameterSeries implements Parcelable {
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

    /*
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
    }*/
}
