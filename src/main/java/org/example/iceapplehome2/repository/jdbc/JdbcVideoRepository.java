package org.example.iceapplehome2.repository.jdbc;

import org.example.iceapplehome2.entity.Video;
import org.example.iceapplehome2.repository.VideoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class JdbcVideoRepository implements VideoRepository {

    private final JdbcTemplate jdbc;

    public JdbcVideoRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    private RowMapper<Video> rowMapper() {
        return (rs, rowNum) -> {
            Timestamp ts = rs.getTimestamp("created_at");
            OffsetDateTime odt = ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;
            return new Video(
                    rs.getString("id"),
                    rs.getString("file_path"),
                    rs.getString("title"),
                    rs.getBoolean("is_current"),
                    odt
            );
        };
    }

    @Override
    public List<Video> findAllOrderByCreatedAtDesc() {
        String sql = "SELECT id, file_path, title, is_current, created_at " +
                "FROM home_video ORDER BY created_at DESC";
        return jdbc.query(sql, rowMapper());
    }

    @Override
    public Optional<Video> findCurrent() {
        String sql = "SELECT id, file_path, title, is_current, created_at " +
                "FROM home_video WHERE is_current = true LIMIT 1";
        return jdbc.query(sql, rowMapper()).stream().findFirst();
    }

    @Override
    public Optional<Video> findById(String id) {
        String sql = "SELECT id, file_path, title, is_current, created_at " +
                "FROM home_video WHERE id = ?";
        return jdbc.query(sql, rowMapper(), id).stream().findFirst();
    }

    @Override
    public String insert(Video video, boolean current) {
        String sql = "INSERT INTO home_video (id, file_path, title, is_current) " +
                "VALUES ('v_' || gen_random_uuid()::text, ?, ?, ?) RETURNING id";
        return jdbc.queryForObject(sql, String.class,
                video.getFilePath(), video.getTitle(), current);
    }

    @Override
    public int unsetAllCurrent() {
        String sql = "UPDATE home_video SET is_current = false WHERE is_current = true";
        return jdbc.update(sql);
    }

    @Override
    public int setCurrentById(String id) {
        String sql = "UPDATE home_video SET is_current = true WHERE id = ?";
        return jdbc.update(sql, id);
    }

    @Override
    public void deleteById(String id) {
        jdbc.update("DELETE FROM home_video WHERE id = ?", id);
    }

    @Override
    public Optional<Video> findLatestExcluding(String excludeId) {
        String sql = "SELECT id, file_path, title, is_current, created_at " +
                "FROM home_video WHERE id <> ? ORDER BY created_at DESC LIMIT 1";
        return jdbc.query(sql, rowMapper(), excludeId).stream().findFirst();
    }
}