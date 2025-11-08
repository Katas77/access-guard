package com.example.access_guard.configuration;

import com.example.access_guard.model.redis.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.time.Duration;
import java.util.Collections;


@Configuration
@EnableRedisRepositories(
        keyspaceConfiguration = RedisConfiguration.RefreshTokenKeyspaceConfiguration.class,
        enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP
)
public class RedisConfiguration {

    /**
     * Время жизни токенов обновления (срок жизни равный 30 дн а затем он будет автоматически удалён из хранилища Redis).
     */
    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());          // хост Redis
        config.setPort(redisProperties.getPort());              // порт Redis
        config.setPassword(redisProperties.getPassword());      // пароль Redis

        return new JedisConnectionFactory(config);
    }

    public class RefreshTokenKeyspaceConfiguration extends KeyspaceConfiguration {
        private static final String REFRESH_TOKEN_KEYSPACE = "refresh_tokens";
        @Override
        protected Iterable<KeyspaceSettings> initialConfiguration() {
            KeyspaceSettings settings = new KeyspaceSettings(RefreshToken.class, REFRESH_TOKEN_KEYSPACE);
            settings.setTimeToLive(refreshTokenExpiration.getSeconds()); // время жизни токена в секундах
            return Collections.singleton(settings);
        }
    }
}