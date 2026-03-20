package com.busanit501.busanit501ec2.config; // 본인의 패키지 경로에 맞게 수정하세요!

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // "스프링아, 이건 환경설정 파일이야. 실행할 때 꼭 읽어봐!" 라고 알려줍니다.
public class S3Config {

    // application.properties에 적어둔 키 값들을 가져옵니다.
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean // "스프링아, 네가 못 만든 AmazonS3Client 객체를 내가 직접 만들어서 줄게. 이걸 가져다 써!"
    public AmazonS3Client amazonS3Client() {
        // 1. 내 엑세스 키와 시크릿 키로 자격 증명서(신분증)를 만듭니다.
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        // 2. 신분증과 지역(서울) 정보를 넣어서 AmazonS3Client 객체를 조립해 반환합니다.
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }
}