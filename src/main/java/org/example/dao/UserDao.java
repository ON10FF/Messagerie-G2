package org.example.dao;

import org.example.model.User;
import java.util.List;

public interface UserDao {

    public void save(User user) throws Exception;
    public void update(User user) throws Exception;
    public void delete(User user) throws Exception;
    public User findByUsername(String username) throws Exception;
    public boolean existsByUsername(String username) throws Exception;
    public List<User> findAll() throws Exception;

}
