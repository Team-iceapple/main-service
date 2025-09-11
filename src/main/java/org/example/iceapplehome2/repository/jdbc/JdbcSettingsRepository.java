package org.example.iceapplehome2.repository.jdbc;

import org.example.iceapplehome2.repository.SettingsRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class JdbcSettingsRepository implements SettingsRepository {
    private final JdbcTemplate jdbc;
    public JdbcSettingsRepository(DataSource ds) { this.jdbc = new JdbcTemplate(ds); }

    @Override
    public Optional<Double> getPlaybackRate() {
        var sql = "SELECT playback_rate FROM video_settings WHERE id = TRUE";
        return jdbc.query(sql, rs -> rs.next() ? Optional.of(rs.getDouble(1)) : Optional.empty());
    }

    @Override
    public void upsertPlaybackRate(Double playbackRate) {
        var sql = """
            INSERT INTO video_settings (id, playback_rate)
            VALUES (TRUE, COALESCE(?, 1.0))
            ON CONFLICT (id) DO UPDATE
            SET playback_rate = COALESCE(EXCLUDED.playback_rate, video_settings.playback_rate),
                updated_at   = now()
            """;
        jdbc.update(sql, playbackRate);
    }
}