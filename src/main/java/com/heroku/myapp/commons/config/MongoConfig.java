package com.heroku.myapp.commons.config;

import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.util.LinkedHashMap;
import java.util.Map;
import static java.util.Optional.ofNullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private static final Map<String, MongoClientURI> MONGO_SETTINGS
            = new LinkedHashMap<>();

    public static MongoClientURI getMongoClientURI(MongoTarget target) {
        return MONGO_SETTINGS.get(target.expression());
    }

    public MongoConfig() {
        try {
            MongoClientURI ownMongoClientURI = new MongoClientURI(
                    ofNullable(System.getenv("MONGOLAB_URI"))
                    .orElseGet(() -> Environments.ENV.get("MONGODB_URI")));
            try (MongoClient mongoClient = new MongoClient(ownMongoClientURI)) {
                Map<String, String> settings = mongoClient.
                        getDatabase(ownMongoClientURI.getDatabase())
                        .getCollection("settings").find().iterator().next()
                        .get("mongodb", Map.class);
                settings.entrySet().stream().forEach((entry)
                        -> MONGO_SETTINGS.put(entry.getKey(),
                                new MongoClientURI(entry.getValue())));
                MONGO_SETTINGS.put(
                        MongoTarget.DUMMY.expression(), ownMongoClientURI);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("mongodb client initialization failed..."
                    + "\nSystem is shutting down.");
            System.exit(1);
        }
    }

    @Bean(name = "master")
    public MongoClient getMongoClientMaster() {
        return new MongoClient(getMongoClientURI(MongoTarget.MASTER));
    }

    @Bean(name = "snapshot")
    public MongoClient getMongoClientSnapshot() {
        return new MongoClient(getMongoClientURI(MongoTarget.SNAPSHOT));
    }

    @Bean(name = "diff")
    public MongoClient getMongoClientDiff() {
        return new MongoClient(getMongoClientURI(MongoTarget.DIFF));
    }

    @Bean(name = "seiyulab")
    public MongoClient getMongoClientSeiyulab() {
        return new MongoClient(getMongoClientURI(MongoTarget.SEIYULAB));
    }

    @Bean(name = "dummy")
    public MongoClient getMongoClientDummy() {
        return new MongoClient(getMongoClientURI(MongoTarget.DUMMY));
    }
}
