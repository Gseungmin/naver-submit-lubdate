package com.example.naver.web.controller;

import com.example.naver.domain.dto.image.req.ImageRequestDto;
import com.example.naver.domain.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public String handleFileUpload(@RequestBody ImageRequestDto dto) {
        URL presignedUrl = s3Service.generatePresignedUrl(dto.getFilename(), dto.getType());
        return presignedUrl.toString();
    }
}
