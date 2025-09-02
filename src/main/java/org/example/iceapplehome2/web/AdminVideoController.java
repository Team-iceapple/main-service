package org.example.iceapplehome2.web;

import lombok.RequiredArgsConstructor;
import org.example.iceapplehome2.dto.response.AdminVideoResponse;
import org.example.iceapplehome2.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos")
public class AdminVideoController {

    private final VideoService service;

    // 업로드 + 등록(현재영상으로 설정)
    @PostMapping(value = "/upload", consumes = MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AdminVideoResponse upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam("title") String title) {
        return service.uploadAndCreate(file, title);
    }

    @GetMapping("/current")
    public AdminVideoResponse current() { return service.getCurrent(); }

    @GetMapping
    public List<AdminVideoResponse> list() { return service.list(); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) { service.delete(id); }
}