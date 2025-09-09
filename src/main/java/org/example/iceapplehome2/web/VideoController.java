package org.example.iceapplehome2.web;

import lombok.RequiredArgsConstructor;
import org.example.iceapplehome2.dto.response.AdminVideoResponse;
import org.example.iceapplehome2.service.VideoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos") // 일반 사용자용
public class VideoController {

    private final VideoService service;

    // 공개: 전체 목록 (현재 구현에선 전체 반환)
    @GetMapping
    public List<AdminVideoResponse> list() {
        return service.list();
    }

    // 공개: 현재 영상
    @GetMapping("/current")
    public AdminVideoResponse current() {
        return service.getCurrent();
    }
}