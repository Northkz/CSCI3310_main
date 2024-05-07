package com.example.CUSplit.balance;

import java.math.BigDecimal;

public class Balance {
    String userId;
    BigDecimal balance;
    String BalanceOwner;
    public Balance(BigDecimal amount , String memberId, String BalanceOwner) {
        this.userId = memberId;
        this.balance = amount;
        this.BalanceOwner = BalanceOwner;
    }


    // Getters and setters
    public String getMemberId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return balance;
    }

    public void setMemberId(String memberId) {
        this.userId = memberId;
    }

    public void setAmount(BigDecimal amount) {
        this.balance = amount;
    }
}
