package com.rpa.repository.processors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.ClaimsDownload;

public interface OmniDocsClaimsDocDownloaderRepository extends JpaRepository<ClaimsDownload, Integer> {

	@Query("select s from ClaimsDownload s where s.isProcessed='N'")
	List<ClaimsDownload> getClaimsListFromDb();

	@Query("select count(*) from ClaimsDownload s")
	String getTotalCalimsCount();

	@Query("select count(*) from ClaimsDownload s where s.isProcessed='Y'")
	String getTotalClaimsProcessedCount();

	@Query("select count(*) from ClaimsDownload s where s.isProcessed='Y' and isFileAvailable='Y'")
	String getClaimsWithFilesCount();

	@Query("select count(*) from ClaimsDownload s where s.isProcessed='Y' and isFileAvailable='N'")
	String getClaimsWithoutFilesCount();

}
