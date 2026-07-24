package main.java.com.example.sbrunner.models;

import jakarta.persistence.Id;
import java.sql.Timestamp;
import java.util.UUID;

public class Activity {
    @Id
    private UUID id;
    private UUID boardId;
    private UUID taskId;
    private UUID actorId;

    private ActivityType type;
    private String description;
    private Timestamp createdAt;

    public Acitivity(String description,  ActivityType type, UUID boardId, UUID taskId, UUID userId){
        this.id = UUID.randomUUID();
        this.boardId = boardId;
        this.taskId = taskId;
        this.userId = userId;

        this.description = description;
        this.type = type;

        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public UUID getId(){
        return this.id;
    }

    public void setActivityType(ActivityType type){
        this.type = type;
    }

    public void setDescription(String description){
        this.description = description;
    }

}
