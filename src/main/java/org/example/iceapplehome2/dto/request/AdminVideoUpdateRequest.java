package org.example.iceapplehome2.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminVideoUpdateRequest {
    private String title;         // 제목 변경
//    private Boolean current;      // 대표 지정/해제
    private Boolean enabled;      // 재생목록 포함/제외
    private Integer weight;       // 정렬 우선순위
    private Double playbackRate;  // 재생 속도

}