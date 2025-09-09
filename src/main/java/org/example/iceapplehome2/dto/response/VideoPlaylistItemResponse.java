package org.example.iceapplehome2.dto.response;

public record VideoPlaylistItemResponse(
        String id,
        String title,
        String fileUrl,
        Integer durationSec,
        Integer weight
) {}