package com.medibhavan.repository;

import com.medibhavan.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByConnectionIdOrderByCreatedAtAsc(String connectionId);
    long countByReceiverIdAndReadFalse(String receiverId);
    List<Message> findByConnectionIdAndReceiverIdAndReadFalse(String connectionId, String receiverId);
}
