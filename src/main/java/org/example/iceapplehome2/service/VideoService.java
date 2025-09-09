//package org.example.iceapplehome2.service;
//
//import lombok.RequiredArgsConstructor;
//import org.example.iceapplehome2.dto.response.AdminVideoResponse;
//import org.example.iceapplehome2.entity.Video;
//import org.example.iceapplehome2.repository.VideoRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.*;
//import java.nio.file.*;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class VideoService {
//
//    private final VideoRepository repo;
//
//    @Value("${app.upload.dir}")
//    private String uploadDir;
//
//    private static String safeExt(String name) {
//        int i = (name == null) ? -1 : name.lastIndexOf('.');
//        if (i < 0) return "";
//        String ext = name.substring(i).toLowerCase();
//        // 허용 확장자만 통과
//        if (ext.matches("\\.(mp4|webm|mov|m4v|avi)$")) return ext;
//        return ".mp4"; // 기본
//    }
//
//    @Transactional
//    public AdminVideoResponse uploadAndCreate(MultipartFile file, String title) {
//        if (file == null || file.isEmpty()) throw new IllegalArgumentException("파일이 없습니다.");
//        if (title == null || title.isBlank()) throw new IllegalArgumentException("제목을 입력하세요.");
//
//        try {
//            // 디렉토리 보장
//            Path dir = Paths.get(uploadDir);
//            Files.createDirectories(dir);
//
//            String filename = "v_" + UUID.randomUUID() + safeExt(file.getOriginalFilename());
//            Path target = dir.resolve(filename).normalize();
//
//            // 업로드 저장
//            try (InputStream in = file.getInputStream()) {
//                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
//            }
//
//            // DB: 현재 영상 단일 유지
//            repo.unsetAllCurrent();
//            String id = repo.insert(new Video(null, filename, title, true, null), true);
//
//            Video saved = repo.findById(id).orElseThrow(() -> new IllegalStateException("저장 실패"));
//            return toResp(saved);
//
//        } catch (IOException e) {
//            throw new IllegalStateException("파일 저장 실패: " + e.getMessage(), e);
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public AdminVideoResponse getCurrent() {
//        return repo.findCurrent().map(this::toResp)
//                .orElseThrow(() -> new IllegalStateException("현재 영상이 없습니다."));
//    }
//
//    @Transactional(readOnly = true)
//    public List<AdminVideoResponse> list() {
//        return repo.findAllOrderByCreatedAtDesc().stream().map(this::toResp).collect(Collectors.toList());
//    }
//
//    @Transactional
//    public void delete(String id) {
//        Video v = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));
//        // 파일 삭제 시도 (없으면 무시)
//        try {
//            Path p = Paths.get(uploadDir).resolve(v.getFilePath()).normalize();
//            Files.deleteIfExists(p);
//        } catch (Exception ignore) {}
//
//        boolean wasCurrent = v.isCurrent();
//        repo.deleteById(id);
//        if (wasCurrent) {
//            repo.unsetAllCurrent();
//            repo.findLatestExcluding(id).ifPresent(latest -> repo.setCurrentById(latest.getId()));
//        }
//    }
//
//    private AdminVideoResponse toResp(Video v) {
//        String fileUrl = "/media/" + v.getFilePath(); // 정적 서빙 매핑 경로
//        return new AdminVideoResponse(v.getId(), v.getTitle(), v.isCurrent(), v.getFilePath(), fileUrl);
//    }
//
//    @Transactional
//    public AdminVideoResponse makeCurrent(String id) {
//        Video v = repo.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));
//
//        repo.unsetAllCurrent();
//
//        repo.setCurrentById(id);
//
//        Video updated = repo.findById(id)
//                .orElseThrow(() -> new IllegalStateException("갱신된 영상을 찾을 수 없습니다."));
//        return toResp(updated);
//    }
//}
package org.example.iceapplehome2.service;

import lombok.RequiredArgsConstructor;
import org.example.iceapplehome2.dto.request.AdminVideoEnableRequest;
import org.example.iceapplehome2.dto.request.AdminVideoMetaUpdateRequest;
import org.example.iceapplehome2.dto.response.AdminVideoResponse;
import org.example.iceapplehome2.dto.response.VideoPlaylistItemResponse;
import org.example.iceapplehome2.entity.Video;
import org.example.iceapplehome2.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.OffsetDateTime;
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

            // 기존 current가 있으면 새 영상은 current=false
            // 아무 것도 없으면 첫 업로드는 current=true
            boolean hasCurrent = repo.findCurrent().isPresent();

            Video video = new Video();
            video.setFilePath(filename);
            video.setTitle(title);
            video.setCurrent(!hasCurrent);

            String id = repo.insert(video, !hasCurrent);

            Video saved = repo.findById(id).orElseThrow(() -> new IllegalStateException("저장 실패"));
            return toResp(saved);

        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 실패: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public AdminVideoResponse getCurrent() {
        return repo.findCurrent().map(this::toResp)
                .orElseThrow(() -> new IllegalStateException("현재 영상이 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<AdminVideoResponse> list() {
        return repo.findAllOrderByCreatedAtDesc().stream().map(this::toResp).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String id) {
        Video v = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));
        try {
            Path p = Paths.get(uploadDir).resolve(v.getFilePath()).normalize();
            Files.deleteIfExists(p);
        } catch (Exception ignore) {}

        boolean wasCurrent = v.isCurrent();
        repo.deleteById(id);
        if (wasCurrent) {
            repo.unsetAllCurrent();
            repo.findLatestExcluding(id).ifPresent(latest -> repo.setCurrentById(latest.getId()));
        }
    }

    private AdminVideoResponse toResp(Video v) {
        String fileUrl = "/media/" + v.getFilePath();
        return new AdminVideoResponse(v.getId(), v.getTitle(), v.isCurrent(), v.getFilePath(), fileUrl);
    }

    @Transactional
    public AdminVideoResponse makeCurrent(String id) {
        Video v = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));
        repo.unsetAllCurrent();
        repo.setCurrentById(id);
        Video updated = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("갱신된 영상을 찾을 수 없습니다."));
        return toResp(updated);
    }

    @Transactional(readOnly = true)
    public List<VideoPlaylistItemResponse> getPlaylist() {
        var now = OffsetDateTime.now();
        return repo.findPlaylist(now).stream()
                .map(v -> new VideoPlaylistItemResponse(
                        v.getId(),
                        v.getTitle(),
                        "/media/" + v.getFilePath(),
                        v.getDurationSec(),
                        v.getWeight()
                ))
                .toList();
    }

    @Transactional
    public void setEnable(String id, AdminVideoEnableRequest req) {
        repo.updateEnable(id, req.enabled());
    }

    @Transactional
    public AdminVideoResponse updateMeta(String id, AdminVideoMetaUpdateRequest req) {
        repo.updateMeta(id, req.title(), req.weight(), req.durationSec(), req.startsAt(), req.endsAt());
        var updated = repo.findById(id).orElseThrow();
        return toResp(updated);
    }
}