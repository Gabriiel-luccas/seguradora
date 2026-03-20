package com.acme.seguradora.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outbox_seq")
    @SequenceGenerator(name = "outbox_seq", sequenceName = "outbox_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "quote_id")
    private Long quoteId;

    @Column(name = "flag_sent", nullable = false)
    @Builder.Default
    private boolean flagSent = false;

    @Column(name = "dat_sent")
    private LocalDateTime datSent;

    @Column(name = "dat_received")
    private LocalDateTime datReceived;

    @CreationTimestamp
    @Column(name = "dat_created", nullable = false, updatable = false)
    private LocalDateTime datCreated;

    @UpdateTimestamp
    @Column(name = "dat_updated", nullable = false)
    private LocalDateTime datUpdated;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
