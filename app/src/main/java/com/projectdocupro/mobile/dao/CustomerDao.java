package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.CustomersModel;

import java.util.List;

@Dao
public interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CustomersModel customersModel);

    @Query("DELETE FROM customersmodel")
    void deleteAll();

    @Query("SELECT * from customersmodel ORDER BY customerId ASC")
    LiveData<List<CustomersModel>> getCustomersList();

    @Query("SELECT * from customersmodel")
    int getCustomersCount();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(CustomersModel customersModel);

}
