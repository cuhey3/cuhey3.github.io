package com.heroku.myapp.commons.config.enumerate;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.util.Locale;

public enum MongoTarget {

    SNAPSHOT, DIFF, MASTER, DUMMY, SEIYULAB;

    private final String expression;
    private MongoClientURI uri;

    private MongoTarget() {
        this.expression = this.name().toLowerCase(Locale.US);
    }

    public String expression() {
        return this.expression;
    }

    public void mongoClientURI(MongoClientURI uri) {
        this.uri = uri;
    }

    public MongoClientURI mongoClientURI() {
        return this.uri;
    }

    public MongoClient mongoClient() {
        return new MongoClient(this.uri);
    }
}
