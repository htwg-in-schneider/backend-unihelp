package de.htwg.in.schneider.unihelp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderOauthId;
    private String senderName;

    private String receiverOauthId;
    private String receiverName;

    @Column(length = 1000)
    private String content;

    private LocalDateTime timestamp;
    private boolean isRead;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderOauthId() {
        return senderOauthId;
    }

    public void setSenderOauthId(String senderOauthId) {
        this.senderOauthId = senderOauthId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverOauthId() {
        return receiverOauthId;
    }

    public void setReceiverOauthId(String receiverOauthId) {
        this.receiverOauthId = receiverOauthId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
