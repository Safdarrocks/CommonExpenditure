package com.example.mohammadali.commonexpenditure;

/**
 * Created by mohammadali on 9/22/17.
 */

public class ExpenseDetails {
    private String mExpense;
    private String mCost;
    private String mName;
    private String mDateTime;
    private String mDay;
    private String mIsGift;

    public ExpenseDetails(){

    }

    public ExpenseDetails(String expense, String cost, String name, String dateTime, String isGift){
        mExpense = expense;
        mCost = cost;
        mName = name;
        mDateTime = dateTime;
        mIsGift = isGift;
    }

    public String getExpense() {
        return mExpense;
    }

    public String getCost() {
        return mCost;
    }

    public String getName() {
        return mName;
    }

    public String getDateTime() {
        return mDateTime;
    }

    public String getDay(){
        return mDay;
    }

    public String getIsGift(){return mIsGift;}

    public void setDay(String day){
        this.mDay = day;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setCost(String cost) {
        this.mCost = cost;
    }

    public void setExpense(String expense) {
        this.mExpense = expense;
    }

    public void setDateTime(String mDateTime) {
        this.mDateTime = mDateTime;
    }

    public void setIsGift(String isGift){
        this.mIsGift = isGift;
    }
}
