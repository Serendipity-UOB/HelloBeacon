package com.bristol.hackerhunt.helloworld.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PlayerIdentifiers implements Parcelable {

    public static final Parcelable.Creator<PlayerIdentifiers> CREATOR = new Parcelable.Creator<PlayerIdentifiers>(){
        @Override
        public PlayerIdentifiers createFromParcel(Parcel parcel) {
            return new PlayerIdentifiers(parcel);
        }

        @Override
        public PlayerIdentifiers[] newArray(int size) {
            return new PlayerIdentifiers[size];
        }
    };

    private String realName;
    private String hackerName;
    private String nfcId;


    public PlayerIdentifiers(String realName, String hackerName, String nfcId) {
        this.realName = realName;
        this.hackerName = hackerName;
        this.nfcId = nfcId;
    }

    public PlayerIdentifiers(Parcel in) {
        realName = in.readString();
        hackerName = in.readString();
        nfcId = in.readString();
    }

    public String getRealName() {
        return realName;
    }

    public String getHackerName() {
        return hackerName;
    }

    public String getNfcId() {
        return nfcId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(realName);
        parcel.writeString(hackerName);
        parcel.writeString(nfcId);
    }
}
