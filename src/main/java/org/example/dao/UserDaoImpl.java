package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import org.example.model.User;
import org.example.util.HibernateUtil;

import java.util.List;

public class UserDaoImpl implements UserDao {

    private EntityManagerFactory emFactory;

    public UserDaoImpl()
    {
        this.emFactory = HibernateUtil.getEntityManagerFactory();
    }

    @Override
    public void save(User user) throws Exception
    {
        EntityManager saveManager = emFactory.createEntityManager();

        try{

            saveManager.getTransaction().begin();
            saveManager.persist(user);
            saveManager.getTransaction().commit();

        } catch (Exception e) {

            saveManager.getTransaction().rollback();
            throw e;

        }finally {
            saveManager.close();
        }
    }

    @Override
    public void update(User user) throws Exception{

        EntityManager updateManager =  emFactory.createEntityManager();


        try{
            updateManager.getTransaction().begin();
            updateManager.merge(user);
            updateManager.getTransaction().commit();
        } catch (Exception e) {

            updateManager.getTransaction().rollback();
            throw e;
        }finally {
            updateManager.close();
        }
    }

    @Override
    public void delete(User user) throws Exception{

        EntityManager deleteManager =  emFactory.createEntityManager();


        try{
            deleteManager.getTransaction().begin();
            deleteManager.remove(deleteManager.merge(user));
            deleteManager.getTransaction().commit();

        }catch(Exception e){
            deleteManager.getTransaction().rollback();
            throw e;
        }finally {
            deleteManager.close();
        }

    }

    @Override
    public User findByUsername(String username) throws NoResultException{

        EntityManager fbuManager =  emFactory.createEntityManager();

        try{
            return fbuManager.createQuery("select u from User u where u.userName=:username", User.class).setParameter("username", username).getSingleResult();
        }catch(NoResultException e){
            return null;
        }finally {
            fbuManager.close();
        }

    }

    @Override
    public boolean existsByUsername(String username) {

        EntityManager existManager = emFactory.createEntityManager();
        try {
            Long exist = existManager.createQuery("select Count(u) from User u where u.userName=:username", Long.class).setParameter("username", username).getSingleResult();
            return exist > 0;
        } catch (Exception e) {
           throw e;
        }finally {
            existManager.close();
        }
    }

    @Override
    public List<User> findAll() {

        EntityManager fAManager = emFactory.createEntityManager();

        try {
            return fAManager.createQuery("select u from User u", User.class).getResultList();
        }catch (Exception e){
            throw e;
        }finally {
            fAManager.close();
        }
    }
}
