package org.example.dao;

import org.example.model.Message;
import org.example.model.User;

import java.util.List;

public interface MessageDao {


    public void save (Message message) throws Exception;
    public void update (Message message) throws Exception;
    public void delete (Message message) throws Exception;
    public void updateStatut (Message message) throws Exception;
    public List<Message> findConversation(User sender, User receiver) throws Exception;
    public List<Message> findPendingMessage(User receiver) throws Exception;
}
