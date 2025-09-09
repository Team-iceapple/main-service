//package org.example.iceapplehome2.repository.jdbc;
//
//import org.example.iceapplehome2.entity.Video;
//import org.example.iceapplehome2.repository.VideoRepository;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowMapper;
//
//import javax.sql.DataSource;
//import java.sql.Timestamp;
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.*;
//
//public class JdbcVideoRepository implements VideoRepository {
//
//    private final JdbcTemplate jdbc;
//
//    public JdbcVideoRepository(DataSource dataSource) {
//        this.jdbc = new JdbcTemplate(dataSource);
//    }
//
//
//    private RowMapper<Video> rowMapper() {
//        return (rs, rowNum) -> {
//            Timestamp ts = rs.getTimestamp("created_at");
//            OffsetDateTime odt = ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;
//            return new Video(
//                    rs.getString("id"),
//                    rs.getString("file_path"),
//                    rs.getString("title"),
//                    rs.getBoolean("is_current"),
//                    odt
//            );
//        };
//    }
//
//    @Override
//    public List<Video> findAllOrderByCreatedAtDesc() {
//        String sql = "SELECT id, file_path, title, is_current, created_at " +
//                "FROM home_video ORDER BY created_at DESC";
//        return jdbc.query(sql, rowMapper());
//    }
//
//    @Override
//    public Optional<Video> findCurrent() {
//        String sql = "SELECT id, file_path, title, is_current, created_at " +
//                "FROM home_video WHERE is_current = true LIMIT 1";
//        return jdbc.query(sql, rowMapper()).stream().findFirst();
//    }
//
//    @Override
//    public Optional<Video> findById(String id) {
//        String sql = "SELECT id, file_path, title, is_current, created_at " +
//                "FROM home_video WHERE id = ?";
//        return jdbc.query(sql, rowMapper(), id).stream().findFirst();
//    }
//
//    @Override
//    public String insert(Video video, boolean current) {
//        String sql = "INSERT INTO home_video (id, file_path, title, is_current) " +
//                "VALUES ('v_' || gen_random_uuid()::text, ?, ?, ?) RETURNING id";
//        return jdbc.queryForObject(sql, String.class,
//                video.getFilePath(), video.getTitle(), current);
//    }
//
//    @Override
//    public int unsetAllCurrent() {
//        String sql = "UPDATE home_video SET is_current = false WHERE is_current = true";
//        return jdbc.update(sql);
//    }
//
//    @Override
//    public int setCurrentById(String id) {
//        String sql = "UPDATE home_video SET is_current = true WHERE id = ?";
//        return jdbc.update(sql, id);
//    }
//
//    @Override
//    public void deleteById(String id) {
//        jdbc.update("DELETE FROM home_video WHERE id = ?", id);
//    }
//
//    @Override
//    public Optional<Video> findLatestExcluding(String excludeId) {
//        String sql = "SELECT id, file_path, title, is_current, created_at " +
//                "FROM home_video WHERE id <> ? ORDER BY created_at DESC LIMIT 1";
//        return jdbc.query(sql, rowMapper(), excludeId).stream().findFirst();
//    }
//}

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

    // ----- helpers -----
    private static OffsetDateTime toOdt(Timestamp ts) {
        return ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;
    }

    private RowMapper<Video> baseRowMapper() {
        return (rs, rowNum) -> {
            Video v = new Video();
            v.setId(rs.getString("id"));
            v.setFilePath(rs.getString("file_path"));
            v.setTitle(rs.getString("title"));
            v.setCurrent(rs.getBoolean("is_current"));
            v.setCreatedAt(toOdt(rs.getTimestamp("created_at")));
            // playlist 필드는 셀렉트에 없으니 기본값(null/false)
            return v;
        };
    }

    private RowMapper<Video> playlistRowMapper() {
        return (rs, rowNum) -> {
            Video v = new Video();
            v.setId(rs.getString("id"));
            v.setFilePath(rs.getString("file_path"));
            v.setTitle(rs.getString("title"));
            v.setCurrent(rs.getBoolean("is_current"));
            v.setCreatedAt(toOdt(rs.getTimestamp("created_at")));
            v.setEnabled(rs.getBoolean("enabled"));
            v.setWeight((Integer) rs.getObject("weight"));
            v.setDurationSec((Integer) rs.getObject("duration_sec"));
            v.setStartsAt(toOdt(rs.getTimestamp("starts_at")));
            v.setEndsAt(toOdt(rs.getTimestamp("ends_at")));
            return v;
        };
    }

    // ----- 기존 메서드 -----

    @Override
    public List<Video> findAllOrderByCreatedAtDesc() {
        String sql = """
            SELECT id, file_path, title, is_current, created_at
            FROM home_video
            ORDER BY created_at DESC
            """;
        return jdbc.query(sql, baseRowMapper());
    }

    @Override
    public Optional<Video> findCurrent() {
        String sql = """
            SELECT id, file_path, title, is_current, created_at
            FROM home_video
            WHERE is_current = TRUE
            LIMIT 1
            """;
        return jdbc.query(sql, baseRowMapper()).stream().findFirst();
    }

    @Override
    public Optional<Video> findById(String id) {
        String sql = """
            SELECT id, file_path, title, is_current, created_at
            FROM home_video
            WHERE id = ?
            """;
        return jdbc.query(sql, baseRowMapper(), id).stream().findFirst();
    }

    @Override
    public String insert(Video video, boolean current) {
        String sql = """
            INSERT INTO home_video (id, file_path, title, is_current)
            VALUES ('v_' || gen_random_uuid()::text, ?, ?, ?)
            RETURNING id
            """;
        return jdbc.queryForObject(sql, String.class,
                video.getFilePath(), video.getTitle(), current);
    }

    @Override
    public int unsetAllCurrent() {
        String sql = "UPDATE home_video SET is_current = FALSE WHERE is_current = TRUE";
        return jdbc.update(sql);
    }

    @Override
    public int setCurrentById(String id) {
        String sql = "UPDATE home_video SET is_current = TRUE WHERE id = ?";
        return jdbc.update(sql, id);
    }

    @Override
    public void deleteById(String id) {
        jdbc.update("DELETE FROM home_video WHERE id = ?", id);
    }

    @Override
    public Optional<Video> findLatestExcluding(String excludeId) {
        String sql = """
            SELECT id, file_path, title, is_current, created_at
            FROM home_video
            WHERE id <> ?
            ORDER BY created_at DESC
            LIMIT 1
            """;
        return jdbc.query(sql, baseRowMapper(), excludeId).stream().findFirst();
    }

    // ----- 추가: 슬라이드/플레이리스트 -----

    @Override
    public List<Video> findPlaylist(OffsetDateTime now) {
        String sql = """
            SELECT id, file_path, title, is_current, created_at,
                   enabled, weight, duration_sec, starts_at, ends_at
            FROM home_video
            WHERE enabled = TRUE
              AND (starts_at IS NULL OR starts_at <= ?)
              AND (ends_at   IS NULL OR ends_at   >= ?)
            ORDER BY weight DESC, created_at DESC
            """;
        return jdbc.query(sql, playlistRowMapper(), now, now);
    }

    @Override
    public void updateEnable(String id, boolean enabled) {
        jdbc.update("UPDATE home_video SET enabled = ? WHERE id = ?", enabled, id);
    }

    @Override
    public void updateMeta(String id, String title, Integer weight, Integer durationSec,
                           OffsetDateTime startsAt, OffsetDateTime endsAt) {
        String sql = """
            UPDATE home_video SET
                title        = COALESCE(?, title),
                weight       = COALESCE(?, weight),
                duration_sec = COALESCE(?, duration_sec),
                starts_at    = COALESCE(?, starts_at),
                ends_at      = COALESCE(?, ends_at)
            WHERE id = ?
            """;
        jdbc.update(sql, title, weight, durationSec, startsAt, endsAt, id);
    }
}