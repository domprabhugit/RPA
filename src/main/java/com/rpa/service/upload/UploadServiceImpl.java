/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.upload;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.FileUpload;
import com.rpa.repository.upload.UploadRepository;

@Service
public class UploadServiceImpl implements UploadService {
    @Autowired
    private UploadRepository uploadRepository;

    @Override
    public List<FileUpload> getUniqueRecords() {
        return uploadRepository.getUniqueRecords();
    }

	@Override
	public List<FileUpload> findByBankName(String bankName) {
		return uploadRepository.findByBankName(bankName);
	}
	
	@Override
	public List<FileUpload> findByBankNameAndProcessName(String bankName,String processName) {
		return uploadRepository.findByBankNameAndProcessName(bankName,processName);
	}

	@Override
	public List<FileUpload> findByBankNameAndProcessNameAndFolderDesc(String bankName, String processName, String folderDesc) {
		return uploadRepository.findByBankNameAndProcessNameAndFolderDesc(bankName,processName,folderDesc);
	}
}
