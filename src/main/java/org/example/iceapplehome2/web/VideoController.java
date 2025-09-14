package org.example.iceapplehome2.web;

import lombok.RequiredArgsConstructor;
import org.example.iceapplehome2.dto.response.AdminVideoResponse;
import org.example.iceapplehome2.dto.response.VideoPlaylistItemResponse;
import org.example.iceapplehome2.service.VideoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos")
public class VideoController {

    private final VideoService service;

//    @GetMapping
//    public List<AdminVideoResponse> list() {
//        return service.list();
//    }

//    @GetMapping("/current")
//    public AdminVideoResponse current() {
//        return service.getCurrent();
//    }

    @GetMapping("/playlist")
    public List<VideoPlaylistItemResponse> playlist(
            @RequestParam(defaultValue = "true") boolean includeCurrent,
            @RequestParam(required = false) Integer limit
    ) {
        return service.getPlaylist(includeCurrent, limit);
    }
}