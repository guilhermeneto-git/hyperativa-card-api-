package com.hyperativa.card.service;

import com.hyperativa.card.dto.UploadResultDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    UploadResultDto processCardFile(MultipartFile file);
}

