package com.example.split_bill;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class BillViewModel extends AndroidViewModel {
    private BillRepository repository;
    public LiveData<List<BillEntity>> allBills;

    BillViewModel(@NonNull Application application,String gName) {
        super(application);
        repository = new BillRepository(application,gName);
        allBills = repository.getAllBills();
    }

    public void insert(BillEntity bill) {
        repository.insert(bill);
    }

    public void update(BillEntity bill) {
        repository.update(bill);
    }

    public void delete(BillEntity bill) {
        repository.delete(bill);
    }

    public void deleteAll(String gName) {
        repository.deleteAll(gName);
    }

    public LiveData<List<BillEntity>> getAllBills() {
        return allBills;
    }

    public List<BillEntity> getAllMemberBills(String gName, int mid) {
        return repository.getAllBillsForMember(gName, mid);
    }
}
