package org.example.iceapplehome2.web;

import lombok.RequiredArgsConstructor;
import org.example.iceapplehome2.dto.request.VideoPlaybackSettingsUpdateRequest;
import org.example.iceapplehome2.dto.response.VideoPlaybackSettingsResponse;
import org.example.iceapplehome2.service.SettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    private final SettingsService service;

    @GetMapping("/video-playback")
    public VideoPlaybackSettingsResponse get() {
        return service.getPlayback();
    }

    @PutMapping("/video-playback")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody VideoPlaybackSettingsUpdateRequest req) {
        service.updatePlayback(req);
    }
    // 200으로 바꾸고 싶으면 위 메서드 반환타입을 VideoPlaybackSettingsResponse로 바꾸고
    // return service.updatePlayback(req); 하면 됨
}