package com.icche.aisdatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> ID;
    ArrayList<String> Title;
    ArrayList<String> Message;
    ArrayList<String> Time;


    public ListAdapter(
            Context context2,
            ArrayList<String> id,
            ArrayList<String> title,
            ArrayList<String> message,
            ArrayList<String> time
    ) {

        this.context = context2;
        this.ID = id;
        this.Title = title;
        this.Message = message;
        this.Time = time;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return ID.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int position, View child, ViewGroup parent) {

        Holder holder;

        LayoutInflater layoutInflater;

        if (child == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            child = layoutInflater.inflate(R.layout.items, null);

            holder = new Holder();

            holder.ID_TextView = (TextView) child.findViewById(R.id.textViewID);
            holder.Title_TextView = (TextView) child.findViewById(R.id.textViewTITLE);
            holder.MessageTextView = (TextView) child.findViewById(R.id.textViewMESSAGE);
            holder.TimeTextView = (TextView) child.findViewById(R.id.textViewTIME);

            child.setTag(holder);

        } else {

            holder = (Holder) child.getTag();
        }
        holder.ID_TextView.setText(ID.get(position));
        holder.Title_TextView.setText(Title.get(position));
        holder.MessageTextView.setText(Message.get(position));
        holder.TimeTextView.setText(Time.get(position));
        return child;
    }

    public class Holder {

        TextView ID_TextView;
        TextView Title_TextView;
        TextView MessageTextView;
        TextView TimeTextView;
    }

}