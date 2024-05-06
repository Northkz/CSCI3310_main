package com.example.split_bill.utils;

public interface CurrencyExchangeCallback {
    void onResult(double rate);

    void onError(Exception e);
}
