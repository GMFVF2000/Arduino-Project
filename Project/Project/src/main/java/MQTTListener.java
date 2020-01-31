public interface MQTTListener {

    void onConnectionStateChange(boolean connected);

    void onConnectionFailure(Throwable reason);

    void onPublicationSuccess(String topic, Object message);

    void onPublicationQueued(String topic, Object message, Throwable reason);

    boolean shouldEnqueue(String topic, Object message);
}
