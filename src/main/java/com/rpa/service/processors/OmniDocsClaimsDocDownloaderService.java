package com.rpa.service.processors;

import java.util.List;

import com.rpa.model.processors.ClaimsDownload;
import com.rpa.response.ClaimsDownloadResponse;

public interface OmniDocsClaimsDocDownloaderService {

	List<ClaimsDownload> getClaimsListFromDb();

	ClaimsDownload save(ClaimsDownload claimsDowload);

	ClaimsDownloadResponse getMarutiDailyStatus();

}
