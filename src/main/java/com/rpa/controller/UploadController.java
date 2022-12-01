/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.controller;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rpa.messages.RPAMessages;
import com.rpa.model.FileUpload;
import com.rpa.model.UploadModel;
import com.rpa.service.upload.UploadService;
import com.rpa.util.UtilityFile;

@Controller
public class UploadController {

	private static final Logger logger = LoggerFactory.getLogger(UploadController.class.getName());

	@Autowired
	private UploadService uploadService;

	@PostMapping(value = "/uploadSingleFile", consumes = "multipart/form-data")
	public @ResponseBody String uploadSingleFile(@ModelAttribute UploadModel uploadModel) throws URISyntaxException {

		logger.info("BEGIN - uploadSingleFile() - Bank Name::" + uploadModel.getBankName());

		try {
			logger.info("uploadSingleFile() - Folder Desc::" + uploadModel.getFolderDesc());

			List<FileUpload> list = uploadService.findByBankNameAndProcessNameAndFolderDesc(uploadModel.getBankName(), uploadModel.getProcessName(), uploadModel.getFolderDesc());
			byte[] bytes = uploadModel.getFileInput().getBytes();
			String fileFullPath =  UtilityFile.getCodeBasePath() + list.get(0).getFolderPath() + uploadModel.getFileInput().getOriginalFilename();
			StringBuilder sb = new StringBuilder(fileFullPath);
			sb.deleteCharAt(0);
			Path path = Paths.get(sb.toString());
			Files.write(path, bytes);
		} catch (Exception e) {
			logger.error("END - uploadSingleFile() - Exception Occurred::",e);
			return uploadModel.getFileInput().getOriginalFilename() + RPAMessages.FILE_UPLOAD_FAIL_MSG;
		}
		logger.info("END - uploadSingleFile() - Process Name::" + uploadModel.getProcessName());

		return uploadModel.getFileInput().getOriginalFilename() + RPAMessages.FILE_UPLOAD_SUCC_MSG;
	}
}