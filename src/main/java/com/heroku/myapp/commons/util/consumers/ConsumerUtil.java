package com.heroku.myapp.commons.util.consumers;

import com.heroku.myapp.commons.config.enumerate.Kind;
import io.iron.ironmq.Client;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.SimpleBuilder;

public class ConsumerUtil {

    private static final String IRONMQ_CLIENT_BEAN_NAME = "myironmq";

    public static Expression affectQueueUri() {
        return new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                String kindString = exchange.getIn().getBody(String.class);
                Optional<Kind> optionalKind
                        = Kind.optionalKindFromString(kindString);
                if (optionalKind.isPresent()) {
                    exchange.getIn().setBody(optionalKind.get().preMessage());
                    return type.cast(String.format("ironmq:%s?client=%s",
                            "snapshot_" + kindString, IRONMQ_CLIENT_BEAN_NAME));
                } else {
                    return type.cast("");
                }
            }
        };
    }

    public static void sendError(RouteBuilder rb, String method, Exception ex) {
        try {
            Client client = rb.getContext().getRegistry()
                    .lookupByNameAndType(IRONMQ_CLIENT_BEAN_NAME, Client.class);
            client.queue("exception_in")
                    .push(new Date().toString()
                            + "\nClass: " + rb.getClass().getName()
                            + "\nmethod: " + method
                            + "\nException class: " + ex.getClass().getName()
                            + "\nmessage: " + ex.getMessage());
        } catch (IOException ex1) {
        }
    }

    public static void sendLog(RouteBuilder rb, String method, String message) {
        try {
            Client client = rb.getContext().getRegistry()
                    .lookupByNameAndType(IRONMQ_CLIENT_BEAN_NAME, Client.class);
            client.queue("log_in")
                    .push(new Date().toString()
                            + "\nClass: " + rb.getClass().getName()
                            + "\nmethod: " + method
                            + "\nmessage: " + message);
        } catch (IOException ex1) {
        }
    }

    public static Processor requestSnapshotProcess() {
        return (Exchange exchange) -> {
            Optional<Kind> optionalKind
                    = new QueueMessage(exchange).optionalKind();
            if (optionalKind.isPresent()) {
                new ConsumerUtil().snapshot()
                        .postMessage(exchange, optionalKind.get());
            }
        };
    }

    private QueueType queueType;
    private Kind kind;
    private int timeout = 60;

    public ConsumerUtil kind(Kind kind) {
        this.kind = kind;
        return this;
    }

    public ConsumerUtil timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ConsumerUtil timer() {
        this.queueType = QueueType.TIMER;
        return this;
    }

    public ConsumerUtil snapshot() {
        this.queueType = QueueType.SNAPSHOT;
        return this;
    }

    public ConsumerUtil diff() {
        this.queueType = QueueType.DIFF;
        return this;
    }

    public ConsumerUtil completion() {
        this.queueType = QueueType.COMPLETION;
        this.kind = Kind.all;
        return this;
    }

    public ConsumerUtil changed() {
        this.queueType = QueueType.CHANGED;
        this.kind = Kind.all;
        return this;
    }

    public ConsumerUtil changing() {
        this.queueType = QueueType.CHANGING;
        this.kind = Kind.all;
        return this;
    }

    public ConsumerUtil exception() {
        this.queueType = QueueType.EXCEPTION;
        this.kind = Kind.in;
        return this;
    }

    public String id() {
        return queueType.expression() + "_" + kind.expression();
    }

    public SimpleBuilder camelBatchComplete() {
        return SimpleBuilder.simple("${exchangeProperty.CamelBatchComplete}");
    }

    public void postMessage(Exchange exchange, Kind k) throws IOException {
        Client client = exchange.getContext().getRegistry()
                .lookupByNameAndType(IRONMQ_CLIENT_BEAN_NAME, Client.class);
        client.queue(this.queueType + "_" + k.expression())
                .push(k.preMessage());
    }

    public String consumeUri() {
        return String.format("ironmq:%s"
                + "?client=%s"
                + "&timeout=%s"
                + "&maxMessagesPerPoll=100",
                queueType.expression() + "_" + kind.expression(),
                IRONMQ_CLIENT_BEAN_NAME, timeout);
    }

    public String postUri() {
        return String.format("ironmq:%s?client=%s",
                queueType.expression() + "_" + kind.expression(),
                IRONMQ_CLIENT_BEAN_NAME);
    }

    public String completionPostUri() {
        return new ConsumerUtil().completion().postUri();
    }

    public String completionConsumeUri() {
        return new ConsumerUtil().completion().consumeUri();
    }

    private enum QueueType {

        TIMER, SNAPSHOT, DIFF, COMPLETION, CHANGING, CHANGED, EXCEPTION;

        private final String expression;

        private QueueType() {
            this.expression = this.name().toLowerCase(Locale.US);
        }

        public String expression() {
            return this.expression;
        }
    }

}
