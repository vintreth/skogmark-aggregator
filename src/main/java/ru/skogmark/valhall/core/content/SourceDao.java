package ru.skogmark.valhall.core.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class SourceDao {
    private static final Logger log = LoggerFactory.getLogger(SourceDao.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SourceDao(@Nonnull NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void insertAuthorizationMeta(@Nonnull AuthorizationMeta authorizationMeta) {
        requireNonNull(authorizationMeta, "authorizationMeta");
        log.info("insertAuthorizationMeta(): authorizationMeta={}", authorizationMeta);
        jdbcTemplate.update("insert into authorization_meta (source_id, authorization_token) values " +
                        "(:sourceId, :authorizationToken)",
                new MapSqlParameterSource()
                        .addValue("sourceId", authorizationMeta.getSource().getId())
                        .addValue("authorizationToken", authorizationMeta.getAuthorizationToken()));
        log.info("AuthorizationMeta inserted: authorizationMeta={}", authorizationMeta);
    }

    public Optional<AuthorizationMeta> getAuthorizationMeta(int sourceId) {
        log.info("getAuthorizationMeta(): sourceId={}", sourceId);
        return Optional.empty(); // todo
    }

    public void updateAuthorizationMeta(@Nonnull AuthorizationMeta authorizationMeta) {
        log.info("updateAuthorizationMeta(): authorizationMeta={}", authorizationMeta);
        // todo
    }
}