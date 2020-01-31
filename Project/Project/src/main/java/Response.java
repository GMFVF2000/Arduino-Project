import java.util.Date;

public class Response {

    private boolean status;
    private String error;
    private int temperature;
    private int humidity;
    private Date date;

    Response() {
        date = new Date();
    }

    public boolean isSuccess() {
        return status;
    }

    public String getError() {
        return error;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", date=" + date +
                '}';
    }
}

