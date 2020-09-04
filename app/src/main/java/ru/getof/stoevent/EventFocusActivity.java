package ru.getof.stoevent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
import com.bumptech.glide.Glide;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.ImageQuality;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import ru.getof.stoevent.Adapters.GaleryAdapter;
import ru.getof.stoevent.Model.ClientsModel;
import ru.getof.stoevent.Model.EventModelSort;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

@Keep
public class EventFocusActivity extends AppCompatActivity implements BottomSheetTimePickerDialog.OnTimeSetListener {

    private static final String TAG = "EventFocus";
    private Toolbar toolbarEventFocus;
    private Menu evFocusMenu;
    private EditText nameClient, phoneClient, descEvent, priceEvent, costsEvent;
    private TextInputLayout nameCllayout, phoneCllayout;
    private Button btnDate, btnTime;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<ClientsModel> clientsModelList = new ArrayList<>();
    private List<String> imagesEvents = new ArrayList<>();
    private List<String> returnValue = new ArrayList<>();
    private boolean clientValid = false;
    private String idSto, dateEv, timeEv, flagActivity, uid_Event;
    private EventModelSort eventModel, editModel;
    private RecyclerView rvImages;
    private GaleryAdapter galeryAdapter;
    private Options options;
    private StorageReference mStorageRef;
    private ProgressBar progressBar;

    private OnSelectDateListener listener = new OnSelectDateListener() {
        @Override
        public void onSelect(List<Calendar> calendar) {
            Calendar selectedDate = calendar.get(0);
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            btnDate.setText(df.format(selectedDate.getTime()));
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_focus);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        toolbarEventFocus = findViewById(R.id.toolbarEventFocus);
        toolbarEventFocus.setTitle("Визит");
        setSupportActionBar(toolbarEventFocus);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        bindElements();
        flagActivity = getIntent().getStringExtra("flag");
        assert flagActivity != null;
        if (flagActivity.equals("0")) {
            idSto = getIntent().getStringExtra("id_sto");
            dateEv = getIntent().getStringExtra("date");
            timeEv = getIntent().getStringExtra("time");
            btnDate.setText(dateEv);
            btnTime.setText(timeEv);
            imagesEvents.clear();
            loadClients(idSto);
            initImages();
        } else {
            if (flagActivity.equals("1")){
                eventModel = (EventModelSort) Objects.requireNonNull(getIntent().getExtras()).getSerializable("class");
                //imagesEvents = eventModel.getImg_url();
                //uid_Event = getIntent().getStringExtra("uid_event");
                if (eventModel != null) {
                    uid_Event = eventModel.getuID();
                }
                DocumentReference docRef = db.collection("clients").document(eventModel.getClientId());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            setElements(eventModel, doc.get("name").toString(), doc.get("phone").toString());
                        }
                    }
                });

            }
        }

        DatePickerBuilder builder = new DatePickerBuilder(this, listener)
                    .setPickerType(CalendarView.ONE_DAY_PICKER);

        DatePicker datePicker = builder.build();

        Calendar today = Calendar.getInstance();

        GridTimePickerDialog grid = GridTimePickerDialog.newInstance(this,
                today.get(Calendar.HOUR_OF_DAY),
                today.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this));

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = createDialog();
                dialogFragment.show(getSupportFragmentManager(), TAG);
            }
        });


    }

    private DialogFragment createDialog() {
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


    private void initImages() {
        galeryAdapter = new GaleryAdapter(this, imagesEvents);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rvImages.setLayoutManager(layoutManager);
        rvImages.setAdapter(galeryAdapter);
        galeryAdapter.setOnItemClickListener((itemView, position) -> {
            if (itemView == R.id.layout_close) {
                imagesEvents.remove(position);
                galeryAdapter.notifyDataSetChanged();
            } else {
                if (itemView == R.id.layout_image__add){
                    options = Options.init()
                            .setRequestCode(100)                                                 //Request code for activity results
                            .setCount(3)                                                         //Number of images to restict selection count
                            .setFrontfacing(false)                                                //Front Facing camera on start
                            .setImageQuality(ImageQuality.HIGH)                                 //Image Quality
                            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT);           //Orientaion
                    Pix.start(EventFocusActivity.this, options);
                } else {
                    if (itemView == R.id.imagePhoto){
                        new StfalconImageViewer.Builder<>(this, imagesEvents, new ImageLoader<String>() {
                            @Override
                            public void loadImage(ImageView imageView, String image) {
                                Glide.with(getApplicationContext())
                                        .load(image)
                                        .into(imageView);
                            }
                        })
                                .withStartPosition(position)
                                .withHiddenStatusBar(false)
                                .show();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            assert data != null;
            returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            assert returnValue != null;
            if (imagesEvents.size() < 3 && returnValue.size() != 0){
                    uploadPhoto(returnValue.get(0));
            }
        }
    }

    private void uploadPhoto(String value) {
        progressBar.setVisibility(View.VISIBLE);
            Uri file = Uri.fromFile(new File(value));
            StorageReference riversRev = mStorageRef.child(idSto+"/"+btnDate.getText().toString()+"/"+file.getLastPathSegment());
            riversRev.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    riversRev.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imagesEvents.add(uri.toString());
                            galeryAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(EventFocusActivity.this, options);
                } else {
                    Toast.makeText(EventFocusActivity.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void setElements(EventModelSort model, String clName, String clPhone) {
        nameClient.setText(clName);
        nameClient.setEnabled(false);
        phoneClient.setText(clPhone);
        phoneClient.setEnabled(false);
        btnDate.setText(model.getDate());
        btnTime.setText(model.getTime());
        descEvent.setText(model.getDesc());
        priceEvent.setText(model.getPrice());
        costsEvent.setText(model.getCosts());
        idSto = model.getId_sto();
        imagesEvents = model.getImg_url();

        loadClients(idSto);
        initImages();
    }

    private void loadClients(String id_sto) {
        db.collection("clients")
                .whereEqualTo("idsto", id_sto)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                clientsModelList.add(document.toObject(ClientsModel.class));
                            }

                        }

                    }
                });
    }

    private void bindElements() {
        nameClient = findViewById(R.id.textInputClient);
        phoneClient = findViewById(R.id.textPhoneClient);
        nameCllayout = findViewById(R.id.enterClientLayout);
        phoneCllayout = findViewById(R.id.enterPhoneClientLayout);
        descEvent = findViewById(R.id.textDesc);
        priceEvent = findViewById(R.id.textPrice);
        costsEvent = findViewById(R.id.textCost);

        btnDate = findViewById(R.id.buttonDate);
        btnTime = findViewById(R.id.buttonTime);

        rvImages = findViewById(R.id.rv_images);

        progressBar = findViewById(R.id.progressBar);


        MaskImpl mask = MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER);
        FormatWatcher watcher = new MaskFormatWatcher(mask);
        watcher.installOn(phoneClient);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.evFocusMenu = menu;
        getMenuInflater().inflate(R.menu.event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            case R.id.action_done:
                if (flagActivity.equals("0")){
                    addEvent();
                } else {
                    if (flagActivity.equals("1")){
                        upduteEvent();
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void upduteEvent() {
        Map<String, Object> eventUp = new HashMap<>();
        eventUp.put("date", btnDate.getText().toString());
        eventUp.put("time", btnTime.getText().toString());
        eventUp.put("desc", descEvent.getText().toString());
        eventUp.put("price", priceEvent.getText().toString());
        eventUp.put("costs", costsEvent.getText().toString());
        eventUp.put("img_url", imagesEvents);
        db.collection("events").document(uid_Event)
                .update(eventUp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"Успешно!",Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Ошибка записи данных",Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addEvent() {

        if (isValidFormEvent()){
            String textCl = nameClient.getText().toString();
            String phoneCl = phoneClient.getText().toString();
            textCl = textCl.substring(0,1).toUpperCase()+textCl.substring(1).toLowerCase();
            for (ClientsModel clientsModel : clientsModelList){
                if (clientsModel.getName().toLowerCase().equals(textCl.toLowerCase()) && clientsModel.getPhone().equals(phoneCl)){
                    clientValid = true;
                }
            }

            if (clientValid) {
                final String[] uid_cl = new String[1];
                db.collection("clients")
                        .whereEqualTo("name", textCl)
                        .whereEqualTo("phone", phoneCl)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot doc : task.getResult()){
                                        uid_cl[0] = doc.getId();
                                    }
                                    addInDB(uid_cl[0]);
                                }
                            }
                        });
            } else {
                Map<String, Object> client = new HashMap<>();
                client.put("idsto", idSto);
                client.put("name", textCl);
                client.put("phone", phoneCl);
                db.collection("clients")
                        .add(client)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                addInDB(documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

            }

        }

    }

    private Boolean isValidFormEvent(){
        boolean isValid = true;

        if (nameClient.getText().toString().isEmpty()){
            nameCllayout.setErrorEnabled(true);
            nameCllayout.setError("Укажите клиента!");
            isValid = false;
        } else {
            nameCllayout.setErrorEnabled(false);
        }

        if (!isValidPhone(phoneClient.getText().toString())){
            phoneCllayout.setErrorEnabled(true);
            phoneCllayout.setError("Номер слишком короткий!");
            isValid = false;
        } else {
            phoneCllayout.setErrorEnabled(false);
        }

        return isValid;
    }

    private boolean isValidPhone(String target) {
        return target != null && target.matches("((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}");
    }

    private void addInDB(String uid_client){
        Map<String, Object> event = new HashMap<>();
        event.put("clientId", uid_client);
        event.put("id_sto", idSto);
        event.put("date", btnDate.getText().toString());
        event.put("time", btnTime.getText().toString());
        event.put("desc", descEvent.getText().toString());
        event.put("price", priceEvent.getText().toString());
        event.put("costs", costsEvent.getText().toString());
        event.put("img_url", imagesEvents);
        db.collection("events")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(),"Успешно!",Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Ошибка записи данных",Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        btnTime.setText(DateFormat.getTimeFormat(this).format(cal.getTime()));
    }
}
