package com.acme.seguradora.application.port.output;

import com.acme.seguradora.domain.model.Quote;

public interface MessagePublisherPort {
    void publishQuoteReceived(Quote quote);
}
