package org.example.iceapplehome2.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AdminVideoCreateRequest {
    private String url;
    private String title;
}