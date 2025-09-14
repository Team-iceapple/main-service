package org.example.iceapplehome2.web;

import lombok.RequiredArgsConstructor;
import org.example.iceapplehome2.dto.request.AdminVideoEnableRequest;
import org.example.iceapplehome2.dto.request.AdminVideoMetaUpdateRequest;
import org.example.iceapplehome2.dto.request.AdminVideoUpdateRequest;
import org.example.iceapplehome2.dto.response.AdminVideoResponse;
import org.example.iceapplehome2.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/videos")
public class AdminVideoController {

    private final VideoService service;

    @GetMapping
    public List<AdminVideoResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public AdminVideoResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AdminVideoResponse create(@RequestParam("file") MultipartFile file,
                                     @RequestParam("title") String title) {
        return service.uploadAndCreate(file, title);
    }

    @PatchMapping("/{id}")
    public AdminVideoResponse update(@PathVariable String id,
                                     @RequestBody AdminVideoUpdateRequest req) {
        return service.updatePartial(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}