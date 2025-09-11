package org.example.iceapplehome2.service;

import lombok.RequiredArgsConstructor;
import org.example.iceapplehome2.dto.request.VideoPlaybackSettingsUpdateRequest;
import org.example.iceapplehome2.dto.response.VideoPlaybackSettingsResponse;
import org.example.iceapplehome2.repository.SettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository repo;

    @Transactional(readOnly = true)
    public VideoPlaybackSettingsResponse getPlayback() {
        double rate = repo.getPlaybackRate().orElse(1.0);
        return new VideoPlaybackSettingsResponse(rate);
    }

    @Transactional
    public VideoPlaybackSettingsResponse updatePlayback(VideoPlaybackSettingsUpdateRequest req) {
        if (req.playbackRate() == null)
            throw new IllegalArgumentException("playbackRate is required");
        double r = req.playbackRate();
        if (r <= 0.1 || r > 4.0)
            throw new IllegalArgumentException("playbackRate must be in (0.1, 4.0]");

        repo.upsertPlaybackRate(r);
        return getPlayback();
    }
}