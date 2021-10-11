package com.example.cardapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cardapp.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TicketAdapter extends ArrayAdapter<Ticket> {

    public TicketAdapter(Context context, ArrayList<Ticket> ticketArrayList) {
        super(context,0, ticketArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ticket ticket = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.available_bus_list, parent,false);
        }

        TextView shareScId = convertView.findViewById(R.id.shareScId);
        TextView departingTime = convertView.findViewById(R.id.departingTime);
        TextView price = convertView.findViewById(R.id.price);

        shareScId.setText(ticket.getBusName());
        departingTime.setText("Travel date : " + ticket.getTravelDate());
        price.setText("Price : " + Double.toString(ticket.getTicketPrice()) + "ETB");

        return convertView;
    }
}
