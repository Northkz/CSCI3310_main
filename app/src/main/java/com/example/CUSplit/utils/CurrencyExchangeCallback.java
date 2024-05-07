package com.example.CUSplit.utils;

public interface CurrencyExchangeCallback {
    void onResult(double rate);

    void onError(Exception e);
}
