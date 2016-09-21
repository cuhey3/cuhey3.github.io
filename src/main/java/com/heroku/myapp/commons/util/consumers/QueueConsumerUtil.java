package com.heroku.myapp.commons.util.consumers;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.KindOption;
import com.heroku.myapp.commons.consumers.QueueConsumer;
import com.heroku.myapp.commons.exceptions.QueueConsumerUtilNotReadyException;
import io.iron.ironmq.Client;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.SimpleBuilder;

public class QueueConsumerUtil {

    private static final String IRONMQ_CLIENT_BEAN_NAME = "myironmq";

    private final QueueConsumer consumer;
    private QueueType queueType;
    private Kind kind;
    private int timeout = 60;

    public QueueConsumerUtil(QueueConsumer queueConsumer) {
        this.consumer = queueConsumer;
        kind(Kind.optionalKindFromClassName(queueConsumer).orElse(null));
    }

    public QueueConsumerUtil(Kind k) {
        this.consumer = null;
        kind(k);
    }

    public Predicate loadAffectPredicate() {
        return (Exchange exchange) -> {
            QueueMessage message = new QueueMessage(exchange);
            Optional<Kind> optionalKind = message.optionalKind();
            if (optionalKind.isPresent()) {
                Kind k = optionalKind.get();
                if (k.isEnable(KindOption.affect)) {
                    exchange.getIn().setBody(k.affects());
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        };
    }

    public Processor requestSnapshotProcess() {
        return (Exchange exchange) -> {
            Optional<Kind> optionalKind
                    = new QueueMessage(exchange).optionalKind();
            if (optionalKind.isPresent()) {
                Kind k = optionalKind.get();
                copy().snapshot().kind(k).postMessage();
            }
        };
    }

    public void sendLog(String method, String message) {
        try {
            ironmqClient().queue(copy().log().id())
                    .push(new Date().toString()
                            + "\nClass: " + consumer.getClass().getName()
                            + "\nmethod: " + method
                            + "\nmessage: " + message);
        } catch (IOException ex1) {
        }
    }

    public void sendError(String method, Exception ex) {
        try {
            ironmqClient().queue(copy().exception().id())
                    .push(new Date().toString()
                            + "\nClass: " + consumer.getClass().getName()
                            + "\nmethod: " + method
                            + "\nException class: " + ex.getClass().getName()
                            + "\nmessage: " + ex.getMessage());
        } catch (IOException ex1) {
        }
    }

    public final QueueConsumerUtil kind(Kind kind) {
        this.kind = kind;
        return this;
    }

    public Kind kind() {
        return Optional.ofNullable(this.kind)
                .orElseThrow(() -> new QueueConsumerUtilNotReadyException());
    }

    public QueueConsumerUtil kindFromClassName() {
        this.kind = Kind.optionalKindFromClassName(this.consumer).orElse(null);
        return this;
    }

    public QueueConsumerUtil timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public QueueConsumerUtil timer() {
        this.queueType = QueueType.TIMER;
        return this;
    }

    public QueueConsumerUtil snapshot() {
        this.queueType = QueueType.SNAPSHOT;
        return this;
    }

    public QueueConsumerUtil diff() {
        this.queueType = QueueType.DIFF;
        return this;
    }

    public QueueConsumerUtil completion() {
        this.queueType = QueueType.COMPLETION;
        this.kind = Kind.all;
        return this;
    }

    public QueueConsumerUtil changed() {
        this.queueType = QueueType.CHANGED;
        this.kind = Kind.all;
        return this;
    }

    public QueueConsumerUtil changing() {
        this.queueType = QueueType.CHANGING;
        this.kind = Kind.all;
        return this;
    }

    public QueueConsumerUtil exception() {
        this.queueType = QueueType.EXCEPTION;
        this.kind = Kind.in;
        return this;
    }

    public QueueConsumerUtil log() {
        this.queueType = QueueType.LOG;
        this.kind = Kind.in;
        return this;
    }

    private QueueConsumerUtil queueType(QueueType queueType) {
        return this;
    }

    public String id() {
        if (queueType != null && kind != null) {
            return queueType.expression() + "_" + kind.expression();
        } else {
            throw new QueueConsumerUtilNotReadyException();
        }
    }

    public SimpleBuilder camelBatchComplete() {
        return SimpleBuilder.simple("${exchangeProperty.CamelBatchComplete}");
    }

    public void postMessage() throws IOException {
        ironmqClient().queue(id()).push(kind().preMessage());
    }

    public String ironmqConsumeUri() {
        if (queueType != null && kind != null) {
            return String.format("ironmq:%s"
                    + "?client=%s"
                    + "&timeout=%s"
                    + "&maxMessagesPerPoll=100",
                    id(), IRONMQ_CLIENT_BEAN_NAME, timeout);
        } else {
            throw new QueueConsumerUtilNotReadyException();
        }
    }

    public String ironmqPostUri() {
        if (queueType != null && kind != null) {
            return String.format(
                    "ironmq:%s?client=%s", id(), IRONMQ_CLIENT_BEAN_NAME);
        } else {
            throw new QueueConsumerUtilNotReadyException();
        }
    }

    public QueueConsumerUtil copy() {
        return new QueueConsumerUtil(this.consumer)
                .queueType(queueType).kind(kind);
    }

    public Client ironmqClient() {
        return consumer.getContext().getRegistry()
                .lookupByNameAndType(IRONMQ_CLIENT_BEAN_NAME, Client.class);
    }

    private enum QueueType {

        TIMER, SNAPSHOT, DIFF, COMPLETION, CHANGING, CHANGED, EXCEPTION, LOG;

        private final String expression;

        private QueueType() {
            this.expression = this.name().toLowerCase(Locale.US);
        }

        public String expression() {
            return this.expression;
        }
    }
}
