package ru.getof.stoevent.Custom;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class DateAxisValueFormatter extends ValueFormatter {

    private ArrayList<String> labelsDate;


    public DateAxisValueFormatter(ArrayList<String> labelsDate) {
        this.labelsDate = labelsDate;
    }

    @Override
    public String getFormattedValue(float value) {
        //return labelsDate.get((int) value);
        return labelsDate.get((int) value);
    }


}
