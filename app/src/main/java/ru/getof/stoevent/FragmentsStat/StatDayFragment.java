package ru.getof.stoevent.FragmentsStat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.getof.stoevent.Custom.DateAxisValueFormatter;
import ru.getof.stoevent.Model.DataChartOfDay;
import ru.getof.stoevent.R;

public class StatDayFragment extends Fragment {

    private Button btnPeriodDate;
    private BarChart chart;
    private String stoID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<BarEntry> entries;
    private ArrayList<String> labels;
    private ArrayList<DataChartOfDay> ofDays = new ArrayList<>();

    private OnSelectDateListener listener = new OnSelectDateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onSelect(List<Calendar> calendar) {
            Calendar beginDate = calendar.get(0);
            Calendar endDate = calendar.get(calendar.size()-1);
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            btnPeriodDate.setText("с "+df.format(beginDate.getTime())+" по "+df.format(endDate.getTime()));
            loadDataOfBD(beginDate, endDate);
        }
    };

    private void loadDataOfBD(Calendar beginDate, Calendar endDate) {
        if (labels == null && entries == null){
            labels = new ArrayList<>();
            entries = new ArrayList<>();
        } else {
            labels.clear();
            entries.clear();
        }
        if (ofDays != null) ofDays.clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        db.collection("events")
                .whereEqualTo("id_sto", stoID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot doc : task.getResult()){
                            Date currentDt = null;
                            try {
                                currentDt = dateFormat.parse(doc.get("date").toString());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            assert currentDt != null;
                            int compBeg = currentDt.compareTo(beginDate.getTime());
                            int compEnd = currentDt.compareTo(endDate.getTime());

                            if (compBeg >= 0 && compEnd <= 0){
                                String dt = doc.get("date").toString();
                                float pr;
                                if (doc.get("price").toString().equals("")){
                                    pr = 0f;
                                } else {
                                    pr = Float.parseFloat(doc.get("price").toString());
                                    if (!doc.get("costs").toString().equals(""))
                                        pr = pr - Float.parseFloat(doc.get("costs").toString());
                                }
                                boolean flag = true;
                                if (ofDays.size() != 0){
                                    for (int i=0;i<ofDays.size();i++){
                                        if (ofDays.get(i).getDate().equals(dt)){
                                            pr = pr+ofDays.get(i).getPrice();
                                            ofDays.get(i).setPrice(pr);
                                            flag = false;
                                        }
                                    }
                                } else {
                                    ofDays.add(new DataChartOfDay(dt,pr));
                                    flag = false;
                                }
                                if (flag) ofDays.add(new DataChartOfDay(dt,pr));
                            }
                        }
                        Collections.sort(ofDays, DataChartOfDay::compareTo);
                        initChart(ofDays);
                    }
                });
    }

    private void initChart(ArrayList<DataChartOfDay> ofDays) {
        for (int i=0;i<ofDays.size();i++){
            labels.add(ofDays.get(i).getDate());
            entries.add(new BarEntry(i,ofDays.get(i).getPrice()));
        }

        ValueFormatter xAxisFormatter = new DateAxisValueFormatter(labels);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setTextColor(getResources().getColor(R.color.colorSecondaryText));
        xAxis.setValueFormatter(xAxisFormatter);

        YAxis left = chart.getAxisLeft();
        left.setDrawLabels(false); // no axis labels
        left.setDrawAxisLine(false); // no axis line
        left.setDrawGridLines(false); // no grid lines
        left.setDrawZeroLine(true); // draw a zero line
        chart.getAxisRight().setEnabled(false);


        BarDataSet set;

        if (chart.getData() != null && chart.getData().getDataSetCount() > 0){
            set = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set.setValues(entries);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        } else {
            set = new BarDataSet(entries, "Стоимость - расходы");
            set.setColors(getResources().getColor(R.color.colorAccent));
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set);

            BarData  data = new BarData(dataSets);
            data.setBarWidth(0.9f);
            data.setValueTextColor(getResources().getColor(R.color.colorPrimaryLight));
            chart.animateY(1100);
            chart.setData(data);
            chart.invalidate();
        }
    }


    public StatDayFragment(String sto) {
        this.stoID = sto;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_daystat, container, false);

        btnPeriodDate = v.findViewById(R.id.buttonPeriodChart);
        chart = v.findViewById(R.id.barChart);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);
        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);

        DatePickerBuilder builder = new DatePickerBuilder(getActivity(), listener)
                .setPickerType(CalendarView.RANGE_PICKER);
        DatePicker datePicker = builder.build();

        btnPeriodDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker.show();
            }
        });



        return v;
    }
}
