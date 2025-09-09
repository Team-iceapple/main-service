package org.example.iceapplehome2.repository;

import org.example.iceapplehome2.entity.Video;

import java.time.OffsetDateTime;
import java.util.*;

public interface VideoRepository {
    List<Video> findAllOrderByCreatedAtDesc();
    Optional<Video> findCurrent();
    Optional<Video> findById(String id);
    String insert(Video video, boolean current);     // filePath, title 저장
    int unsetAllCurrent();
    int setCurrentById(String id);
    void deleteById(String id);
    Optional<Video> findLatestExcluding(String excludeId);

List<Video> findPlaylist(OffsetDateTime now);
    void updateEnable(String id, boolean enabled);
    void updateMeta(String id, String title, Integer weight, Integer durationSec,
                    OffsetDateTime startsAt, OffsetDateTime endsAt);
}