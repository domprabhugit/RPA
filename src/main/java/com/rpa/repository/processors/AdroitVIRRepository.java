package com.rpa.repository.processors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;

import com.rpa.model.processors.AdroitVIR;


public interface AdroitVIRRepository  extends JpaRepository<AdroitVIR, Integer>{

	List<AdroitVIR> findAll();

	@Query("SELECT s.policyNo from AdroitVIR s where inspectionDate = to_char(sysdate-1,'mm/dd/yyyy') and s.backLogFlag <> 'Y'")
	List<String> findYesterdayPolicyNo();

	//@Query("SELECT s from AdroitVIR s where s.backLogFlag <> 'Y' and (s.isReportUploaded in ('N','D') or s.isProposalUploaded in ('N','D')) and (s.isReportDownloaded in ('Y') or s.isProposalDownloaded in ('Y')) and rownum<=?1 order by to_date(inspectionDate,'MM/DD/YYYY')")
	@Query(value="select * from (SELECT * from ADROIT_VIR s where s.back_Log_Flag <> 'Y' and (s.is_Report_Uploaded in ('N','D') ) and (s.is_Report_Downloaded in ('Y') ) order by to_date(inspection_date,'MM/DD/YYYY') ) a where rownum<=?1", nativeQuery = true)
	List<AdroitVIR> findPdfUnUploadedPolicies(Long rowCount);

	@Query("SELECT s from AdroitVIR s where s.policyNo=?1")
	AdroitVIR findByPolicyNo(String policyNo);

	@Procedure
	String UPDATE_MAX_ID(String paramValue);

	@Query("SELECT count(*) from AdroitVIR s where to_date(inspectionDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') ")
	String getTotalPolicies(String inspectionDate);

	@Query("SELECT count(*) from AdroitVIR s where to_date(inspectionDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isReportUploaded='Y' ")
	String getUploadedPolicies(String inspectionDate);

	/*@Query("SELECT count(*) from AdroitVIR s where to_date(inspectionDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isProposalUploaded='Y' ")
	String getUploadedProposals(String inspectionDate);*/
	
	@Query("SELECT distinct s.inspectionDate from  AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isReportUploaded='N' order by to_date(inspectionDate,'MM/DD/YYYY') ")
	List<String> getMarutiPendingPolicyDateList(String policyMonthYear);
	
	@Query("SELECT distinct s.inspectionDate from  AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.inspectionDate not in (SELECT distinct s.inspectionDate from  AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isReportUploaded='N') order by to_date(inspectionDate,'MM/DD/YYYY') ")
	List<String> getMarutiUploadedPoliyDateList(String policyMonthYear);

	@Query("SELECT count(*) from AdroitVIR s where to_date(inspectionDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isReportUploaded='E' ")
	String getErrorPolicies(String inspectionDate);

	/*@Query("SELECT count(*) from AdroitVIR s where to_date(inspectionDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isProposalUploaded='E' ")
	String getErrorProposals(String inspectionDate);*/

	@Query("SELECT s.policyNo from AdroitVIR s where (to_date(s.inspectionDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and s.backLogFlag='Y' ")
	List<String> findBacklogPolicyNo(String startDate, String endDate);

	//@Query("SELECT s from AdroitVIR s where s.backLogFlag='Y' and (to_date(s.inspectionDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.isReportUploaded in ('N','D') or s.isProposalUploaded in ('N','D')) and ( s.isReportDownloaded in ('Y') or s.isProposalDownloaded in ('Y') )and rownum<=?3 order by to_date(inspectionDate,'MM/DD/YYYY')")
	@Query(value="select * from (SELECT * from ADROIT_VIR s where s.back_Log_Flag='Y' and (to_date(s.inspection_date,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.is_Report_Uploaded in ('N','D') ) and ( s.is_Report_Downloaded in ('Y') ) order by to_date(inspection_date,'MM/DD/YYYY') ) a where rownum<=?3", nativeQuery = true)
	List<AdroitVIR> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long rowCount);

	@Query("SELECT distinct s.virType from AdroitVIR s")
	List<String> getCarTypeList();

	@Query("SELECT s from AdroitVIR s where (to_date(s.inspectionDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and s.virType=?3 ")
	List<AdroitVIR> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	//@Query("SELECT s from AdroitVIR s where s.backLogFlag <> 'Y' and (s.isReportDownloaded in ('N') or s.isProposalDownloaded in ('N')) and rownum<=?1 order by to_date(inspectionDate,'MM/DD/YYYY')")
	@Query(value="select * from (SELECT * from ADROIT_VIR s where s.back_Log_Flag <> 'Y' and (s.is_Report_Downloaded in ('N')) order by to_date(inspection_date,'MM/DD/YYYY') )a where rownum<=?1  ", nativeQuery = true)
	List<AdroitVIR> findPdfToBeDownloaded(Long rowCount);

	//@Query("SELECT s from AdroitVIR s where s.backLogFlag='Y' and (to_date(s.inspectionDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.isReportDownloaded in ('N') or s.isProposalDownloaded in ('N')) and rownum<=?3 order by to_date(inspectionDate,'MM/DD/YYYY')")
	@Query(value="select * from (SELECT * from ADROIT_VIR s where s.back_Log_Flag='Y' and (to_date(s.inspection_date,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.is_Report_Downloaded in ('N')) order by to_date(inspection_date,'MM/DD/YYYY') )a where rownum<=?3  ", nativeQuery = true)
	List<AdroitVIR> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long rowCount);

	@Query("SELECT count(*) from AdroitVIR s where to_date(inspectionDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isReportDownloaded='Y' ")
	String getExtractedPolicies(String inspectionDate);

	/*@Query("SELECT count(*) from AdroitVIR s where to_date(inspectionDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isProposalDownloaded='Y' ")
	String getExtractedProposals(String inspectionDate);*/

	@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 ")
	String getTotalMonthlyPolicies(String monthYear);

	@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isReportDownloaded='Y' ")
	String getMonthlyExtractedPolicies(String monthYear);

	/*@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isProposalDownloaded='Y' ")
	String geMonthlytExtractedProposals(String monthYear);*/

	@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isReportUploaded='Y' ")
	String getMonthlyUploadedPolicies(String monthYear);

	/*@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isProposalUploaded='Y' ")
	String getMonthlyUploadedProposals(String monthYear);*/

	@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isReportUploaded='E' ")
	String getMonthlyErrorPolicies(String monthYear);

	/*@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isProposalUploaded='E' ")
	String getMonthlyErrorProposals(String monthYear);*/
	
	@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'yyyy') = ?1 ")
	String getTotalYearlyPolicies(String monthYear);

	@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isReportDownloaded='Y' ")
	String getYearlyExtractedPolicies(String monthYear);

	/*@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isProposalDownloaded='Y' ")
	String geYearlytExtractedProposals(String monthYear);*/

	@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isReportUploaded='Y' ")
	String getYearlyUploadedPolicies(String monthYear);

	/*@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isProposalUploaded='Y' ")
	String getYearlyUploadedProposals(String monthYear);*/

	@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isReportUploaded='E' ")
	String getYearlyErrorPolicies(String monthYear);

	/*@Query("SELECT count(*) from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isProposalUploaded='E' ")
	String getYearlyErrorProposals(String monthYear);*/

	@Query("SELECT s.policyNo from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yy'),'DD/MM/YYYY') = to_char(to_date(?1,'dd/mm/yyyy'),'DD/MM/YYYY') and s.backLogFlag <> 'Y'")
	List<String> findExistingPolicyList(String inspectionDate);

	@Query("SELECT s from AdroitVIR s where policyNo = ?1 ")
	AdroitVIR getUploadReference(String policyNo);

	@Query("SELECT s.policyNo from AdroitVIR s where to_char(to_date(inspectionDate,'mm/dd/yy'),'DD/MM/YYYY') = to_char(to_date(?1,'dd/mm/yyyy'),'DD/MM/YYYY') and s.backLogFlag = 'Y'")
	List<String> findExistingBacklogPolicyList(String inspectionDate);
	
}
