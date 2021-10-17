package com.cassioleodegario.kafka.producer;

import com.cassioleodegario.kafka.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Component
@Slf4j
public class LibraryEventProducer {

    private static final String TOPIC = "library-events";

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ListenableFuture<SendResult<Integer, String>> listanableFuture = kafkaTemplate.sendDefault(key, value);
        listanableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
    }

    public SendResult<Integer, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = null;

        try {
            sendResult = kafkaTemplate.sendDefault(key, value).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("InterruptedException/ExecutionException Error sending the message {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error sending the message {}", e.getMessage());
            throw e;
        }

        return sendResult;

    }

    public ListenableFuture sendLibraryEvent_Approach2(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, TOPIC);

        ListenableFuture<SendResult<Integer, String>> listanableFuture = kafkaTemplate.send(producerRecord);
        listanableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });

        return listanableFuture;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {

        List<Header> recordHeader = List.of(new RecordHeader("event-source", "scanner".getBytes()));

        return new ProducerRecord<Integer, String>(topic, null, key,value, null);
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error sending the message {}", ex.getMessage());
        try{
            throw ex;
        }catch (Throwable e){
            log.error("Error on failure {}", e.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Success for the key {} and value {} , partition is {}", key, value, result.getRecordMetadata());
    }

}
