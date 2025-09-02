package org.example.iceapplehome2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class AdminVideoResponse {
    private String id;
    private String title;
    private boolean current;
    private String filePath;    // 저장된 파일 경로(상대)
    private String fileUrl;     // 서빙용 URL (예: /media/파일명)
}