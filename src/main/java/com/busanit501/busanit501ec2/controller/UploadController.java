

package com.busanit501.busanit501ec2.controller;

import com.busanit501.busanit501ec2.dto.UploadResultDTO;
import com.busanit501.busanit501ec2.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Log4j2
public class UploadController {

    private final S3Uploader s3Uploader;

    // application.properties에 설정한 임시 저장 폴더 (C:\\upload)
    @Value("${com.busanit501.upload.path}")
    private String uploadPath;


    // 1. 업로드 테스트 화면 띄우기
    @GetMapping("/upload")
    public String uploadGET() {
        return "upload"; // templates/upload.html 을 찾아갑니다.
    }

    // 2. 파일 업로드 처리 (AJAX)
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseBody
    public List<UploadResultDTO> uploadPOST(@RequestParam("files") MultipartFile[] files) {
        List<UploadResultDTO> list = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String saveName = uuid + "_" + originalName; // 중복 방지를 위해 UUID 붙임

            // 내 컴퓨터(로컬) 임시 폴더에 먼저 저장
            Path savePath = Paths.get(uploadPath, saveName);
            try {
                file.transferTo(savePath);

                // 로컬에 저장된 파일을 S3로 업로드 (업로드 후 로컬 파일은 S3Uploader가 지워줌)
                s3Uploader.upload(savePath.toString());

                // 화면에 돌려줄 정보 담기
                list.add(UploadResultDTO.builder()
                        .uuid(uuid)
                        .fileName(originalName)
                        .link(saveName) // S3에 올라간 실제 파일명
                        .build());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return list; // JSON 형태로 화면에 응답
    }

    // 🌟 3. [핵심] 5분 임시 링크로 리다이렉트(방향 틀기) 해주는 메서드
    @GetMapping("/view/{fileName}")
    public String viewFileGET(@PathVariable String fileName) {
        // S3Uploader에서 5분짜리 안전한 임시 출입증(URL)을 받아옵니다.
        String presignedUrl = s3Uploader.getPresignedUrl(fileName);

        // 브라우저에게 "저기 S3 주소로 가서 이미지 받아와!" 라고 지시합니다.
        return "redirect:" + presignedUrl;
    }

    // 4. 파일 삭제 처리 (AJAX)
    @DeleteMapping("/remove/{fileName}")
    @ResponseBody
    public ResponseEntity<String> removeFile(@PathVariable String fileName) {
        s3Uploader.removeS3File(fileName); // S3에서 파일 완전 삭제
        return ResponseEntity.ok("success");
    }
}