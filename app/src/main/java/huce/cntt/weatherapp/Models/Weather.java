package huce.cntt.weatherapp.Models;

public class Weather {
    private String date;
    private String status;
    private String img;
    private String max;
    private String min;

    public Weather(String date, String status, String img, String max, String min) {
        this.date = date;
        this.status = status;
        this.img = img;
        this.max = max;
        this.min = min;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }
}
