/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.processors.ClaimsDownload;
import com.rpa.repository.processors.OmniDocsClaimsDocDownloaderRepository;
import com.rpa.response.ClaimsDownloadResponse;

@Service
public class OmniDocsClaimsDocDownloaderServiceImpl implements OmniDocsClaimsDocDownloaderService {
    
  @Autowired
   private OmniDocsClaimsDocDownloaderRepository omniDocsClaimsDocDownloaderRepository;

@Override
public List<ClaimsDownload> getClaimsListFromDb() {
	return omniDocsClaimsDocDownloaderRepository.getClaimsListFromDb();
}

@Override
public ClaimsDownload save(ClaimsDownload claimsDowload) {
	return omniDocsClaimsDocDownloaderRepository.save(claimsDowload);
}

@Override
public ClaimsDownloadResponse getMarutiDailyStatus() {
	ClaimsDownloadResponse ClaimsDownloadResponse = new ClaimsDownloadResponse();
	ClaimsDownloadResponse.setTotalClaims(omniDocsClaimsDocDownloaderRepository.getTotalCalimsCount());
	ClaimsDownloadResponse.setProcessedClaims(omniDocsClaimsDocDownloaderRepository.getTotalClaimsProcessedCount());
	ClaimsDownloadResponse.setClaimsWithFiles(omniDocsClaimsDocDownloaderRepository.getClaimsWithFilesCount());
	ClaimsDownloadResponse.setClaimsWithoutFiles(omniDocsClaimsDocDownloaderRepository.getClaimsWithoutFilesCount());
	return ClaimsDownloadResponse;
}

	
}
