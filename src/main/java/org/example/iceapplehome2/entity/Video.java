package org.example.iceapplehome2.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Video {
    private String id;            // v_<uuid>
    private String filePath;      // 파일명(상대경로) 예: v_abcd.mp4
    private String title;
    private boolean current;
    private OffsetDateTime createdAt;

    private boolean enabled;        // 재생목록 포함 여부 (DEFAULT TRUE)
    private Integer weight;         // 정렬 우선순위(클수록 먼저, DEFAULT 0)
    private Integer durationSec;    // 개별 체류시간(초, NULL이면 프론트 기본값 사용)
    private OffsetDateTime startsAt; // 노출 시작 (NULL=항상)
    private OffsetDateTime endsAt;   // 노출 종료 (NULL=항상)
}