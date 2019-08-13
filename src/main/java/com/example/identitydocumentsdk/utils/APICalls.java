package com.example.identitydocumentsdk.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class APICalls {

    public String getIdDetails(String url, String IDNumber) {
        String stringUrl = url;
        String IDno = IDNumber;
        String inputLine;

        try {
            URL object=new URL(stringUrl);

            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestMethod("POST");
            DataOutputStream localDataOutputStream = new DataOutputStream(con.getOutputStream());

            localDataOutputStream.writeBytes(IDno);
            localDataOutputStream.flush();
            localDataOutputStream.close();

            int responseCode = con.getResponseCode();

            Log.e("responseCode", " responseCode =" + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();


            return response.toString();
        }
        catch (Exception e){
            Log.v("ErrorAPP",e.toString());
        }
        return "";
    }






}
