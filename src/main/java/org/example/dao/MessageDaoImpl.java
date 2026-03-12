package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.model.Message;
import org.example.model.User;
import org.example.util.HibernateUtil;

import java.util.List;

public class MessageDaoImpl implements MessageDao {

    private EntityManagerFactory emfactory;

    public MessageDaoImpl() {
        this.emfactory = HibernateUtil.getEntityManagerFactory();
    }

    public void save(Message message) throws Exception {

        EntityManager saveManager = emfactory.createEntityManager();

        try {

            saveManager.getTransaction().begin();
            saveManager.persist(message);
            saveManager.getTransaction().commit();

        }catch(Exception e){

            saveManager.getTransaction().rollback();
            throw e;

        }finally{

            saveManager.close();
        }

    }

    public void update(Message message) throws Exception {
        EntityManager updateManager = emfactory.createEntityManager();

        try {

            updateManager.getTransaction().begin();
            updateManager.merge(message);
            updateManager.getTransaction().commit();

        }catch(Exception e){

            updateManager.getTransaction().rollback();
            throw e;

        }finally {

            updateManager.close();
        }
    }

    public void delete(Message message) throws Exception {
        EntityManager deleteManager = emfactory.createEntityManager();

        try {

            deleteManager.getTransaction().begin();
            deleteManager.remove(deleteManager.merge(message));
            deleteManager.getTransaction().commit();

        } catch (Exception e) {

            deleteManager.getTransaction().rollback();
            throw e;

        }finally{

            deleteManager.close();
        }
    }

    public void updateStatut(Message message) throws Exception {
        EntityManager updateStatutManager = emfactory.createEntityManager();

        try{

            if (message.getStatutMessage() == Message.StatutMessage.ENVOYE)
            {
                message.setStatutMessage(Message.StatutMessage.RECU);
            } else if (message.getStatutMessage() == Message.StatutMessage.RECU) {
                message.setStatutMessage(Message.StatutMessage.LU);
            }else if (message.getStatutMessage() == Message.StatutMessage.LU) {
                return;
            }

            updateStatutManager.getTransaction().begin();
            updateStatutManager.merge(message);
            updateStatutManager.getTransaction().commit();
        } catch (Exception e) {
            updateStatutManager.getTransaction().rollback();
            throw e;
        }finally{
            updateStatutManager.close();
        }
    }

    public List<Message> findConversation(User sender, User receiver) throws Exception{

        EntityManager fCManager = emfactory.createEntityManager();

        try {
            return fCManager.createQuery("select m from Message m where (m.sender.id=:senderid and m.receiver.id=:receiverid) or (m.sender.id=:receiverid and m.receiver.id=:senderid) ORDER BY m.dateHeureEnvoi ASC", Message.class)
                    .setParameter("senderid", sender.getId())
                    .setParameter("receiverid", receiver.getId())
                    .getResultList();
        }catch(Exception e){

            throw e;
        }finally {
            fCManager.close();
        }
    }

    public List<Message>  findPendingMessage(User receiver) throws Exception{

        EntityManager fPManager = emfactory.createEntityManager();

        try {
            return fPManager.createQuery("select m from Message m where m.receiver=:receiver and m.statutMessage=:statut", Message.class)
                    .setParameter("receiver", receiver)
                    .setParameter("statut", Message.StatutMessage.ENVOYE)
                    .getResultList();
        } catch (Exception e) {
            throw e;
        }finally {
            fPManager.close();
        }
    }

}
