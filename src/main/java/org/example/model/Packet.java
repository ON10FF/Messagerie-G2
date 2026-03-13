package org.example.model;

import java.io.Serializable;

public class Packet implements Serializable {

    private String type;
    private String username;
    private String role;
    private String message;
    private String receiver;
    private Object data;

    public Packet() {}

    public Packet(String type) {
        this.type = type;
    }

    public String getType()              { return type; }
    public void   setType(String type)   { this.type = type; }

    public String getUsername()                  { return username; }
    public void   setUsername(String username)   { this.username = username; }

    public String getRole()              { return role; }
    public void   setRole(String role)   { this.role = role; }

    public String getMessage()                   { return message; }
    public void   setMessage(String message)     { this.message = message; }

    public String getReceiver()                  { return receiver; }
    public void   setReceiver(String receiver)   { this.receiver = receiver; }

    public Object getData()              { return data; }
    public void   setData(Object data)   { this.data = data; }
}