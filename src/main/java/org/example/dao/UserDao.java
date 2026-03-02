package org.example.dao;

import org.example.model.User;
import java.util.List;

public interface UserDao {

    public void save(User user);
    public void update(User user);
    public void delete(User user);
    public User findByUsername(String username);
    public boolean existsByUsername(String username);
    public List<User> findAll();

}
