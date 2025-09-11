package org.example.iceapplehome2.dto.request;

import java.time.OffsetDateTime;

public record AdminVideoMetaUpdateRequest(
        String title,
        Integer weight,
        Double playbackRate        // null 이면 병경 X
//        Integer durationSec,
//        OffsetDateTime startsAt,
//        OffsetDateTime endsAt
) {}