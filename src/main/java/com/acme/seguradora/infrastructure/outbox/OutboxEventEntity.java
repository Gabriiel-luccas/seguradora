package com.acme.seguradora.infrastructure.outbox;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

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
    private int retryCount = 0;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    protected OutboxEventEntity() {}

    private OutboxEventEntity(Builder builder) {
        this.topic        = builder.topic;
        this.eventType    = builder.eventType;
        this.payload      = builder.payload;
        this.quoteId      = builder.quoteId;
        this.flagSent     = builder.flagSent;
        this.datSent      = builder.datSent;
        this.datReceived  = builder.datReceived;
        this.retryCount   = builder.retryCount;
        this.status       = builder.status;
        this.errorMessage = builder.errorMessage;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getTopic() { return topic; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Long getQuoteId() { return quoteId; }
    public boolean isFlagSent() { return flagSent; }
    public LocalDateTime getDatSent() { return datSent; }
    public LocalDateTime getDatReceived() { return datReceived; }
    public LocalDateTime getDatCreated() { return datCreated; }
    public LocalDateTime getDatUpdated() { return datUpdated; }
    public int getRetryCount() { return retryCount; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }

    public void setFlagSent(boolean flagSent) { this.flagSent = flagSent; }
    public void setDatSent(LocalDateTime datSent) { this.datSent = datSent; }
    public void setDatReceived(LocalDateTime datReceived) { this.datReceived = datReceived; }
    public void setDatUpdated(LocalDateTime datUpdated) { this.datUpdated = datUpdated; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public void setStatus(String status) { this.status = status; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public static final class Builder {
        private String topic;
        private String eventType;
        private String payload;
        private Long quoteId;
        private boolean flagSent = false;
        private LocalDateTime datSent;
        private LocalDateTime datReceived;
        private int retryCount = 0;
        private String status = "PENDING";
        private String errorMessage;

        private Builder() {}

        public Builder topic(String v) { this.topic = v; return this; }
        public Builder eventType(String v) { this.eventType = v; return this; }
        public Builder payload(String v) { this.payload = v; return this; }
        public Builder quoteId(Long v) { this.quoteId = v; return this; }
        public Builder flagSent(boolean v) { this.flagSent = v; return this; }
        public Builder datSent(LocalDateTime v) { this.datSent = v; return this; }
        public Builder datReceived(LocalDateTime v) { this.datReceived = v; return this; }
        public Builder retryCount(int v) { this.retryCount = v; return this; }
        public Builder status(String v) { this.status = v; return this; }
        public Builder errorMessage(String v) { this.errorMessage = v; return this; }
        public OutboxEventEntity build() { return new OutboxEventEntity(this); }
    }
}
