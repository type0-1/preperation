package main.java.com.example.sbrunner.models;
import jakarta.persistence.Id;
import java.util.UUID;
import java.sql.Timestamp;

public class User {
    @Id
    private UUID id;
    private String displayName;
    private String email;
    private Timestamp createdAt;

    public User(String displayName, String email){
        this.displayName = displayName;
        this.email = email;
        this.id = UUID.randomUUID();
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    private UUID getId(){
        return this.id;
    }

    private String getDisplayName(){
        return this.displayName;
    }

    private String getEmail(){
        return this.email;
    }

    private void setName(String name){
        this.name = name;
    }
}
