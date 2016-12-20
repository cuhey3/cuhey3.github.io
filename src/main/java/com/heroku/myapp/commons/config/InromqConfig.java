package com.heroku.myapp.commons.config;

import com.heroku.myapp.commons.util.AppUtil;
import io.iron.ironmq.Client;
import io.iron.ironmq.Cloud;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InromqConfig {

    @Bean(name = "myironmq")
    Client getIronmqClient() {
        try {
            return new Client(
                    Environments.IRON.getOr("IRON_MQ_PROJECT_ID", "project_id"),
                    Environments.IRON.getOr("IRON_MQ_TOKEN", "token"),
                    Cloud.ironAWSUSEast);
        } catch (Exception ex) {
            AppUtil.shuttingDownConsumer()
                    .accept("ironmq client initialization failed...");
            return null;
        }
    }
}
