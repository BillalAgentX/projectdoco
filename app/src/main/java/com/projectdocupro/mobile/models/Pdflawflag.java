
package com.projectdocupro.mobile.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
public class Pdflawflag implements Parcelable{


    @PrimaryKey(autoGenerate = true)
    @SerializedName("localPdflawflagId")
    public long localPdflawflagId = 0;
    @SerializedName("pdProjectid")
    @Expose
    private String pdProjectid;
    @SerializedName("pdFlawFlagServerId")
    @Expose
    private String pdFlawFlagServerId = "0";
    @SerializedName("flaw_Id")
    @Expose
    private String flaw_Id;
    @SerializedName("flaw_status")
    @Expose
    private String flaw_status;
    @SerializedName("local_flaw_Id")
    @Expose
    private String local_flaw_Id;
    @SerializedName("pdflawflagid")
    @Expose
    private String pdflawflagid;
    @SerializedName("pdplanid")
    @Expose
    private String pdplanid;
    @SerializedName("xcoord")
    @Expose
    private String xcoord;
    @SerializedName("degree")
    @Expose
    public Integer degree = 0;
    @SerializedName("viewx")
    @Expose
    public String viewx;
    @SerializedName("viewy")
    @Expose
    public String viewy;
    @SerializedName("local_photo_id")
    @Expose
    private String local_photo_id;
    @SerializedName("ycoord")
    @Expose
    private String ycoord;
    @SerializedName("created")
    @Expose
    private String created;
    @SerializedName("lastupdated")
    @Expose
    private String lastupdated;
    public float scale_factor = 0;
    public Integer is_arrow_located = 0;
    private String pdFlagId = "";

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;

    public Pdflawflag() {
    }

    protected Pdflawflag(Parcel in) {
        localPdflawflagId = in.readLong();
        pdProjectid = in.readString();
        pdFlawFlagServerId = in.readString();
        flaw_Id = in.readString();
        flaw_status = in.readString();
        local_flaw_Id = in.readString();
        pdflawflagid = in.readString();
        pdplanid = in.readString();
        xcoord = in.readString();
        if (in.readByte() == 0) {
            degree = null;
        } else {
            degree = in.readInt();
        }
        viewx = in.readString();
        viewy = in.readString();
        local_photo_id = in.readString();
        ycoord = in.readString();
        created = in.readString();
        lastupdated = in.readString();
        scale_factor = in.readFloat();
        if (in.readByte() == 0) {
            is_arrow_located = null;
        } else {
            is_arrow_located = in.readInt();
        }
        pdFlagId = in.readString();
    }

    public static final Creator<Pdflawflag> CREATOR = new Creator<Pdflawflag>() {
        @Override
        public Pdflawflag createFromParcel(Parcel in) {
            return new Pdflawflag(in);
        }

        @Override
        public Pdflawflag[] newArray(int size) {
            return new Pdflawflag[size];
        }
    };

    public String getPdFlawFlagServerId() {
        return pdFlawFlagServerId;
    }

    public void setPdFlawFlagServerId(String pdFlawFlagServerId) {
        this.pdFlawFlagServerId = pdFlawFlagServerId;
    }

    public String getFlaw_status() {
        return flaw_status;
    }

    public void setFlaw_status(String flaw_status) {
        this.flaw_status = flaw_status;
    }

    public String getLocal_flaw_Id() {
        return local_flaw_Id;
    }

    public void setLocal_flaw_Id(String local_flaw_Id) {
        this.local_flaw_Id = local_flaw_Id;
    }

    public String getFlaw_Id() {
        return flaw_Id;
    }

    public void setFlaw_Id(String flaw_Id) {
        this.flaw_Id = flaw_Id;
    }

    public Integer getDegree() {
        return degree;
    }

    public void setDegree(Integer degree) {
        this.degree = degree;
    }

    public String getViewx() {
        return viewx;
    }

    public void setViewx(String viewx) {
        this.viewx = viewx;
    }

    public String getViewy() {
        return viewy;
    }

    public void setViewy(String viewy) {
        this.viewy = viewy;
    }

    public float getScale_factor() {
        return scale_factor;
    }

    public void setScale_factor(float scale_factor) {
        this.scale_factor = scale_factor;
    }

    public Integer getIs_arrow_located() {
        return is_arrow_located;
    }

    public void setIs_arrow_located(Integer is_arrow_located) {
        this.is_arrow_located = is_arrow_located;
    }

    public String getLocal_photo_id() {
        return local_photo_id;
    }

    public void setLocal_photo_id(String local_photo_id) {
        this.local_photo_id = local_photo_id;
    }

    public long getLocalPdflawflagId() {
        return localPdflawflagId;
    }

    public void setLocalPdflawflagId(long localPdflawflagId) {
        this.localPdflawflagId = localPdflawflagId;
    }

    public String getPdProjectid() {
        return pdProjectid;
    }

    public void setPdProjectid(String pdProjectid) {
        this.pdProjectid = pdProjectid;
    }

    public String getPdflawflagid() {
        return pdflawflagid;
    }

    public void setPdflawflagid(String pdflawflagid) {
        this.pdflawflagid = pdflawflagid;
    }

    public String getPdplanid() {
        return pdplanid;
    }

    public void setPdplanid(String pdplanid) {
        this.pdplanid = pdplanid;
    }

    public String getXcoord() {
        return xcoord;
    }

    public void setXcoord(String xcoord) {
        this.xcoord = xcoord;
    }

    public String getYcoord() {
        return ycoord;
    }

    public void setYcoord(String ycoord) {
        this.ycoord = ycoord;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLastupdated() {
        return lastupdated;
    }

    public void setLastupdated(String lastupdated) {
        this.lastupdated = lastupdated;
    }

    public String getPdFlagId() {
        return pdFlagId;
    }

    public void setPdFlagId(String pdFlagId) {
        this.pdFlagId = pdFlagId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(localPdflawflagId);
        parcel.writeString(pdProjectid);
        parcel.writeString(pdFlawFlagServerId);
        parcel.writeString(flaw_Id);
        parcel.writeString(flaw_status);
        parcel.writeString(local_flaw_Id);
        parcel.writeString(pdflawflagid);
        parcel.writeString(pdplanid);
        parcel.writeString(xcoord);
        if (degree == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(degree);
        }
        parcel.writeString(viewx);
        parcel.writeString(viewy);
        parcel.writeString(local_photo_id);
        parcel.writeString(ycoord);
        parcel.writeString(created);
        parcel.writeString(lastupdated);
        parcel.writeFloat(scale_factor);
        if (is_arrow_located == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(is_arrow_located);
        }
        parcel.writeString(pdFlagId);
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2;
    }

    public String getExtra3() {
        return extra3;
    }

    public void setExtra3(String extra3) {
        this.extra3 = extra3;
    }

    public String getExtra4() {
        return extra4;
    }

    public void setExtra4(String extra4) {
        this.extra4 = extra4;
    }

    public String getExtra5() {
        return extra5;
    }

    public void setExtra5(String extra5) {
        this.extra5 = extra5;
    }
}
