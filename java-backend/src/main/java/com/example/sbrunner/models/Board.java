package main.java.com.example.sbrunner.models;

import jakarta.persistence.Id;
import java.util.UUID;
import java.sql.Timestamp;


public class Board {
    @Id
    private UUID id;
    private String name;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Board(String name, String description){
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public UUID getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getDescription(){
        return this.description;
    }

    public void updateTimestamp(){
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }
}
