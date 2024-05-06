package com.example.split_bill.utils;

import com.google.gson.JsonObject;

public interface ExchangeRatesCallback {
    void onCallback(JsonObject rates);

    void onError(Exception e);
}
