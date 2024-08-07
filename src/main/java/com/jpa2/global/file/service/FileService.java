package com.jpa2.global.file.service;

import javax.annotation.processing.FilerException;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

	// 저장된 파일 경로 반환
	String save(MultipartFile multipartFile) throws FilerException;
	
	void delete(String filePath);
}