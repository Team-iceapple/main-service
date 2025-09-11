package org.example.iceapplehome2.repository;
import java.util.Optional;

public interface SettingsRepository {
    Optional<Double> getPlaybackRate();
    void upsertPlaybackRate(Double playbackRate);
}