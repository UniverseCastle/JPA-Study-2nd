package com.jpa2.global.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
class FileServiceTest {
	
	@Autowired
	FileService fileService;
	
	private MockMultipartFile getMockUploadFile() throws Exception {
		return new MockMultipartFile("file", "file.jfif", "image/jfif", new FileInputStream("C:/Users/uc/Desktop/uc/Develop/images/thumb/bh.jfif")); // 파일이름, 오리지널 파일이름, 파일타입, 파일경로
	}
	
	//== Test ==//
	
//	파일저장
//	@Test
	public void 파일저장_성공() throws Exception {
		// given, when
		String filePath = fileService.save(getMockUploadFile());
		
		// then
		File file = new File(filePath);
		
		// finally
		file.delete(); // 파일 삭제
	}
	
//	파일삭제
//	@Test
	public void 파일삭제_성공() throws Exception {
		// given, when
		String filePath = fileService.save(getMockUploadFile());
		fileService.delete(filePath);
		
		// then
		File file = new File(filePath);
		
		assertThat(file.exists()).isFalse();
	}
}