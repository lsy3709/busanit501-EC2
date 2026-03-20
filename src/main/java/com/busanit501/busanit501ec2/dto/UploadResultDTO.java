

package com.busanit501.busanit501ec2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResultDTO {
    private String uuid;
    private String fileName;

    // 화면에서 /view/{link} 형태로 이미지를 요청할 때 사용할 최종 파일명 (uuid_파일명)
    private String link;
}