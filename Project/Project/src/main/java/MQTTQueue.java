import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MQTTQueue {

    private HashMap<String, List<Object>> map;

    public MQTTQueue(String[] topics) {
        map = new HashMap<>(topics.length);
        for (String topic: topics) {
            map.put(topic, new ArrayList<>());
        }
    }

    public List<Object> getTopicQueue(String topic) {
        if (!map.containsKey(topic)) {
            return map.put(topic, new ArrayList<>());
        }
        return map.get(topic);
    }

    public void addToQueue(String topic, Object object) {
        getTopicQueue(topic).add(object);
    }

    public void publishAll(MQTTConnection connection) {
        map.forEach((topic, objects) -> {
            for (int i = 0; i < objects.size(); i++) {
                connection.publish(topic, objects.remove(i));
            }
        });
    }
}
