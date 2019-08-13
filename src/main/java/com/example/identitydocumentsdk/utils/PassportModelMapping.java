package com.example.identitydocumentsdk.utils;

import com.example.identitydocumentsdk.models.IDModel;

public class PassportModelMapping {

    public IDModel MapCardData(String data, String docType) {

        IDModel cardDataModel = new IDModel();

        String split2 = "";
        String names = "";

        if (data.length() >= 88) {
            cardDataModel.DocType = data.substring(0, 2);
            cardDataModel.CountryOfBirth = data.substring(2, 5);
            cardDataModel.Nationality = data.substring(2, 5);
            cardDataModel.Surname = data.substring(5, data.indexOf("<"));
            names = data.substring(data.indexOf("<") + 2, 44);
            String[] nameArray = names.split("\\<");
            if (nameArray.length > 0) {
                for (String name : nameArray) {
                    cardDataModel.GivenNames = cardDataModel.GivenNames + " " + name;
                    cardDataModel.Names = cardDataModel.Names + " " + name;
                }
            }
            split2 = data.substring(44, 88);
            cardDataModel.DocumentNumber = split2.substring(0, 10);
            cardDataModel.DateOfBirth = split2.substring(13, 19);
            cardDataModel.Sex = split2.substring(20, 21);
//            cardDataModel.expiryDate = getExpiryDate(split2.substring(21, 27));
            cardDataModel.IdNumber = split2.substring(28, 41);
        }
        return cardDataModel;
    }

    private String getExpiryDate(String date) {
        String result = "";
        String year = "20";
        String month = "";
        String day = "";

        if (null != date && date.length() >= 6) {
            year = year + date.substring(0, 2);
            month = date.substring(2, 4);
            day = date.substring(4, 6);
            result = year + "/" + month + "/" + day;
        }

        return result;
    }


}