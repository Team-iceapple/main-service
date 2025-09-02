package org.example.iceapplehome2.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AdminVideoCreateRequest {
    private String url;    // 원본 링크
    private String title;  // 선택
}