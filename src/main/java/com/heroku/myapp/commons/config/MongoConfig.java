package com.heroku.myapp.commons.config;

import com.heroku.myapp.commons.config.enumerate.MongoTarget;
import com.heroku.myapp.commons.util.AppUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.util.Locale;
import java.util.Map;
import static java.util.Optional.ofNullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    public MongoConfig() {
        try {
            MongoClientURI uri = getOwnMongoClientURI();
            MongoTarget.DUMMY.mongoClientURI(uri);
            try (MongoClient client = new MongoClient(uri)) {
                Map<String, String> mongoUriMap = getMongoUriMap(client, uri);
                mongoUriMap.entrySet().stream().forEach((entry) -> {
                    MongoTarget.valueOf(entry.getKey().toUpperCase(Locale.US))
                            .mongoClientURI(new MongoClientURI(entry.getValue()));
                });
            }
        } catch (Exception ex) {
            AppUtil.shuttingDownConsumer()
                    .accept("mongodb client initialization failed...");
        }
    }

    @Bean(name = "master")
    public MongoClient getMongoClientMaster() {
        return MongoTarget.MASTER.mongoClient();
    }

    @Bean(name = "snapshot")
    public MongoClient getMongoClientSnapshot() {
        return MongoTarget.SNAPSHOT.mongoClient();
    }

    @Bean(name = "diff")
    public MongoClient getMongoClientDiff() {
        return MongoTarget.DIFF.mongoClient();
    }

    @Bean(name = "seiyulab")
    public MongoClient getMongoClientSeiyulab() {
        return MongoTarget.SEIYULAB.mongoClient();
    }

    @Bean(name = "dummy")
    public MongoClient getMongoClientDummy() {
        return MongoTarget.DUMMY.mongoClient();
    }

    private MongoClientURI getOwnMongoClientURI() {
        return new MongoClientURI(ofNullable(System.getenv("MONGOLAB_URI"))
                .orElseGet(() -> Environments.ENV.get("MONGODB_URI")));
    }

    private Map<String, String> getMongoUriMap(MongoClient client, MongoClientURI uri) {
        return client.getDatabase(uri.getDatabase()).getCollection("settings")
                .find().iterator().next().get("mongodb", Map.class);
    }
}
