package com.example.identitydocumentsdk.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Arrays;

public class IDModel  {

    public String Surname = "";
    public String Names = "";
    public String GivenNames = "";
    public String Sex = "";
    public String Nationality = "";
    public String IdNumber = "";
    public String DocumentNumber = "";
    public String DateOfBirth = "";
    public String CountryOfBirth = "";
    public String ResidenceStatus = "";
    public String DateOfIssue = "";
    public String RSACode = "";
    public String CardNo = "";
    public Bitmap PortraitImage;
    public Bitmap DocumentImage;
    public Bitmap BackImage;
    public String PersonalNumber = "";
    public String DocType = "";

    public IDModel(){}

    public IDModel(String surname, String names, String givenNames, String sex, String nationality, String idNumber, String documentNumber, String dateOfBirth, String countryOfBirth, String residenceStatus, String dateOfIssue, String RSACode, String cardNo, Bitmap portraitImage, Bitmap documentImage, Bitmap backImage, String personalNumber, String docType) {
        Surname = surname;
        Names = names;
        GivenNames = givenNames;
        Sex = sex;
        Nationality = nationality;
        IdNumber = idNumber;
        DocumentNumber = documentNumber;
        DateOfBirth = dateOfBirth;
        CountryOfBirth = countryOfBirth;
        ResidenceStatus = residenceStatus;
        DateOfIssue = dateOfIssue;
        this.RSACode = RSACode;
        CardNo = cardNo;
        PortraitImage = portraitImage;
        DocumentImage = documentImage;
        BackImage = backImage;
        PersonalNumber = personalNumber;
        DocType = docType;
    }

    @Override
    public String toString() {
        return "IDModel{" +
                "Surname='" + Surname + '\'' +
                ", Names='" + Names + '\'' +
                ", GivenNames='" + GivenNames + '\'' +
                ", Sex='" + Sex + '\'' +
                ", Nationality='" + Nationality + '\'' +
                ", IdNumber='" + IdNumber + '\'' +
                ", DocumentNumber='" + DocumentNumber + '\'' +
                ", DateOfBirth='" + DateOfBirth + '\'' +
                ", CountryOfBirth='" + CountryOfBirth + '\'' +
                ", ResidenceStatus='" + ResidenceStatus + '\'' +
                ", DateOfIssue='" + DateOfIssue + '\'' +
                ", RSACode='" + RSACode + '\'' +
                ", CardNo='" + CardNo + '\'' +
                ", PortraitImage=" + PortraitImage +
                ", DocumentImage=" + DocumentImage +
                ", BackImage=" + BackImage +
                ", PersonalNumber='" + PersonalNumber + '\'' +
                ", DocType='" + DocType + '\'' +
                '}';
    }
}
