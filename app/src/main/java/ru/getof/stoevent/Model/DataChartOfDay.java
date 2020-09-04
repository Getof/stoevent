package ru.getof.stoevent.Model;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataChartOfDay implements Comparable<DataChartOfDay> {

    private String date;
    private float price;

    public DataChartOfDay(String date, float price) {
        this.date = date;
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public int compareTo(@NotNull DataChartOfDay o) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date1 = null;
        try {
            date1 = sdf.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date2 = null;
        try {
            date2 = sdf.parse(o.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1.compareTo(date2);    }
}
