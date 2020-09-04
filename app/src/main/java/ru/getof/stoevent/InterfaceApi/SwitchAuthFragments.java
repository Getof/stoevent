package ru.getof.stoevent.InterfaceApi;

public interface SwitchAuthFragments {

    void PressButtonSignIn(String userEmail, String userPass);
    void PressButtonGetAcc();
    void PressButtonRegAcc(String userProf, String userPhone, String userEmail, String userPass, String idSto);

}
