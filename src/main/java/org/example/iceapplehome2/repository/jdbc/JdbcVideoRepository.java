//
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
//    // ----- helpers -----
//    private static OffsetDateTime toOdt(Timestamp ts) {
//        return ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;
//    }
//
//    private RowMapper<Video> baseRowMapper() {
//        return (rs, rowNum) -> {
//            Video v = new Video();
//            v.setId(rs.getString("id"));
//            v.setFilePath(rs.getString("file_path"));
//            v.setTitle(rs.getString("title"));
//            v.setCurrent(rs.getBoolean("is_current"));
//            v.setCreatedAt(toOdt(rs.getTimestamp("created_at")));
//            return v;
//        };
//    }
//
//    private RowMapper<Video> adminRowMapper() {
//        return (rs, rowNum) -> {
//            Video v = new Video();
//            v.setId(rs.getString("id"));
//            v.setFilePath(rs.getString("file_path"));
//            v.setTitle(rs.getString("title"));
//            v.setCurrent(rs.getBoolean("is_current"));
//            v.setEnabled(rs.getBoolean("enabled"));
//            v.setWeight((Integer) rs.getObject("weight"));
//            v.setPlaybackRate((Double) rs.getObject("playback_rate"));
//            v.setCreatedAt(toOdt(rs.getTimestamp("created_at")));
//            return v;
//        };
//    }
//
//    private RowMapper<Video> playlistRowMapper() {
//        return (rs, rowNum) -> {
//            Video v = new Video();
//            v.setId(rs.getString("id"));
//            v.setFilePath(rs.getString("file_path"));
//            v.setTitle(rs.getString("title"));
//            v.setCurrent(rs.getBoolean("is_current"));
//            v.setEnabled(rs.getBoolean("enabled"));
//            v.setWeight((Integer) rs.getObject("weight"));
//            v.setPlaybackRate((Double) rs.getObject("playback_rate"));
//            v.setCreatedAt(toOdt(rs.getTimestamp("created_at")));
//            return v;
//        };
//    }
//    @Override
//    public List<Video> findAllForAdmin() {
//        String sql = """
//            SELECT id, file_path, title, is_current, enabled, weight, playback_rate, created_at
//            FROM home_video
//            ORDER BY is_current DESC, created_at DESC
//            """;
//        return jdbc.query(sql, adminRowMapper());
//    }
//
//    @Override
//    public List<Video> findPlaylist(boolean includeCurrent, Integer limit) {
//        String offsetSql = includeCurrent ? "" : "OFFSET 1";
//        String limitSql  = (limit != null) ? "LIMIT ?" : "";
//
//        String sql = ("""
//        SELECT id, file_path, title, is_current, enabled, weight, playback_rate, created_at
//        FROM home_video
//        WHERE enabled = TRUE
//        ORDER BY weight ASC, created_at DESC
//        %s
//        %s
//        """).formatted(limitSql, offsetSql); // LIMIT 먼저, OFFSET 나중
//
//        if (limit != null) {
//            return jdbc.query(sql, playlistRowMapper(), limit);
//        } else {
//            return jdbc.query(sql, playlistRowMapper());
//        }
//    }
//
//    @Override
//    public void updateMetaBasic(String id, String title, Integer weight, Double playbackRate) {
//        String sql = """
//            UPDATE home_video SET
//                title         = COALESCE(?, title),
//                weight        = COALESCE(?, weight),
//                playback_rate = COALESCE(?, playback_rate)
//            WHERE id = ?
//            """;
//        jdbc.update(sql, title, weight, playbackRate, id);
//    }
//
//
//
//    @Override
//    public List<Video> findAllOrderByCreatedAtDesc() {
//        String sql = """
//            SELECT id, file_path, title, is_current, created_at
//            FROM home_video
//            ORDER BY created_at DESC
//            """;
//        return jdbc.query(sql, baseRowMapper());
//    }
//
//    @Override
//    public Optional<Video> findCurrent() {
//        String sql = """
//            SELECT id, file_path, title, is_current, created_at
//            FROM home_video
//            WHERE is_current = TRUE
//            LIMIT 1
//            """;
//        return jdbc.query(sql, baseRowMapper()).stream().findFirst();
//    }
//
//    @Override
//    public Optional<Video> findById(String id) {
//        String sql = """
//        SELECT id, file_path, title, is_current, enabled, weight, playback_rate, created_at
//        FROM home_video
//        WHERE id = ?
//        """;
//        return jdbc.query(sql, adminRowMapper(), id).stream().findFirst();
//    }
//
//    @Override
//    public String insert(Video video, boolean current) {
//        String sql = """
//            INSERT INTO home_video (id, file_path, title, is_current)
//            VALUES ('v_' || gen_random_uuid()::text, ?, ?, ?)
//            RETURNING id
//            """;
//        return jdbc.queryForObject(sql, String.class,
//                video.getFilePath(), video.getTitle(), current);
//    }
//
//    @Override
//    public int unsetAllCurrent() {
//        String sql = "UPDATE home_video SET is_current = FALSE WHERE is_current = TRUE";
//        return jdbc.update(sql);
//    }
//
//    @Override
//    public int setCurrentById(String id) {
//        String sql = "UPDATE home_video SET is_current = TRUE WHERE id = ?";
//        return jdbc.update(sql, id);
//    }
//
//    @Override
//    public int updateCurrent(String id, boolean current) {
//        String sql = "UPDATE home_video SET is_current = ? WHERE id = ?";
//        return jdbc.update(sql, current, id);
//    }
//
//    @Override
//    public void deleteById(String id) {
//        jdbc.update("DELETE FROM home_video WHERE id = ?", id);
//    }
//
//    @Override
//    public Optional<Video> findLatestExcluding(String excludeId) {
//        String sql = """
//            SELECT id, file_path, title, is_current, created_at
//            FROM home_video
//            WHERE id <> ?
//            ORDER BY created_at DESC
//            LIMIT 1
//            """;
//        return jdbc.query(sql, baseRowMapper(), excludeId).stream().findFirst();
//    }
//
//
//    @Override
//    public void updateEnable(String id, boolean enabled) {
//        jdbc.update("UPDATE home_video SET enabled = ? WHERE id = ?", enabled, id);
//    }
//
////    @Override
////    public List<Video> findAllForAdmin() {
////        String sql = """
////            SELECT id, file_path, title, is_current, enabled, weight, created_at
////            FROM home_video
////            ORDER BY is_current DESC, created_at DESC
////            """;
////        return jdbc.query(sql, adminRowMapper());
////    }
//
//    @Override
//    public int clearCurrent() {
//        return jdbc.update("UPDATE home_video SET is_current = FALSE WHERE is_current = TRUE");
//    }
//
//    @Override
//    public int makeCurrentAndEnable(String id) {
//        String sql = """
//            UPDATE home_video
//            SET is_current = TRUE, enabled = TRUE
//            WHERE id = ?
//            """;
//        return jdbc.update(sql, id);
//    }
//
//    @Override
//    public int updateEnabledSafe(String id, boolean enabled) {
//        String sql = """
//            UPDATE home_video
//            SET enabled = ?
//            WHERE id = ?
//              AND (NOT is_current OR ? = TRUE)
//            """;
//        return jdbc.update(sql, enabled, id, enabled);
//    }
//
//    @Override
//    public int deleteNonCurrent(String id) {
//        String sql = "DELETE FROM home_video WHERE id = ? AND is_current = FALSE";
//        return jdbc.update(sql, id);
//    }
//
////    @Override
////    public List<Video> findPlaylist(boolean includeCurrent, Integer limit) {
////        String whereExtra = includeCurrent ? "" : "AND is_current = FALSE";
////        String limitSql   = (limit != null) ? "LIMIT ?" : "";
////
////        String sql = ("""
////            SELECT id, file_path, title, is_current, enabled, weight, created_at
////            FROM home_video
////            WHERE enabled = TRUE
////            %s
////            ORDER BY is_current DESC, weight DESC, created_at DESC
////            %s
////            """).formatted(whereExtra, limitSql);
////
////        if (limit != null) {
////            return jdbc.query(sql, playlistRowMapper(), limit);
////        } else {
////            return jdbc.query(sql, playlistRowMapper());
////        }
////    }
//
////    @Override
////    public void updateMetaBasic(String id, String title, Integer weight) {
////        String sql = """
////            UPDATE home_video SET
////                title  = COALESCE(?, title),
////                weight = COALESCE(?, weight)
////            WHERE id = ?
////            """;
////        jdbc.update(sql, title, weight, id);
////    }
//}

package org.example.iceapplehome2.repository.jdbc;

import org.example.iceapplehome2.entity.Video;
import org.example.iceapplehome2.repository.VideoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

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

    // helpers
    private static OffsetDateTime toOdt(Timestamp ts) {
        return ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;
    }

    private RowMapper<Video> baseRowMapper() {
        return (rs, rowNum) -> {
            Video v = new Video();
            v.setId(rs.getString("id"));
            v.setFilePath(rs.getString("file_path"));
            v.setTitle(rs.getString("title"));
            // 대표 개념 제거: 항상 false 로 둠
            v.setCurrent(false);
            v.setEnabled((Boolean) rs.getObject("enabled"));
            v.setWeight((Integer) rs.getObject("weight"));
            v.setPlaybackRate((Double) rs.getObject("playback_rate"));
            v.setCreatedAt(toOdt(rs.getTimestamp("created_at")));
            return v;
        };
    }

    private RowMapper<Video> adminRowMapper() {
        return baseRowMapper();
    }

    private RowMapper<Video> playlistRowMapper() {
        return baseRowMapper();
    }

    // 조회

    @Override
    public List<Video> findAllForAdmin() {
        String sql = """
            SELECT id, file_path, title, enabled, weight, playback_rate, created_at
            FROM home_video
            ORDER BY weight ASC, created_at DESC
            """;
        return jdbc.query(sql, adminRowMapper());
    }

    @Override
    public List<Video> findPlaylist(boolean includeCurrent, Integer limit) {
        // 대표 개념 제거 → includeCurrent는 의미 없음(무시)
        String limitSql = (limit != null) ? "LIMIT ?" : "";
        String sql = ("""
            SELECT id, file_path, title, enabled, weight, playback_rate, created_at
            FROM home_video
            WHERE enabled = TRUE
            ORDER BY weight ASC, created_at DESC
            %s
            """).formatted(limitSql);

        if (limit != null) {
            return jdbc.query(sql, playlistRowMapper(), limit);
        } else {
            return jdbc.query(sql, playlistRowMapper());
        }
    }

    @Override
    public List<Video> findAllOrderByCreatedAtDesc() {
        String sql = """
            SELECT id, file_path, title, enabled, weight, playback_rate, created_at
            FROM home_video
            ORDER BY created_at DESC
            """;
        return jdbc.query(sql, baseRowMapper());
    }

    @Override
    public Optional<Video> findCurrent() {
        // 대표 영상 개념 제거. 그래도 호출될 수 있으니 플레이리스트 상위 1개를 반환(없으면 empty)
        String sql = """
            SELECT id, file_path, title, enabled, weight, playback_rate, created_at
            FROM home_video
            WHERE enabled = TRUE
            ORDER BY weight ASC, created_at DESC
            LIMIT 1
            """;
        return jdbc.query(sql, baseRowMapper()).stream().findFirst();
    }

    @Override
    public Optional<Video> findById(String id) {
        String sql = """
            SELECT id, file_path, title, enabled, weight, playback_rate, created_at
            FROM home_video
            WHERE id = ?
            """;
        return jdbc.query(sql, adminRowMapper(), id).stream().findFirst();
    }

    // --------------------------------------------------------------------
    // 생성/수정/삭제
    // --------------------------------------------------------------------

    @Override
    @Transactional
    public String insert(Video video, boolean current) {
        // 대표 개념 없음: 항상 꼬리(next) 위치로 삽입
        Integer next = jdbc.queryForObject("""
            SELECT COALESCE(MAX(weight), -1) + 1
            FROM home_video
            WHERE enabled = TRUE
            """, Integer.class);

        return jdbc.queryForObject("""
            INSERT INTO home_video (id, file_path, title, enabled, weight)
            VALUES ('v_' || gen_random_uuid()::text, ?, ?, TRUE, ?)
            RETURNING id
            """, String.class, video.getFilePath(), video.getTitle(), next);
    }

    @Override
    public void updateMetaBasic(String id, String title, Integer weight, Double playbackRate) {
        String sql = """
            UPDATE home_video SET
                title         = COALESCE(?, title),
                weight        = COALESCE(?, weight),
                playback_rate = COALESCE(?, playback_rate)
            WHERE id = ?
            """;
        jdbc.update(sql, title, weight, playbackRate, id);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        int n = jdbc.update("DELETE FROM home_video WHERE id = ?", id);
        if (n == 0) return;

        // 활성 영상 weight 압축(0,1,2…)
        compactWeights();
    }

    @Override
    public Optional<Video> findLatestExcluding(String excludeId) {
        String sql = """
            SELECT id, file_path, title, enabled, weight, playback_rate, created_at
            FROM home_video
            WHERE id <> ?
            ORDER BY created_at DESC
            LIMIT 1
            """;
        return jdbc.query(sql, baseRowMapper(), excludeId).stream().findFirst();
    }

    @Override
    @Transactional
    public void updateEnable(String id, boolean enabled) {
        if (!enabled) {
            jdbc.update("UPDATE home_video SET enabled = FALSE WHERE id = ?", id);
            compactWeights();
        } else {
            // 활성화 시 유니크(weight) 충돌 방지 위해 꼬리(next)에 붙임
            Integer next = jdbc.queryForObject("""
                SELECT COALESCE(MAX(weight), -1) + 1
                FROM home_video
                WHERE enabled = TRUE
                """, Integer.class);
            jdbc.update("""
                UPDATE home_video
                SET enabled = TRUE, weight = ?
                WHERE id = ?
                """, next, id);
        }
    }

    // 과거 current 관련 API: no-op

    @Override
    public int unsetAllCurrent() {
        return 0;
    }

    @Override
    public int setCurrentById(String id) {
        // 대표 개념 제거: no-op
        return 0;
    }

    @Override
    public int updateCurrent(String id, boolean current) {
        // 대표 개념 제거: no-op
        return 0;
    }

    @Override
    public int clearCurrent() {
        return 0;
    }

    @Override
    public int makeCurrentAndEnable(String id) {
        // 대표 개념 제거: no-op
        return 0;
    }

    @Override
    public int updateEnabledSafe(String id, boolean enabled) {
        // 과거: (NOT is_current OR ?=TRUE) 가드 → 제거
        int before = Optional.ofNullable(
                jdbc.queryForObject("SELECT CASE WHEN enabled THEN 1 ELSE 0 END FROM home_video WHERE id = ?", Integer.class, id)
        ).orElse(0);
        updateEnable(id, enabled);
        int after = enabled ? 1 : 0;
        return (before == after) ? 0 : 1;
        // (정확히 과거 시그니처 유지)
    }

    @Override
    public int deleteNonCurrent(String id) {
        // 대표/비대표 구분 없음 → 일반 삭제와 동일
        deleteById(id);
        return 1;
    }

    // internal
    private void compactWeights() {
        jdbc.update("""
            WITH ordered AS (
              SELECT id, ROW_NUMBER() OVER (ORDER BY weight ASC) - 1 AS new_w
              FROM home_video
              WHERE enabled = TRUE
            )
            UPDATE home_video h
            SET weight = o.new_w
            FROM ordered o
            WHERE h.id = o.id
              AND h.weight <> o.new_w
            """);
    }
}