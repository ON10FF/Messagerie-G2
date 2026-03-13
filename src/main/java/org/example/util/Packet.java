package org.example.util;

import java.io.Serializable;

public class Packet implements Serializable {

public enum Type {
    LOGIN, REGISTER, MESSAGE, LOGOUT, 
    GET_USERS, GET_HISTORY, GET_ALL_MEMBERS, GET_ONLINE_USERS,
    USER_LIST, ERROR, SUCCESS
}
    private static final long serialVersionUID = 1L;

    private Type type;

    private Object data;

    public Packet(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {return type;}

    public Object getData() {return data;}
}
