package org.example.iceapplehome2.dto.request;

import java.time.OffsetDateTime;

public record AdminVideoMetaUpdateRequest(
        String title,
        Integer weight
//        Integer durationSec,
//        OffsetDateTime startsAt,
//        OffsetDateTime endsAt
) {}