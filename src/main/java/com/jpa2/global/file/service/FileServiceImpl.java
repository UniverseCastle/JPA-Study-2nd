package com.jpa2.global.file.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jpa2.global.file.exception.FileException;
import com.jpa2.global.file.exception.FileExceptionType;

@Service
public class FileServiceImpl implements FileService {

	@Value("${file.dir}") // application.yml 파일에 있는 file.dir의 내용을 가져옴
	private String fileDir; // 파일 저장 경로

	
	@Override
	public String save(MultipartFile multipartFile) {
		String filePath = fileDir + UUID.randomUUID(); // 파일 이름 겹치지 않게 저장
		try {
			multipartFile.transferTo(new File(filePath)); // 지정된 경로에 저장
		} catch (IOException e) {
			// 파일 저장 에러
			throw new FileException(FileExceptionType.FILE_CAN_NOT_SAVE);
		}
		
		return filePath; // 경로 반환
	}

	@Override
	public void delete(String filePath) {
		File file = new File(filePath);
		
		if (!file.exists()) return;
		
		if (!file.delete()) throw new FileException(FileExceptionType.FILE_CAN_NOT_DELETE);
	}
	
	
}