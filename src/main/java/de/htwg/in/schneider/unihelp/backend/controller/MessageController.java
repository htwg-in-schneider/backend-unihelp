package de.htwg.in.schneider.unihelp.backend.controller;

import de.htwg.in.schneider.unihelp.backend.model.Message;
import de.htwg.in.schneider.unihelp.backend.model.User;
import de.htwg.in.schneider.unihelp.backend.repository.MessageRepository;
import de.htwg.in.schneider.unihelp.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Message>> getMyMessages(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null)
            return ResponseEntity.status(401).build();
        String myId = jwt.getSubject();
        List<Message> messages = messageRepository.findBySenderOauthIdOrReceiverOauthIdOrderByTimestampAsc(myId, myId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<Message> sendMessage(@AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> payload) {
        if (jwt == null)
            return ResponseEntity.status(401).build();

        String senderId = jwt.getSubject();
        Optional<User> senderOpt = userRepository.findByOauthId(senderId);
        if (!senderOpt.isPresent())
            return ResponseEntity.badRequest().build();

        Message msg = new Message();
        msg.setSenderOauthId(senderId);
        msg.setSenderName(senderOpt.get().getName());
        msg.setReceiverOauthId(payload.get("receiverOauthId"));
        msg.setReceiverName(payload.get("receiverName"));
        msg.setContent(payload.get("content"));
        msg.setTimestamp(LocalDateTime.now());
        msg.setRead(false);

        return ResponseEntity.ok(messageRepository.save(msg));
    }

    @PutMapping("/{partnerId}/read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal Jwt jwt, @PathVariable String partnerId) {
        if (jwt == null)
            return ResponseEntity.status(401).build();
        String myId = jwt.getSubject();

        List<Message> messages = messageRepository.findBySenderOauthIdOrReceiverOauthIdOrderByTimestampAsc(myId, myId);
        for (Message m : messages) {
            if (m.getSenderOauthId().equals(partnerId) && m.getReceiverOauthId().equals(myId) && !m.isRead()) {
                m.setRead(true);
                messageRepository.save(m);
            }
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (jwt == null)
            return ResponseEntity.status(401).build();
        Optional<Message> msgOpt = messageRepository.findById(id);

        if (msgOpt.isPresent()) {
            Message msg = msgOpt.get();
            if (msg.getSenderOauthId().equals(jwt.getSubject())) {
                messageRepository.delete(msg);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.notFound().build();
    }
}
