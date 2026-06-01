package edu.univ.erp.domain;

public class Settings {
    private final String key;
    private final String value;
    
    public Settings(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() { return key; }
    public String getValue() { return value; }
    
    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(value);
    }
    
    public int getValueAsInt() {
        return Integer.parseInt(value);
    }
    
    @Override
    public String toString() {
        return String.format("Settings{key='%s', value='%s'}", key, value);
    }
}