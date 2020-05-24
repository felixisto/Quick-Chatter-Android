package com.office.quickchatter.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.office.quickchatter.R;
import com.office.quickchatter.network.bluetooth.basic.BEClient;

public class BEClientsListAdapter extends BaseAdapter {
    private final @NonNull LayoutInflater inflater;
    private final @NonNull AdapterData<BEClient> data;

    public BEClientsListAdapter(@NonNull LayoutInflater inflater, @NonNull AdapterData<BEClient> data) {
        this.inflater = inflater;
        this.data = data.copy();
    }

    @Override
    public int getCount() {
        return data.count();
    }

    @Override
    public Object getItem(int position) {
        return data.getValue(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_bluetoothclient, parent, false);
        }

        BEClient model = data.getValue(position);

        TextView titleView = convertView.findViewById(R.id.title);
        titleView.setText(model.getName());

        return convertView;
    }
}
