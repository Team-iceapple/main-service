package org.example.iceapplehome2.service;

import lombok.RequiredArgsConstructor;
import org.example.iceapplehome2.dto.request.AdminVideoEnableRequest;
import org.example.iceapplehome2.dto.request.AdminVideoMetaUpdateRequest;
import org.example.iceapplehome2.dto.request.AdminVideoUpdateRequest;
import org.example.iceapplehome2.dto.response.AdminVideoResponse;
import org.example.iceapplehome2.dto.response.VideoPlaylistItemResponse;
import org.example.iceapplehome2.entity.Video;
import org.example.iceapplehome2.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository repo;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static String safeExt(String name) {
        int i = (name == null) ? -1 : name.lastIndexOf('.');
        if (i < 0) return "";
        String ext = name.substring(i).toLowerCase();
        if (ext.matches("\\.(mp4|webm|mov|m4v|avi)$")) return ext;
        return ".mp4";
    }

    @Transactional
    public AdminVideoResponse uploadAndCreate(MultipartFile file, String title) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("파일이 없습니다.");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("제목을 입력하세요.");

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String filename = "v_" + UUID.randomUUID() + safeExt(file.getOriginalFilename());
            Path target = dir.resolve(filename).normalize();

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            boolean hasCurrent = repo.findCurrent().isPresent();

            Video video = new Video();
            video.setFilePath(filename);
            video.setTitle(title);
            video.setCurrent(!hasCurrent);

            String id = repo.insert(video, !hasCurrent);

            Video saved = repo.findById(id).orElseThrow(() -> new IllegalStateException("저장 실패"));
            return toAdminResp(saved, /*enabled*/ true, /*weight*/ 0);

        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 실패: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public AdminVideoResponse getCurrent() {
        var current = repo.findAllForAdmin().stream()
                .filter(v -> Boolean.TRUE.equals(v.isCurrent()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("현재 영상이 없습니다."));
        return toAdminResp(current);
    }

    // 단건 조회 (new)
    @Transactional(readOnly = true)
    public AdminVideoResponse get(String id) {
        Video v = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 영상입니다."));
        return toAdminResp(v);
    }

    @Transactional(readOnly = true)
    public List<AdminVideoResponse> list() {
        return repo.findAllForAdmin().stream()
                .map(this::toAdminResp)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        Video v = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));
        if (Boolean.TRUE.equals(v.isCurrent())) {
            throw new IllegalStateException("CURRENT_VIDEO_CANNOT_BE_DELETED");
        }

        int deleted = repo.deleteNonCurrent(id);
        if (deleted != 1) {
            throw new IllegalStateException("CURRENT_VIDEO_CANNOT_BE_DELETED");
        }

        try {
            Path p = Paths.get(uploadDir).resolve(v.getFilePath()).normalize();
            Files.deleteIfExists(p);
        } catch (Exception ignore) {}
    }

    // 부분 업데이트 (current/enable/meta 통합)
    @Transactional
    public AdminVideoResponse updatePartial(String id, AdminVideoUpdateRequest req) {
        Video v = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 영상입니다."));

        if (req.getCurrent() != null) {
            if (Boolean.TRUE.equals(req.getCurrent())) {
                repo.clearCurrent();
                int updated = repo.makeCurrentAndEnable(id);
                if (updated != 1) {
                    throw new IllegalStateException("대표 지정 실패");
                }
                v.setCurrent(true);
                v.setEnabled(true);
            } else {
                repo.updateCurrent(id, false); // 이 메서드 없으면 추가 필요
                v.setCurrent(false);
            }
        }

        if (req.getEnabled() != null) {
            if (Boolean.FALSE.equals(req.getEnabled()) && (v.isCurrent() || Boolean.TRUE.equals(req.getCurrent()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "대표 영상은 비활성화할 수 없습니다.");
            }
            repo.updateEnabledSafe(id, req.getEnabled());
            v.setEnabled(req.getEnabled());
        }

        if (req.getTitle() != null || req.getWeight() != null || req.getPlaybackRate() != null) {
            repo.updateMetaBasic(id, req.getTitle(), req.getWeight(), req.getPlaybackRate());
        }

        Video updated = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("갱신 조회 실패"));
        return toAdminResp(updated);
    }


    @Transactional(readOnly = true)
    public List<VideoPlaylistItemResponse> getPlaylist(boolean includeCurrent, Integer limit) {
        return repo.findPlaylist(includeCurrent, limit).stream()
                .map(v -> new VideoPlaylistItemResponse(
                        v.getId(),
                        v.getTitle(),
                        "/media/" + v.getFilePath(),
                        v.getWeight() == null ? 0 : v.getWeight(),
                        v.getPlaybackRate() == null ? 1.0 : v.getPlaybackRate()
                ))
                .toList();
    }

    @Transactional
    public AdminVideoResponse makeCurrent(String id) {
        repo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));

        repo.clearCurrent();
        int updated = repo.makeCurrentAndEnable(id);
        if (updated != 1) throw new IllegalStateException("대표 지정 실패");

        Video updatedV = repo.findById(id).orElseThrow(() -> new IllegalStateException("갱신 조회 실패"));
        return toAdminResp(updatedV, /*enabled*/ true, /*weight*/ 0);
    }



    @Transactional
    public AdminVideoResponse setEnable(String id, AdminVideoEnableRequest req) {
        repo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));

        int updated = repo.updateEnabledSafe(id, req.enabled());
        if (updated != 1) {
            throw new IllegalStateException("CURRENT_VIDEO_MUST_BE_ENABLED");
        }

        var updatedAdmin = repo.findAllForAdmin().stream()
                .filter(v -> id.equals(v.getId()))
                .findFirst()
                .orElseThrow();
        return toAdminResp(updatedAdmin);
    }

    @Transactional
    public AdminVideoResponse updateMeta(String id, AdminVideoMetaUpdateRequest req) {
        repo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));
        repo.updateMetaBasic(id, req.title(), req.weight(), req.playbackRate());

        var updatedAdmin = repo.findAllForAdmin().stream()
                .filter(v -> id.equals(v.getId()))
                .findFirst()
                .orElseThrow();

        return toAdminResp(updatedAdmin);
    }

    // ---- mapper ----

    private AdminVideoResponse toAdminResp(Video v) {
        String fileUrl = "/media/" + v.getFilePath();
        int weight = (v.getWeight() == null) ? 0 : v.getWeight();
        Double rate = (v.getPlaybackRate() == null) ? 1.0 : v.getPlaybackRate();
        return new AdminVideoResponse(
                v.getId(),
                v.getTitle(),
                Boolean.TRUE.equals(v.isCurrent()),
                v.getFilePath(),
                fileUrl,
                v.isEnabled(),
                weight,
                rate
        );
    }

    private AdminVideoResponse toAdminResp(Video v, boolean enabled, int weight) {
        String fileUrl = "/media/" + v.getFilePath();
        Double rate = (v.getPlaybackRate() == null) ? 1.0 : v.getPlaybackRate();
        return new AdminVideoResponse(
                v.getId(),
                v.getTitle(),
                Boolean.TRUE.equals(v.isCurrent()),
                v.getFilePath(),
                fileUrl,
                enabled,
                weight,
                rate
        );
    }
}