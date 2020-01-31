import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.Date;

public class Exec {

    private static final String TEMPERATURE_TOPIC = "temperature";
    private static final String HUMIDITY_TOPIC = "humidity";

    public static void main(String[] args) throws Exception {
        File serialFile = getActivePortFile();

        if (serialFile == null) {
            System.out.println("Cannot find serial input.");
            System.exit(1);
        }

        String[] topics = new String[]{TEMPERATURE_TOPIC, HUMIDITY_TOPIC};
        MQTTConnection mqttConnection = new MQTTConnection(args[0], Integer.parseInt(args[1]), args[2], args[3], topics);
        SerialReader serialReader = new SerialReader(serialFile);
        Gson gson = new GsonBuilder().serializeNulls().create();

        MQTTListener mqttListener = new MQTTListener() {
            @Override
            public void onConnectionStateChange(boolean connected) {
                System.out.println("MQTT connection is now " + (connected ? "UP" : "DOWN") + ".");
            }

            @Override
            public void onConnectionFailure(Throwable reason) {
                System.out.println("MQTT connection failure: ”" + reason.getMessage() + "”.");
            }

            @Override
            public void onPublicationSuccess(String topic, Object message) {
                System.out.println("Published value ”" + message + "” on topic ”" + topic + "”.");
            }

            @Override
            public void onPublicationQueued(String topic, Object message, Throwable reason) {
                System.out.println("Queued publication for value ”" + message + "” on topic ”" + topic + "” : " + reason.getMessage());
            }

            @Override
            public boolean shouldEnqueue(String topic, Object message) {
                if (!(message instanceof JsonObject)) {
                    return false;
                }
                int value = ((JsonObject) message).get("value").getAsInt();
                switch (topic) {
                    case TEMPERATURE_TOPIC:
                        return (value <= 15 || value > 25);
                    case HUMIDITY_TOPIC:
                        return (value > 40);
                    default:
                        return false;
                }
            }
        };

        SerialListener serialListener = new SerialListener() {

            Response previous = null;
            Response response = null;

            @Override
            public void next(String data) {
                if (data.startsWith("{") && data.endsWith("}")) {
                    response = gson.fromJson(data, Response.class);
                    if (response.isSuccess()) {
                        if (previous == null || response.getTemperature() != previous.getTemperature()) {
                            mqttConnection.publish(TEMPERATURE_TOPIC, toJson(response.getTemperature(), response.getDate()));
                        } else {
                            System.out.println("Skipped value " + response.getTemperature() + " for topic " + TEMPERATURE_TOPIC + ".");
                        }
                        if (previous == null || response.getHumidity() != previous.getHumidity()) {
                            mqttConnection.publish(HUMIDITY_TOPIC, toJson(response.getHumidity(), response.getDate()));
                        } else {
                            System.out.println("Skipped value " + response.getHumidity() + " for topic " + HUMIDITY_TOPIC + ".");
                        }
                        previous = gson.fromJson(gson.toJson(response), Response.class);
                    } else {
                        System.out.println("Failure response from serial: ”" + response.getError() + "”");
                    }
                } else {
                    System.out.println("Reading error: ”" + data + "”");
                }
            }

            @Override
            public void onFinish() {
                System.out.println("Serial has been disconnected.");
                System.out.println("Closing MQTT connection...");
                mqttConnection.close();
                System.exit(0);
            }
        };

        try {
            mqttConnection.connect(mqttListener);
            serialReader.read(serialListener);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static File getActivePortFile() {
        File dev = new File("/dev");
        File[] files = dev.listFiles();

        if (files == null) {
            return null;
        }

        for (File file: files) {
            if (file.getName().startsWith("cu.usbmodem")) {
                return file;
            }
        }

        return null;
    }

    private static JsonObject toJson(Number value, Date date) {
        JsonObject object = new JsonObject();
        object.addProperty("value", value);
        object.addProperty("date", date.toString());
        return object;
    }
}
