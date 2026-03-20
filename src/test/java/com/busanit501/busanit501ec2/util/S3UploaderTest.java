package com.busanit501.busanit501ec2.util;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest // 스프링 부트 환경을 띄워서 실제처럼 테스트하겠다는 의미입니다.
@Log4j2 // 결과를 콘솔창에 예쁘게 출력하기 위해 사용합니다.
public class S3UploaderTest {

    @Autowired
    private S3Uploader s3Uploader; // 우리가 만든 S3 업로더 부품을 가져옵니다.

    @Test
    public void testUpload() {

        try {
            // 🚨 주의: 이 경로에 실제로 파일이 있어야 테스트가 성공합니다!
            String filePath = "C:\\upload\\test1.jpg";

            // S3에 업로드를 지시하고, 완료되면 저장된 주소(URL)를 받아옵니다.
            String uploadName = s3Uploader.upload(filePath);

            // 콘솔창에 결과 주소를 출력합니다.
            log.info(uploadName);

        } catch(Exception e) {
            // 에러가 나면 어떤 에러인지 콘솔창에 붉은색으로 띄워줍니다.
            log.error(e.getMessage());
        }
    }

    // [테스트 1] 브라우저에서 볼 수 있는 5분짜리 임시 링크 생성 테스트
    @Test
    public void testGetPresignedUrl() {
        // 🚨 S3에 실제로 올라가 있는 파일 이름을 적어주세요.
        String fileName = "test1.jpg";

        // 임시 열람 링크 발급
        String presignedUrl = s3Uploader.getPresignedUrl(fileName);

        log.info("==================================================");
        log.info("🔐 S3 프라이빗 이미지 임시 열람 링크 (5분간 유효)");
        log.info("📸 아래 링크를 [Ctrl + 클릭] 해서 브라우저에서 확인해 보세요!");
        log.info("👉 " + presignedUrl);
        log.info("==================================================");
    }

    // [테스트 2] S3의 파일을 내 컴퓨터 특정 폴더로 물리적 다운로드 테스트
    @Test
    public void testDownloadFile() {
        // 🚨 S3에 있는 원본 파일명
        String s3FileName = "test1.jpg";

        // 🚨 다운로드 받아서 저장할 내 컴퓨터 경로 (경로의 폴더가 미리 존재해야 합니다)
        String localFilePath = "C:\\upload\\downloaded_from_s3.jpg";

        try {
            s3Uploader.downloadS3File(s3FileName, localFilePath);

            // 파일이 실제로 다운로드 되었는지 자바 코드로 검증(확인)
            File downloadedFile = new File(localFilePath);
            Assertions.assertTrue(downloadedFile.exists(), "파일 다운로드 실패!");

            log.info("==================================================");
            log.info("✅ S3 파일 다운로드 성공!");
            log.info("저장된 위치: " + localFilePath);
            log.info("직접 폴더로 가서 이미지가 잘 받아졌는지 확인해 보세요.");
            log.info("==================================================");

        } catch (Exception e) {
            log.error("다운로드 중 에러 발생: " + e.getMessage());
        }
    }

    @Test
    public void testRemove() {
        try {
            // 🚨 주의: 삭제할 파일의 'S3에 저장된 이름'을 정확히 적어주세요!
            // 로컬 경로(C:\\...)가 아니라, 실제 S3 버킷에 올라가 있는 파일명입니다.
            String fileName = "test1.jpg";

            // S3Uploader의 삭제 기능 실행
            s3Uploader.removeS3File(fileName);

            // 에러 없이 여기까지 오면 삭제 성공!
            log.info("S3 버킷에서 파일 삭제 완료: " + fileName);

        } catch (Exception e) {
            // 권한이 없거나 파일이 없어서 에러가 나면 붉은 글씨로 출력됩니다.
            log.error("S3 파일 삭제 실패: " + e.getMessage());
        }
    }

}