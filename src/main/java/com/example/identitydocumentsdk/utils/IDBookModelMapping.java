package com.example.identitydocumentsdk.utils;

import com.example.identitydocumentsdk.models.IDModel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class IDBookModelMapping {

    public IDModel MapCardData (String data , String docType) {
        JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();

        IDModel bookDataModel = new IDModel();
        bookDataModel.Surname = jsonObject.get("Surname").toString().replaceAll("^\"|\"$", "");
        bookDataModel.GivenNames = jsonObject.get("GivenNames").toString().replaceAll("^\"|\"$", "");
        bookDataModel.Names = jsonObject.get("GivenNames").toString().replaceAll("^\"|\"$", "");
        bookDataModel.Sex = jsonObject.get("Sex").toString().replaceAll("^\"|\"$", "");
        bookDataModel.Nationality = jsonObject.get("Nationality").toString().replaceAll("^\"|\"$", "");
        bookDataModel.DocumentNumber = jsonObject.get("DocumentNumber").toString().replaceAll("^\"|\"$", "");
        bookDataModel.DateOfBirth = jsonObject.get("DateOfBirth").toString().replaceAll("^\"|\"$", "");
        bookDataModel.CountryOfBirth = jsonObject.get("Nationality").toString().replaceAll("^\"|\"$", "");
        bookDataModel.ResidenceStatus = jsonObject.get("Nationality").toString().replaceAll("^\"|\"$", "");
        bookDataModel.DateOfIssue = jsonObject.get("DateOfIssue").toString().replaceAll("^\"|\"$", "");
        bookDataModel.RSACode = jsonObject.get("RSACode").toString().replaceAll("^\"|\"$", "");
        bookDataModel.CardNo = jsonObject.get("DocumentNumber").toString().replaceAll("^\"|\"$", "");
        bookDataModel.IdNumber = jsonObject.get("DocumentNumber").toString().replaceAll("^\"|\"$", "");
        bookDataModel.DocType = docType;

        return bookDataModel;
    }
}