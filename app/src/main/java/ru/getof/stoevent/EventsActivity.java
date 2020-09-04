package ru.getof.stoevent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.getof.stoevent.Adapters.EventsAdapter;
import ru.getof.stoevent.Controllers.SwipeController;
import ru.getof.stoevent.Controllers.SwipeControllerActions;
import ru.getof.stoevent.Model.ClientsForRV;
import ru.getof.stoevent.Model.EventModel;
import ru.getof.stoevent.Model.EventModelSort;
@Keep
public class EventsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomSheetTimePickerDialog.OnTimeSetListener {

    public static final String SAVED_PUSH = "PUSH";
    private static final String TAG = "Events";
    private FloatingActionButton fabAddEvent;
    private String dateEvent = null, timeEvent = null;
    private String userProf, userEmail, idSto;
    private ArrayList<ClientsForRV> clientsForRVList;
    private ArrayList<EventModel> eventModelList;
    private ArrayList<EventModelSort> eventModelListSort;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EventsAdapter eventsAdapter;
    private RecyclerView rv;
    private CalendarView evCalendar;
    private List<EventDay> mEventDays = new ArrayList<>();
    private Calendar today,end;
    private ArrayList<String> uidEvent;


    @Override
    protected void onStart() {
        super.onStart();
        if (eventsAdapter != null){
            initBD();
            getCalEvents();
        } else {
            getCalEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventsAdapter != null){
            getCalEvents();
            eventsAdapter.notifyDataSetChanged();
        } else {
            getCalEvents();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        userProf = getIntent().getStringExtra("prof");
        userEmail = getIntent().getStringExtra("email");
        idSto = getIntent().getStringExtra("idsto");
        clientsForRVList = new ArrayList<>();
        eventModelList = new ArrayList<>();
        eventModelListSort = new ArrayList<>();
        uidEvent = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //Обработка для переключателя Push
        MenuItem itemN = navigationView.getMenu().findItem(R.id.app_bar_switch);
        SwitchCompat notifSwitch = (SwitchCompat) itemN.getActionView().findViewById(R.id.switchNotif);
        SharedPreferences setPref = getPreferences(MODE_PRIVATE);
        if (setPref.contains(SAVED_PUSH)){
            if (setPref.getString(SAVED_PUSH, "").equals("true")) notifSwitch.setChecked(true);
                else notifSwitch.setChecked(false);
        } else notifSwitch.setChecked(false);

        notifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    FirebaseMessaging.getInstance().subscribeToTopic(idSto)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Toast.makeText(EventsActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    SharedPreferences setPref = getPreferences(MODE_PRIVATE);
                                    SharedPreferences.Editor ed = setPref.edit();
                                    ed.putString(SAVED_PUSH, "true");
                                    ed.apply();
                                }
                            });
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(idSto)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        SharedPreferences setPref = getPreferences(MODE_PRIVATE);
                                        SharedPreferences.Editor ed = setPref.edit();
                                        ed.putString(SAVED_PUSH, "false");
                                        ed.apply();
                                        Toast.makeText(EventsActivity.this, "Вы отписались", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        View navHeader = navigationView.getHeaderView(0);
        TextView profTitle = navHeader.findViewById(R.id.profTitle);
        TextView emailTitle = navHeader.findViewById(R.id.emailTitle);
        profTitle.setText(userProf);
        emailTitle.setText(userEmail);

        evCalendar = findViewById(R.id.eventsCalendar);
        fabAddEvent = findViewById(R.id.fab);

        today = Calendar.getInstance();
        end = Calendar.getInstance();
        end.add(Calendar.YEAR, 2);
        try {
            evCalendar.setDate(today);
        } catch (OutOfDateRangeException e) {
            e.printStackTrace();
        }
        evCalendar.setMinimumDate(today);
        evCalendar.setMaximumDate(end);
        evCalendar.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar selectedDate = eventDay.getCalendar();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                dateEvent = df.format(selectedDate.getTime());
                initBD();
            }
        });

        if (dateEvent == null){
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            dateEvent = df.format(today.getTime());
        }

        GridTimePickerDialog grid = GridTimePickerDialog.newInstance(this,
                today.get(Calendar.HOUR_OF_DAY),
                today.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this));

        fabAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = createDialog();
                dialogFragment.show(getSupportFragmentManager(), TAG);
            }
        });

        if (eventsAdapter == null) {
            initBD();
        }

        getCalEvents();

        /*db.collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null){
                            return;
                        }
                        if (queryDocumentSnapshots != null) {
                           // Toast.makeText(getApplicationContext(),queryDocumentSnapshots.getDocumentChanges().toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                });*/


    }

    private void getCalEvents() {
        mEventDays.clear();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String dateToday = df.format(today.getTime());
        /*Date dtTodey = null;
        try {
            dtTodey=df.parse(dateToday);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        //Date finalDtTodey = dtTodey;
        db.collection("events")
                //.whereEqualTo("id_sto", idSto)
                .whereGreaterThanOrEqualTo("date", dateToday)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            SimpleDateFormat pattern = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            for (QueryDocumentSnapshot doc : task.getResult()){
                                String dateEv = doc.get("date").toString();
                                Date dtBD = null;
                                try {
                                    dtBD=pattern.parse(dateEv);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                assert dtBD != null;
                                /*if (dtBD.equals(finalDtTodey) || dtBD.after(finalDtTodey)){
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(dtBD);
                                    mEventDays.add(new EventDay(cal,R.drawable.dots_event));
                                }*/
                                if (doc.get("id_sto").toString().equals(idSto)){
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(dtBD);
                                    mEventDays.add(new EventDay(cal,R.drawable.dots_event));
                                }
                            }
                            evCalendar.setEvents(mEventDays);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void initBD(){
        if (clientsForRVList != null) {
            clientsForRVList.clear();
        }
        db.collection("clients")
                .whereEqualTo("idsto", idSto)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                clientsForRVList.add(new ClientsForRV(documentSnapshot.getId(),documentSnapshot.get("name").toString(),documentSnapshot.get("phone").toString()));
                            }
                            loadEvents(dateEvent);
                        }
                    }
                });
    }

    private DialogFragment createDialog() {
        return createDialogWithBuilders();
    }

    private DialogFragment createDialogWithBuilders() {
        BottomSheetPickerDialog.Builder builder = null;
        Calendar now = Calendar.getInstance();
        builder = new GridTimePickerDialog.Builder(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)
        );

        GridTimePickerDialog.Builder gridDialogBuilder = (GridTimePickerDialog.Builder) builder;
        gridDialogBuilder.setHeaderTextColorSelected(0xFFFFC107)
                .setHeaderTextColorUnselected(0x4AFFC107)
                .setTimeSeparatorColor(0xFF000000)
                .setHalfDayButtonColorSelected(0xFFFFC107)
                .setHalfDayButtonColorUnselected(0x4AFFC107);

        builder.setAccentColor(0xFFFFC107)
                .setBackgroundColor(0xFF607D8B)
                .setHeaderColor(0xFF455A64)
                .setHeaderTextDark(false);

        return builder.build();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_stat){
            Intent intent = new Intent(this, StatActivity.class);
            intent.putExtra("sto", idSto);
            startActivity(intent);
        }

        return true;
    }

    @Override
    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        timeEvent = DateFormat.getTimeFormat(this).format(cal.getTime());
        Intent intent = new Intent(this, EventFocusActivity.class);
        intent.putExtra("date", dateEvent);
        intent.putExtra("time", timeEvent);
        intent.putExtra("id_sto", idSto);
        intent.putExtra("flag", "0");
        startActivity(intent);
    }

    private void loadEvents(String dt){
        if (eventModelList != null) {
            eventModelList.clear();
            uidEvent.clear();
            eventModelListSort.clear();
        }
        db.collection("events")
                .whereEqualTo("date", dt)
                .whereEqualTo("id_sto", idSto)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()){
                                eventModelList.add(document.toObject(EventModel.class));
                                uidEvent.add(document.getId());
                                eventModelListSort.add(new EventModelSort(uidEvent.get(i),
                                        eventModelList.get(i).getClientId(),
                                        eventModelList.get(i).getId_sto(),
                                        eventModelList.get(i).getDate(),
                                        eventModelList.get(i).getTime(),
                                        eventModelList.get(i).getDesc(),
                                        eventModelList.get(i).getPrice(),
                                        eventModelList.get(i).getCosts(),
                                        eventModelList.get(i).getImg_url()
                                        ));
                                i++;
                            }
                            Collections.sort(eventModelListSort, EventModelSort::compareTo);
                            putEventsInRV(eventModelListSort,uidEvent);
                        }
                    }
                });

    }

    private void putEventsInRV(ArrayList<EventModelSort> ev, ArrayList<String> uidEvent) {
       //Collections.sort(ev, EventModelSort::compareTo);
        rv = findViewById(R.id.rvEvents);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        rv.setLayoutManager(layout);

        eventsAdapter = new EventsAdapter(this,ev,clientsForRVList);
        rv.setAdapter(eventsAdapter);

        if (eventsAdapter != null){
            eventsAdapter.setOnItemClickListener(new EventsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, int position) {
                    EventModelSort evPut = ev.get(position);
                    Intent intent = new Intent(getApplicationContext(), EventFocusActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("class", evPut);
                    intent.putExtras(bundle);
                    intent.putExtra("flag", "1");
                    //intent.putExtra("uid_event", uidEvent.get(position));
                    startActivity(intent);

                }
            });
            eventsAdapter.notifyDataSetChanged();
        }
    }

}
