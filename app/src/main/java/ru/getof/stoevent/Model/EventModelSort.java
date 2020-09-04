package ru.getof.stoevent.Model;

import java.io.Serializable;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventModelSort implements Serializable, Comparable<EventModelSort> {

    private String uID;
    private String clientId;
    private String id_sto;
    private String date;
    private String time;
    private String desc;
    private String price;
    private String costs;
    private List<String> img_url;

    public EventModelSort(String uID, String clientId, String id_sto, String date, String time, String desc, String price, String costs, List<String> img_url) {
        this.uID = uID;
        this.clientId = clientId;
        this.id_sto = id_sto;
        this.date = date;
        this.time = time;
        this.desc = desc;
        this.price = price;
        this.costs = costs;
        this.img_url = img_url;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public List<String> getImg_url() {
        return img_url;
    }

    public void setImg_url(List<String> img_url) {
        this.img_url = img_url;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getId_sto() {
        return id_sto;
    }

    public void setId_sto(String id_sto) {
        this.id_sto = id_sto;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCosts() {
        return costs;
    }

    public void setCosts(String costs) {
        this.costs = costs;
    }


    public boolean someEquals(EventModel someObj){
        return getClientId().equals(someObj.getClientId()) &&
                getCosts().equals(someObj.getCosts()) &&
                getDate().equals(someObj.getDate()) &&
                getTime().equals(someObj.getTime()) &&
                getPrice().equals(someObj.getPrice()) &&
                getDesc().equals(someObj.getDesc()) &&
                getId_sto().equals(someObj.getId_sto());
    }


    @Override
    public int compareTo(EventModelSort o) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
        Date date1 = null;
        try {
            date1 = sdf.parse(this.time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date2 = null;
        try {
            date2 = sdf.parse(o.time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1.compareTo(date2);
    }
}