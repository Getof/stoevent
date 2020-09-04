package ru.getof.stoevent.Model;

import java.io.Serializable;
import java.util.List;

public class EventModel implements Serializable {

    private String clientId;
    private String id_sto;
    private String date;
    private String time;
    private String desc;
    private String price;
    private String costs;
    private List<String> img_url;

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
}
