package org.example.iceapplehome2.entity;

//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonInclude.Include;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.OffsetDateTime;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@JsonInclude(Include.NON_NULL)
//public class Video {
//    private String id;
//    private String url;
//    private String title;
//    private boolean current;
//    private OffsetDateTime createdAt;

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

}