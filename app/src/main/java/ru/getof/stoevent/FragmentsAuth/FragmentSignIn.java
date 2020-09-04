package ru.getof.stoevent.FragmentsAuth;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import ru.getof.stoevent.InterfaceApi.SwitchAuthFragments;
import ru.getof.stoevent.R;

public class FragmentSignIn extends Fragment {

    private SwitchAuthFragments switchAuthFragments;

    private EditText textEmail, textPass;
    private TextInputLayout layoutEmail, layoutPass;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);



        Button btnSignIn = view.findViewById(R.id.buttonSignIn);
        Button btnGetAcc = view.findViewById(R.id.buttonGetAcc);
        textEmail = view.findViewById(R.id.textEmail);
        textPass = view.findViewById(R.id.textPass);
        layoutEmail = view.findViewById(R.id.email_enter);
        layoutPass = view.findViewById(R.id.pass_enter);


        btnGetAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchAuthFragments.PressButtonGetAcc();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchAuthFragments.PressButtonSignIn(textEmail.getText().toString(),textPass.getText().toString());
            }
        });
        return view;
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
