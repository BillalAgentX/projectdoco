package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class CustomersModel {

    @PrimaryKey
    @NonNull
    @SerializedName("customer_id")
    String  customerId;

    @SerializedName("customer_number")
    String  customerNumber;

    @SerializedName("company_name")
    String  companyName;

    @SerializedName("company_name_department")
    String  companyNameDepartment;

    @SerializedName("adress")
    String  address;

    @SerializedName("adress2")
    String  address2;

    @SerializedName("zipcode")
    String  zipcode;

    @SerializedName("city")
    String  city;

    @SerializedName("country")
    String  country;

    @SerializedName("contactperson_u_id")
    String  contactpersonUserId;

    @SerializedName("gender")
    String  gender;

    @SerializedName("title")
    String title;

    @SerializedName("firstname")
    String  firstName;

    @SerializedName("lastname")
    String  lastName;

    @SerializedName("phone1")
    String  phone1;

    @SerializedName("phone2")
    String  phone2;

    @SerializedName("information")
    String  information;

    @SerializedName("date_create")
    String  dateCreate;

    @SerializedName("date_edit")
    String  dateEdit;

    @SerializedName("authorityId")
    String  authorityId;

    public CustomersModel(@NonNull String customerId, String customerNumber, String companyName, String companyNameDepartment, String address, String address2, String zipcode, String city, String country, String contactpersonUserId, String gender, String title, String firstName, String lastName, String phone1, String phone2, String information, String dateCreate, String dateEdit, String authorityId) {
        this.customerId = customerId;
        this.customerNumber = customerNumber;
        this.companyName = companyName;
        this.companyNameDepartment = companyNameDepartment;
        this.address = address;
        this.address2 = address2;
        this.zipcode = zipcode;
        this.city = city;
        this.country = country;
        this.contactpersonUserId = contactpersonUserId;
        this.gender = gender;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.information = information;
        this.dateCreate = dateCreate;
        this.dateEdit = dateEdit;
        this.authorityId = authorityId;
    }


    @NonNull
    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyNameDepartment() {
        return companyNameDepartment;
    }

    public String getAddress() {
        return address;
    }

    public String getAddress2() {
        return address2;
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

    public String getContactpersonUserId() {
        return contactpersonUserId;
    }

    public String getGender() {
        return gender;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone1() {
        return phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public String getInformation() {
        return information;
    }

    public String getDateCreate() {
        return dateCreate;
    }

    public String getDateEdit() {
        return dateEdit;
    }

    public String getAuthorityId() {
        return authorityId;
    }
}
