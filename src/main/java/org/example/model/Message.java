package org.example.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="Messages")
public class Message implements Serializable {

    public enum StatutMessage {ENVOYE, RECU, LU}

    private static final long serialVersionUID = 1L;

    public Message(){}

    public Message(User sender, User receiver, String message)
    {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.dateHeureEnvoi = LocalDateTime.now();
        this.statutMessage = StatutMessage.ENVOYE;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name= "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name= "receiver_id")
    private User receiver;

    @Column(length=1000)
    private String message;

    @Column(name ="date_envoi")
    private LocalDateTime dateHeureEnvoi;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StatutMessage statutMessage;


    public Long getId() {return id;}

    public LocalDateTime getDateHeureEnvoi() {return dateHeureEnvoi;}

    public User getSender() {return sender;}
    public void setSender(User sender) {this.sender = sender;}

    public User getReceiver() {return receiver;}
    public void setReceiver(User receiver) {this.receiver = receiver;}

    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}

    public StatutMessage getStatutMessage() {return statutMessage;}
    public void setStatutMessage(StatutMessage statutMessage) {this.statutMessage = statutMessage;}


}
