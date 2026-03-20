package com.busanit501.busanit501ec2.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

@Component // 스프링이 이 클래스를 관리하는 부품(Bean)으로 등록하게 합니다.
@RequiredArgsConstructor // final이 붙은 변수의 생성자를 자동으로 만들어줍니다.
@Log4j2 // 시스템에 로그(기록)를 남기기 위해 사용하는 어노테이션입니다.
public class S3Uploader {

    // S3와 통신을 담당하는 AWS에서 제공하는 객체입니다.
    private final AmazonS3Client amazonS3Client;

    // application.properties 파일에 적어둔 내 S3 버킷 이름을 가져와서 변수에 쏙 넣습니다.
    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    // 1. 외부에서 파일을 업로드할 때 부르는 메인 기능 (파일 경로를 넘겨받음)
    public String upload(String filePath) throws RuntimeException {
        // 내 컴퓨터(로컬)의 경로를 바탕으로 실제 파일 객체를 준비합니다.
        File targetFile = new File(filePath);

        // 아래에 있는 putS3 메서드를 불러서 실제 클라우드(S3)로 파일을 보냅니다.
        String uploadImageUrl = putS3(targetFile, targetFile.getName());

        // 클라우드에 업로드가 끝났으니, 내 컴퓨터에 임시로 만들어진 원본 파일은 지웁니다.
        removeOriginalFile(targetFile);

        // 최종적으로 S3에 올라간 이미지의 URL(주소)을 반환합니다.
        return uploadImageUrl;
    }

    // 2. 실제 S3 창고로 파일을 전송하는 핵심 기능 🚨(수정 및 개선된 부분)🚨
    private String putS3(File uploadFile, String fileName) throws RuntimeException {
        // S3 버킷에 파일을 업로드하는 요청을 보냅니다.
        // (💡주의: 우리 버킷은 '퍼블릭 액세스 차단'으로 꽁꽁 잠겨있기 때문에, 에러가 나지 않도록 전체 공개 권한을 주는 코드를 삭제했습니다.)
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile));

        // 업로드가 완료된 파일의 S3 URL(주소)을 가져와서 글자(String)로 반환합니다.
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // 3. 내 컴퓨터(로컬)에 임시로 저장된 파일을 찌꺼기가 남지 않게 청소하는 기능
    private void removeOriginalFile(File targetFile) {
        // 파일이 실제로 존재하고, 삭제에 성공했다면
        if (targetFile.exists() && targetFile.delete()) {
            log.info("File delete success"); // 콘솔창에 "삭제 성공" 이라고 기록을 남깁니다.
            return;
        }
        // 삭제에 실패했다면 "삭제 실패" 기록을 남깁니다.
        log.info("fail to remove");
    }

    // 4. S3 클라우드에 올라가 있는 파일을 삭제하는 기능 (나중에 게시글을 지울 때 사용)
    public void removeS3File(String fileName){
        // '이 버킷에 있는, 이 이름의 파일을 지워주세요' 라는 요청서를 만듭니다.
        final DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, fileName);

        // AWS S3 클라이언트에게 지워달라고 요청서를 제출합니다.
        amazonS3Client.deleteObject(deleteObjectRequest);
    }

    // 5. 프라이빗한 S3 파일을 브라우저에서 볼 수 있도록 '임시 열람 링크'를 생성하는 기능 (Presigned URL)
    public String getPresignedUrl(String fileName) {
        // 링크의 유효 시간을 설정합니다. (현재 시간 + 5분)
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 5; // 5분(300초) 설정
        expiration.setTime(expTimeMillis);

        // '이 파일에 대해, GET(읽기) 권한을 가진, 5분짜리 링크를 만들어줘' 라는 요청서 작성
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileName)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        // 암호화된 임시 링크 생성 및 반환
        return amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    // 6. S3에 있는 파일을 내 컴퓨터(로컬)로 직접 다운로드하는 기능
    public void downloadS3File(String s3FileName, String localFilePath) throws Exception {
        // S3에서 파일을 달라고 요청
        S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(bucket, s3FileName));
        S3ObjectInputStream inputStream = s3Object.getObjectContent();

        // 가져온 파일을 내 컴퓨터의 지정된 경로에 저장 (이미 파일이 있으면 덮어쓰기)
        Files.copy(inputStream, Paths.get(localFilePath), StandardCopyOption.REPLACE_EXISTING);

        // 메모리 절약을 위해 스트림 닫기
        inputStream.close();
    }

}