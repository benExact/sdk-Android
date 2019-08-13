package com.example.identitydocumentsdk.utils;


import com.example.identitydocumentsdk.models.IDModel;


public class IDModelMapping {

 

    public IDModel MapCardData (String data , String docType) {
        String[] splitTestData = data.split("\\|");

        IDModel cardDataModel = new IDModel();

        cardDataModel.Surname = splitTestData[0];
        cardDataModel.GivenNames = splitTestData[1];
        cardDataModel.Sex = splitTestData[2];
        cardDataModel.Nationality = splitTestData[3];
        cardDataModel.DocumentNumber = splitTestData[4];
        cardDataModel.DateOfBirth = splitTestData[5];
        cardDataModel.CountryOfBirth = splitTestData[6];
        cardDataModel.ResidenceStatus = splitTestData[7];
        cardDataModel.DateOfIssue = splitTestData[8];
        cardDataModel.RSACode = splitTestData[9];
        cardDataModel.CardNo = splitTestData[10];
        cardDataModel.DocType = docType;

//        String serilizedData = new Gson().toJson(cardDataModel);

        return cardDataModel;
    }
}