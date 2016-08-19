package com.heroku.myapp.commons.consumers;

import com.heroku.myapp.commons.util.consumers.QueueConsumerUtil;
import java.util.Optional;
import org.apache.camel.builder.RouteBuilder;

public abstract class QueueConsumer extends RouteBuilder {

    private QueueConsumerUtil queueConsumerUtil;

    public final QueueConsumerUtil util() {
        return Optional.ofNullable(queueConsumerUtil).orElseGet(()
                -> queueConsumerUtil = new QueueConsumerUtil(this));
    }
}
