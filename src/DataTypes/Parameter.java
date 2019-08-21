package DataTypes;

import SimulableModel.PreconditionsException;

import java.util.HashMap;
import java.util.Map;

public class Parameter {
    private String entity;
    private String id;
    private Map<String, String> properties;

    public Parameter(String entity, String id) {
        this.entity = entity;
        this.id = id;
        this.properties = new HashMap<>();
    }

    public void addProperty(String property, String value){
        this.properties.put(property, value);
    }

    public void removeProperty(String property) {
        this.properties.remove(property);
    }

    public String getEntity() {
        return entity;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getProperties() {
        return new HashMap<>(this.properties);
    }
}
