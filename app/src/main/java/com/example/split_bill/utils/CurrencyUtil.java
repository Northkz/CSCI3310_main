package com.example.split_bill.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurrencyUtil {

    private Context context;
    private static final String FX_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String FX_URL2 = "/latest/HKD";
    private static final String API_Key = "1631409cfa5a0f4b8237be1d";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    public CurrencyUtil(Context context) {
        this.context = context;
    }

    //Use Geocoder to get the country code based on current location
    public String getCountryCode(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getCountryCode();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Use Locale to get the Currency code based on country code
    public String getCurrencyCode(double latitude, double longitude) {
        String countryCode = getCountryCode(latitude, longitude);
        if (countryCode != null) {
            try {
                Currency currency = Currency.getInstance(new Locale("", countryCode));
                return currency.getCurrencyCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //Get exchange rates based on current location, using the ExchangeRate-API
    //https://www.exchangerate-api.com/
    public void getAllExchangeRate(double latitude, double longitude, CurrencyExchangeCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String baseCurrency = getCurrencyCode(latitude, longitude);


                URL url = new URL(FX_URL + API_Key + FX_URL2);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                //only start when the URL is connected
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                    JsonParser jp = new JsonParser();
                    JsonElement root = jp.parse(streamReader);
                    JsonObject jsonObject = root.getAsJsonObject();
                    JsonObject rates = jsonObject.getAsJsonObject("conversion_rates");

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("FXRates");
                    for (String currencyCode: rates.keySet()){
                        double myRate = rates.get(currencyCode).getAsDouble();
                        databaseReference.child(currencyCode).setValue(myRate);
                    }
                    double rateToHKD = rates.getAsJsonPrimitive(baseCurrency).getAsDouble();
                    handler.post(() -> callback.onResult(rateToHKD));
                } else {
                    throw new Exception("HTTP error code: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                handler.post(() -> callback.onError(e));
            }
        });
    }

    public static void getExchangeRateToHKD(ExchangeRatesCallback callback){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("FXRates");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JsonObject rates = new JsonObject();
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    rates.addProperty(snapshot1.getKey(), snapshot1.getValue(Double.class));
                }
                callback.onCallback(rates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception("Database error: " + error.getMessage()));
            }
        });
    }
}
