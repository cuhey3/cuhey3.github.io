package com.heroku.myapp.commons.util.consumers;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.QueueType;
import com.heroku.myapp.commons.util.MessageUtil;
import io.iron.ironmq.Client;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class IronmqUtil {

    private static final String IRONMQ_CLIENT_BEAN_NAME = "myironmq";

    public static Expression affectQueueUri() {
        return new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                String kindString = exchange.getIn().getBody(String.class);
                exchange.getIn().setBody(Kind.valueOf(kindString).preMessage());
                return type.cast(String.format("ironmq:%s?client=%s",
                        "snapshot_" + kindString, IRONMQ_CLIENT_BEAN_NAME));
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
            Optional<Kind> optionalKind = MessageUtil.optionalGetKind(exchange);
            if (optionalKind.isPresent()) {
                new IronmqUtil().snapshot()
                        .postMessage(exchange, optionalKind.get());
            }
        };
    }

    private String type, kindString;
    private int timeout;

    public IronmqUtil() {
        this.timeout = 60;
    }

    public IronmqUtil type(QueueType type) {
        this.type = type.expression();
        return this;
    }

    public IronmqUtil kind(Kind kind) {
        this.kindString = kind.expression();
        return this;
    }

    public IronmqUtil kindString(String kindString) {
        this.kindString = kindString;
        return this;
    }

    public IronmqUtil timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public IronmqUtil snapshot() {
        this.type = QueueType.SNAPSHOT.expression();
        return this;
    }

    public IronmqUtil diff() {
        this.type = QueueType.DIFF.expression();
        return this;
    }

    private IronmqUtil completion() {
        this.type = QueueType.COMPLETION.expression();
        this.kindString = "all";
        return this;
    }

    public IronmqUtil changed() {
        this.type = QueueType.CHANGED.expression();
        this.kindString = "all";
        return this;
    }

    public IronmqUtil exception() {
        this.type = QueueType.EXCEPTION.expression();
        return this;
    }

    public void postMessage(Exchange exchange, Kind k) throws IOException {
        Client client = exchange.getContext().getRegistry()
                .lookupByNameAndType(IRONMQ_CLIENT_BEAN_NAME, Client.class);
        client.queue(this.type + "_" + k.expression())
                .push(k.preMessage());
    }

    public String consumeUri() {
        return String.format("ironmq:%s"
                + "?client=%s"
                + "&timeout=%s"
                + "&maxMessagesPerPoll=100",
                type + "_" + kindString, IRONMQ_CLIENT_BEAN_NAME, timeout);
    }

    public String postUri() {
        return String.format("ironmq:%s?client=%s",
                type + "_" + kindString, IRONMQ_CLIENT_BEAN_NAME);
    }

    public String completionPostUri() {
        return new IronmqUtil().completion().postUri();
    }

    public String completionConsumeUri() {
        return new IronmqUtil().completion().consumeUri();
    }
}
