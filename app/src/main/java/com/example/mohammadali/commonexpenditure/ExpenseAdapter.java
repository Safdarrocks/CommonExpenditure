package com.example.mohammadali.commonexpenditure;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mohammadali on 9/6/17.
 */

public class ExpenseAdapter extends ArrayAdapter<ExpenseDetails> {
    ExpenseAdapter(Context context, int resource, List<ExpenseDetails> objects){
        super(context, resource, objects);
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_expense, parent, false);
        }
        TextView  expenseTextView = convertView.findViewById(R.id.expenseTextView);
        TextView costTextView = convertView.findViewById(R.id.costTextView);
        TextView dateTimeTextView = convertView.findViewById(R.id.dateTimeTextView);
        ImageView itemIcon = convertView.findViewById(R.id.itemIcon);

        ExpenseDetails expenseDetails = getItem(position);

        assert expenseDetails != null;
        expenseTextView.setText(expenseDetails.getExpense());
        costTextView.setText(expenseDetails.getCost());
        dateTimeTextView.setText(expenseDetails.getDateTime());

        String day = expenseDetails.getDay();
        switch(day){
            case "Sunday" : convertView.setBackgroundColor(Color.parseColor("#ffffbb33"));
                break;
            case "Monday" : convertView.setBackgroundColor(Color.parseColor("#ffff8800"));
                break;
            case "Tuesday" : convertView.setBackgroundColor(Color.parseColor("#ffff4444"));
                break;
            case "Wednesday" : convertView.setBackgroundColor(Color.parseColor("#ffcc0000"));
                break;
            case "Thursday" : convertView.setBackgroundColor(Color.parseColor("#ff99cc00"));
                break;
            case "Friday" : convertView.setBackgroundColor(Color.parseColor("#ff669900"));
                break;
            case "Saturday": convertView.setBackgroundColor(Color.parseColor("#ffff4081"));
                break;
        }

        String expense = expenseDetails.getExpense();
        switch (expense.toLowerCase()){
            case "breakfast": itemIcon.setBackgroundResource(R.drawable.ic_breakfast);
                break;
            case "lunch": itemIcon.setBackgroundResource(R.drawable.ic_food);
                break;
            case "snacks":itemIcon.setBackgroundResource(R.drawable.ic_snacks);
                break;
            case "dinner": itemIcon.setBackgroundResource(R.drawable.ic_food);
                break;
            case "electricity bill": itemIcon.setBackgroundResource(R.drawable.ic_electricity_bill);
                break;
            case "wifi bill": itemIcon.setBackgroundResource(R.drawable.ic_new_wifi);
                break;
            case "room rent": itemIcon.setBackgroundResource(R.drawable.ic_room_rent);
                break;
            case "vegetables": itemIcon.setBackgroundResource(R.drawable.ic_vegetables);
                break;
            case "roti and vegi": itemIcon.setBackgroundResource(R.drawable.ic_roti);
                break;
            case "roti": itemIcon.setBackgroundResource(R.drawable.ic_roti);
                break;
            case "food": itemIcon.setBackgroundResource(R.drawable.ic_food);
                break;
            case "miscellaneous": itemIcon.setBackgroundResource(R.drawable.ic_misc);
                break;
            case "water": itemIcon.setBackgroundResource(R.drawable.ic_action_name);
                break;
            case "cold drink": itemIcon.setBackgroundResource(R.drawable.ic_cold_drink);
                break;
            case "milk": itemIcon.setBackgroundResource(R.drawable.ic_milk);
                break;
            case "egg": itemIcon.setBackgroundResource(R.drawable.ic_egg);
                break;
            case "juice": itemIcon.setBackgroundResource(R.drawable.ic_cold_drink);
                break;
            default: itemIcon.setBackgroundResource(R.drawable.ic_misc);
        }
        if (expense.toLowerCase().startsWith("gift")){
            itemIcon.setBackgroundResource(R.drawable.ic_new_gift_icon);
        }
        return convertView;
    }
}
