package ru.getof.stoevent.FragmentsAuth;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ru.getof.stoevent.InterfaceApi.SwitchAuthFragments;
import ru.getof.stoevent.Model.StoModel;
import ru.getof.stoevent.R;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FragmentSignUp extends Fragment {

    private Spinner spinner;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<String> dataSpinner = new ArrayList<>();
    private ArrayList<String> dataSto = new ArrayList<>();
    private EditText textUpEmail, textUpPass, textUpPhone, textUpProf;
    private TextInputLayout layoutUpEmail, layoutUpPass, layoutUpPhone, layoutUpProf;
    private SwitchAuthFragments switchAuthFragments;
    private List<StoModel> stoModel = new ArrayList<>();
    private StoModel stoModelSingle;
    private List<String> idSto = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        Button btnSignUp = view.findViewById(R.id.buttonSignUp);
        spinner = view.findViewById(R.id.spinner);
        textUpEmail = view.findViewById(R.id.textUpEmail);
        textUpPass = view.findViewById(R.id.textUpPass);
        textUpPhone = view.findViewById(R.id.textUpPhone);
        textUpProf = view.findViewById(R.id.textProf);
        layoutUpEmail = view.findViewById(R.id.email_up_enter);
        layoutUpPass = view.findViewById(R.id.pass_up_enter);
        layoutUpPhone = view.findViewById(R.id.phone_up_enter);
        layoutUpProf = view.findViewById(R.id.prof_enter);

        MaskImpl mask = MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER);
        FormatWatcher watcher = new MaskFormatWatcher(mask);
        watcher.installOn(textUpPhone);



       /* db.collection("sto")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                dataSpinner.add(document.get("name").toString()+" - "+document.get("gorod").toString());
                                dataSto.add(document.get("name").toString());
                                setSpinner(dataSpinner);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });*/

       db.collection("sto")
               .get()
               .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       if (task.isSuccessful()){
                           for (QueryDocumentSnapshot document : task.getResult()) {
                               stoModelSingle = document.toObject(StoModel.class);
                               idSto.add(document.getId());
                               stoModel.add(stoModelSingle);
                               dataSpinner.add(stoModelSingle.getName()+" - "+stoModelSingle.getGorod());
                           }
                           setSpinner(dataSpinner);
                       } else {
                           Log.w(TAG, "Error getting documents.", task.getException());
                       }
                   }
               });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidForm()){
                    switchAuthFragments.PressButtonRegAcc(textUpProf.getText().toString(),
                            textUpPhone.getText().toString(),
                            textUpEmail.getText().toString(),
                            textUpPass.getText().toString(),
                            idSto.get(spinner.getSelectedItemPosition()));
                }

            }
        });



        return view;
    }

    private void setSpinner(List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, data);
        spinner.setAdapter(adapter);
    }

    private Boolean isValidForm(){
        boolean isValid = true;

        String prof = textUpProf.getText().toString();
        final String phone = textUpPhone.getText().toString();
        String email = textUpEmail.getText().toString();
        String pass = textUpPass.getText().toString();

        //ValidateProf
        if (prof.isEmpty()){
            layoutUpProf.setErrorEnabled(true);
            layoutUpProf.setError("Укажите должность!");
            isValid = false;
        } else {
            layoutUpProf.setErrorEnabled(false);
        }

        //ValidatePhone
        if (phone.isEmpty()){
            layoutUpPhone.setErrorEnabled(true);
            layoutUpPhone.setError("Укажите номер телефона!");
            isValid = false;
        } else {
            layoutUpPhone.setErrorEnabled(false);
        }

        //ValidateEmail
        if (!isValidEmail(email)){
            layoutUpEmail.setErrorEnabled(true);
            layoutUpEmail.setError("Не верный E-mail!");
            isValid = false;
        } else {
            layoutUpEmail.setErrorEnabled(false);
        }

        //ValidatePass
        if (pass.isEmpty() || (pass.length()<6)){
            layoutUpPass.setErrorEnabled(true);
            layoutUpPass.setError("Укажите пароль!");
            isValid = false;
        } else {
            layoutUpPass.setErrorEnabled(false);
        }

        //ValidateAcc
        boolean isVal = false;
        for (int i=0; i<stoModel.get(spinner.getSelectedItemPosition()).getPhone().size(); i++){
            if (stoModel.get(spinner.getSelectedItemPosition()).getPhone().get(i).equals(phone)) {
                isVal = true;
            }
        }
        if (!isVal) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Внимание");
            alertDialog.setMessage("Вашего телефона нет в списке\nдоверенных лиц данной организации.\n" +
                    "Регистрация не возможна!");
            alertDialog.setIcon(R.drawable.ic_error_outline_black_24dp);
            alertDialog.show();
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String target) {
        return target != null && target.matches("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\\b");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SwitchAuthFragments) {
            switchAuthFragments = (SwitchAuthFragments) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }
}
