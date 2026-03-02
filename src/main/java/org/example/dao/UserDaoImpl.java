package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.model.User;

import java.util.List;

public class UserDaoImpl implements UserDao {

    private EntityManagerFactory emFactory;
    private EntityManager entityManager;

    public UserDaoImpl()
    {
           this.emFactory=Persistence.createEntityManagerFactory("messageriePU");
           this.entityManager=this.emFactory.createEntityManager();
    }

    @Override
    public void save(User user)
    {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(User user) {

    }

    @Override
    public User findByUsername(String username) {
        return null;
    }

    @Override
    public boolean existsByUsername(String username) {
        return false;
    }

    @Override
    public List<User> findAll() {
        return List.of();
    }
}
