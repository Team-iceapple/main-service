package org.example.iceapplehome2.config;

import org.example.iceapplehome2.repository.VideoRepository;
import org.example.iceapplehome2.repository.jdbc.JdbcVideoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class RepositoryConfig {
    @Bean
    public VideoRepository videoRepository(DataSource dataSource) {
        return new JdbcVideoRepository(dataSource);
    }
}