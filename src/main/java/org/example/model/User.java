package org.example.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name= "Users")
public class User implements Serializable {

        public enum Role {ORGANISATEUR, MEMBRE, BENEVOLE}
        public enum Status {ONLINE, OFFLINE}

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true, nullable = false)
        private String userName;

        @Column(nullable = false)
        private String hashPassword;

        @Column(name ="date_creation")
        private LocalDateTime creationDate;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Role role;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Status status;

        public User() {}

        public User(String userName, String hashPassword, Role role) {
                this.userName = userName;
                this.hashPassword = hashPassword;
                this.role = role;
                this.creationDate = LocalDateTime.now();
                this.status = Status.OFFLINE;
        }

        public Long getId() {return id;}

        public LocalDateTime getCreationDate() {return creationDate;}

        public String getUserName() {return userName;}

        public void setUserName(String userName) {this.userName = userName;}

        public String getHashPassword() {return hashPassword;}

        public void setHashPassword(String hashPassword) {this.hashPassword = hashPassword;}

        public Role getRole() {return role;}

        public void setRole(Role role) {this.role = role;}

        public Status getStatus() {return status;}

        public void setStatus(Status status) {this.status = status;}

        @Override
        public String toString() {
                return userName + " [" + role + "] - " + status;
        }
}
