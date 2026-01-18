package com.mars_sim.core;

public class MockEntity implements Entity {
    private final String id;
    
    public MockEntity(String id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "MockEntity{" + "id='" + id + '\'' + '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MockEntity that = (MockEntity) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public String getContext() {
        return "MockContext";
    }   
}
