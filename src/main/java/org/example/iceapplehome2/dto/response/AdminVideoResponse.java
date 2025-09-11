package org.example.iceapplehome2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminVideoResponse {
    private String id;
    private String title;
    private boolean current;
    private String filePath;
    private String fileUrl;

    private boolean enabled;
    private int weight;

    private Double playbackRate;

}