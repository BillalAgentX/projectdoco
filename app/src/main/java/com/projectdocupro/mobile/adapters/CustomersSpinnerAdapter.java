package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.projectdocupro.mobile.R;

import java.util.List;

public class CustomersSpinnerAdapter extends ArrayAdapter<String> {

    LayoutInflater flater;
    private List<String>    data;

    public CustomersSpinnerAdapter(@NonNull Context context, int   convertView, @NonNull List<String> objects) {
        super(context,R.layout.customers_spinner_items_layout,R.id.customer_title,objects);
        flater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        data    =   objects;
        Log.d("row Title","init");
    }

    @Override
    public int getCount() {
        return data.size();
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

        View convertView = flater.inflate(R.layout.customers_spinner_items_layout,null,true);

        ((TextView) convertView).setText(data.get(position));
        return convertView;
    }


    //    @Override
//    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        CustomersModel rowItem = data.get(position);
//        Log.d("row Title",rowItem.getTitle());
//
//        if (convertView==null){
//            convertView = flater.inflate(R.layout.customers_spinner_items_layout,null,true);
//        }
//        TextView txtTitle = (TextView) convertView.findViewById(R.id.customer_title);
//        txtTitle.setText(rowItem.getTitle());
//
//        return convertView;
//    }

}
