import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;

public class MQTTConnection {

    private MQTT mqtt;
    private MQTTQueue queue;
    private CallbackConnection connection;
    private MQTTListener mqttListener;
    private boolean connected;

    public MQTTConnection(String host, int port, String user, String password, String[] topics) throws Exception {
        mqtt = new MQTT();
        queue = new MQTTQueue(topics);
        mqtt.setHost(host, port);
        if (user != null && password != null) {
            mqtt.setUserName(user);
            mqtt.setPassword(password);
        }
    }

    @SuppressWarnings("deprecation")
    public void connect(MQTTListener listener) {
        connection = mqtt.callbackConnection();
        mqttListener = listener;
        connection.listener(new Listener() {
            @Override
            public void onConnected() {
                connected = true;
                listener.onConnectionStateChange(true);
                queue.publishAll(MQTTConnection.this);
            }

            @Override
            public void onDisconnected() {
                connected = false;
                listener.onConnectionStateChange(false);
            }

            @Override
            public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
                ack.run();
            }

            @Override
            public void onFailure(Throwable value) {
                connected = false;
                listener.onConnectionStateChange(false);
                listener.onConnectionFailure(value);
            }
        });
        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {

            }

            @Override
            public void onFailure(Throwable value) {
                connected = false;
                listener.onConnectionFailure(value);
            }
        });
    }

    public void publish(String topic, Object message) {
        if (!connected || connection == null) {
            enqueue(topic, message, new Exception("Connection not established"));
            return;
        }
        String finalTopic = (!topic.startsWith("/") ? "/" : "") + (topic.contains("topic") ? topic : ("topic/" + topic));
        connection.publish(
                finalTopic,
                String.valueOf(message).getBytes(),
                QoS.AT_LEAST_ONCE,
                false,
                new Callback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        mqttListener.onPublicationSuccess(topic, message);
                    }

                    @Override
                    public void onFailure(Throwable value) {
                        enqueue(topic, message, value);
                    }
                }
        );
    }

    private void enqueue(String topic, Object message, Throwable reason) {
        if (!mqttListener.shouldEnqueue(topic, message)) {
            return;
        }
        mqttListener.onPublicationQueued(topic, message, reason);
        queue.addToQueue(topic, message);
    }

    public void close() {
        if (connection != null) {
            connection.disconnect(null);
            mqttListener.onConnectionStateChange(false);
        }
    }

}
