package com.projectdocupro.mobile.models;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;

public class User {
    @SerializedName("pduserid")
    private String  pduserid;

    @SerializedName("email")
    private String  email;

    @SerializedName("created")
    private String  created;

    @SerializedName("lastlogin")
    private String   lastlogin;

    @SerializedName("active")
    private String active;

    @SerializedName("status")
    String status;

    @SerializedName("gender")
    String gender;

    @SerializedName("title")
    String  title;

    @SerializedName("firstname")
    String  firstname;

    @SerializedName("lastname")
    String  lastname;

    @SerializedName("company")
    String  company;

    @SerializedName("street")
    String  street;

    @SerializedName("zipcode")
    String  zipcode;

    @SerializedName("city")
    String  city;

    @SerializedName("country")
    String  country;

    @SerializedName("description")
    String  description;

    @SerializedName("language")
    String  language;

    @SerializedName("rights")
    String  rights;

    @SerializedName("token")
    String  token;

    @SerializedName("token_date")
    String  token_date;

    @SerializedName("emailinfo")
    String  emailinfo;

    public User(Context context) {
        SharedPrefsManager  sharedPrefsManager  =   new SharedPrefsManager(context);
        this.pduserid = sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID,"");
        this.email = sharedPrefsManager.getStringValue(AppConstantsManager.USER_EMAIL,"");
        this.created = sharedPrefsManager.getStringValue(AppConstantsManager.USER_CREATED,"");
        this.lastlogin = sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_LOGIN,"");
        this.active = sharedPrefsManager.getStringValue(AppConstantsManager.USER_ACTIVE,"0");
        this.status = sharedPrefsManager.getStringValue(AppConstantsManager.USER_STATUS,"0");
        this.gender = sharedPrefsManager.getStringValue(AppConstantsManager.USER_GENDER,"0");
        this.title = sharedPrefsManager.getStringValue(AppConstantsManager.USER_TITLE,"");
        this.firstname = sharedPrefsManager.getStringValue(AppConstantsManager.USER_FIRST_NAME,"");
        this.lastname = sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_NAME,"");
        this.company = sharedPrefsManager.getStringValue(AppConstantsManager.USER_COMPANY,"");
        this.street = sharedPrefsManager.getStringValue(AppConstantsManager.USER_STREET,"");
        this.zipcode = sharedPrefsManager.getStringValue(AppConstantsManager.USER_ZIPCODE,"");
        this.city = sharedPrefsManager.getStringValue(AppConstantsManager.USER_CITY,"");
        this.country = sharedPrefsManager.getStringValue(AppConstantsManager.USER_COUNTRY,"");
        this.description = sharedPrefsManager.getStringValue(AppConstantsManager.USER_DESCRIPTION,"");
        this.language = sharedPrefsManager.getStringValue(AppConstantsManager.USER_LANGUAGE,"");
        this.rights = sharedPrefsManager.getStringValue(AppConstantsManager.USER_RIGHTS,"");
        this.token = sharedPrefsManager.getStringValue(AppConstantsManager.USER_TOKEN,"");
        this.token_date = sharedPrefsManager.getStringValue(AppConstantsManager.USER_TOKEN_DATE,"");
        this.emailinfo = sharedPrefsManager.getStringValue(AppConstantsManager.USER_EMAIL_INFO,"");
    }

    public String getPduserid() {
        return pduserid;
    }

    public String getEmail() {
        return email;
    }

    public String getCreated() {
        return created;
    }

    public String getLastlogin() {
        return lastlogin;
    }

    public String getActive() {
        return active;
    }

    public String getStatus() {
        return status;
    }

    public String getGender() {
        return gender;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getCompany() {
        return company;
    }

    public String getStreet() {
        return street;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public String getRights() {
        return rights;
    }

    public String getToken() {
        return token;
    }

    public String getToken_date() {
        return token_date;
    }

    public String getEmailinfo() {
        return emailinfo;
    }
}
