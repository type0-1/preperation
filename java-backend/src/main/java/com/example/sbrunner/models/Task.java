package main.java.com.example.sbrunner.models;

import java.util.UUID;
import java.sql.Timestamp;
import main.java.com.example.sbrunner.enums.*;

import jarkarta.persistence.Id;

public class Task {
    @Id
    private UUID id;
    private UUID boardId;
    private UUID assigneeId;

    private String title;
    private String description;

    private TaskStatus status;
    private TaskPriority priority;
    private long version;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Task(String name, String title, String description, TaskStatus status, TaskPriority priority){
        this.id = UUID.randomUUID();
        this.boardId = UUID.randomUUID();
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.version = 1;

        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }

    public String getName(){
        return this.name;
    }

    public String getTitle(){
        return this.title;
    }

    public String getDescription(){
        return this.description;
    }

    public UUID getId(){
        return this.id;
    }

    public UUID boardId(){
        return this.boardId;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setAssigneeId(UUID assigneeId){
        this.assigneeId = assigneeId;
    }

    public void setStatus(TaskStatus status){
        this.status = status;
    }

    public void setPriority(TaskPriority priority){
        this.priority = priority;
    }

    public void setUpdatedTaskTimestamp(){
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
