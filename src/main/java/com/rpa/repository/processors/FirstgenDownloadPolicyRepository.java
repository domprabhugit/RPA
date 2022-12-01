package com.rpa.repository.processors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;

import com.rpa.model.processors.FirstgenPolicy;


public interface FirstgenDownloadPolicyRepository  extends JpaRepository<FirstgenPolicy, Integer>{

	List<FirstgenPolicy> findAll();

	@Query("SELECT s.policyNo from FirstgenPolicy s where policyDate = to_char(sysdate-1,'mm/dd/yyyy') and s.backLogFlag <> 'Y'")
	List<String> findYesterdayPolicyNo();

	@Query("SELECT s from FirstgenPolicy s where s.backLogFlag <> 'Y' and (s.isPolicyUploaded in ('N','D')) and (s.isPolicyDownloaded in ('Y') or s.isInvoiceDownloaded in ('Y')) and rownum<=?1 order by to_date(policyDate,'MM/DD/YYYY')")
	List<FirstgenPolicy> findPdfUnUploadedPolicies(Long rowCount);

	@Query("SELECT s from FirstgenPolicy s where s.policyNo=?1")
	FirstgenPolicy findByPolicyNo(String policyNo);

	@Procedure
	String UPDATE_MAX_ID(String paramValue);

	@Query("SELECT count(*) from FirstgenPolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') ")
	String getTotalPolicies(String policyDate);

	@Query("SELECT count(*) from FirstgenPolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isPolicyUploaded='Y' ")
	String getUploadedPolicies(String policyDate);

	@Query("SELECT count(*) from FirstgenPolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isInvoiceUploaded='Y' ")
	String getUploadedProposals(String policyDate);
	
	@Query("SELECT distinct s.policyDate from  FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyUploaded='N' and  s.isInvoiceUploaded='N' order by to_date(policyDate,'MM/DD/YYYY') ")
	List<String> getMarutiPendingPolicyDateList(String policyMonthYear);
	
	@Query("SELECT distinct s.policyDate from  FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.policyDate not in (SELECT distinct s.policyDate from  FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyUploaded='N' and  s.isInvoiceUploaded='N') order by to_date(policyDate,'MM/DD/YYYY') ")
	List<String> getMarutiUploadedPoliyDateList(String policyMonthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isPolicyUploaded='E' ")
	String getErrorPolicies(String policyDate);

	@Query("SELECT count(*) from FirstgenPolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isInvoiceUploaded='E' ")
	String getErrorProposals(String policyDate);

	@Query("SELECT s.policyNo from FirstgenPolicy s where (to_date(s.policyDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and s.backLogFlag='Y' ")
	List<String> findBacklogPolicyNo(String startDate, String endDate);

	@Query("SELECT s from FirstgenPolicy s where s.backLogFlag='Y' and (to_date(s.policyDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.isPolicyUploaded in ('N','D') or s.isInvoiceUploaded in ('N','D')) and ( s.isPolicyDownloaded in ('Y') or s.isInvoiceDownloaded in ('Y') )and rownum<=?3 order by to_date(policyDate,'MM/DD/YYYY')")
	List<FirstgenPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long rowCount);

	@Query("SELECT distinct s.carType from FirstgenPolicy s")
	List<String> getCarTypeList();

	@Query("SELECT s from FirstgenPolicy s where (to_date(s.policyDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and s.carType=?3 ")
	List<FirstgenPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	@Query("SELECT s from FirstgenPolicy s where s.backLogFlag <> 'Y' and (s.isPolicyDownloaded in ('N') or s.isInvoiceDownloaded in ('N')) and rownum<=?1 order by to_date(policyDate,'MM/DD/YYYY')")
	List<FirstgenPolicy> findPdfToBeDownloaded(Long rowCount);

	@Query("SELECT s from FirstgenPolicy s where s.backLogFlag='Y' and (to_date(s.policyDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.isPolicyDownloaded in ('N') or s.isInvoiceDownloaded in ('N')) and rownum<=?3 order by to_date(policyDate,'MM/DD/YYYY')")
	List<FirstgenPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long rowCount);

	@Query("SELECT count(*) from FirstgenPolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isPolicyDownloaded='Y' ")
	String getExtractedPolicies(String policyDate);

	@Query("SELECT count(*) from FirstgenPolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isInvoiceDownloaded='Y' ")
	String getExtractedProposals(String policyDate);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 ")
	String getTotalMonthlyPolicies(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyDownloaded='Y' ")
	String getMonthlyExtractedPolicies(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isInvoiceDownloaded='Y' ")
	String geMonthlytExtractedProposals(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyUploaded='Y' ")
	String getMonthlyUploadedPolicies(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isInvoiceUploaded='Y' ")
	String getMonthlyUploadedProposals(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyUploaded='E' ")
	String getMonthlyErrorPolicies(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isInvoiceUploaded='E' ")
	String getMonthlyErrorProposals(String monthYear);
	
	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 ")
	String getTotalYearlyPolicies(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isPolicyDownloaded='Y' ")
	String getYearlyExtractedPolicies(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isInvoiceDownloaded='Y' ")
	String geYearlytExtractedProposals(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isPolicyUploaded='Y' ")
	String getYearlyUploadedPolicies(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isInvoiceUploaded='Y' ")
	String getYearlyUploadedProposals(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isPolicyUploaded='E' ")
	String getYearlyErrorPolicies(String monthYear);

	@Query("SELECT count(*) from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isInvoiceUploaded='E' ")
	String getYearlyErrorProposals(String monthYear);

	@Query("SELECT s.policyNo from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yy'),'DD/MM/YYYY') = to_char(to_date(?1,'dd/mm/yyyy'),'DD/MM/YYYY') and s.backLogFlag <> 'Y'")
	List<String> findExistingPolicyList(String policyDate);

	@Query("SELECT s from FirstgenPolicy s where policyNo = ?1 ")
	FirstgenPolicy getUploadReference(String policyNo);

	@Query("SELECT s.policyNo from FirstgenPolicy s where to_char(to_date(policyDate,'mm/dd/yy'),'DD/MM/YYYY') = to_char(to_date(?1,'dd/mm/yyyy'),'DD/MM/YYYY') and s.backLogFlag = 'Y'")
	List<String> findExistingBacklogPolicyList(String policyDate);

	@Query("SELECT s from FirstgenPolicy s where s.backLogFlag <> 'Y' and s.isPolicyUploaded in ('E') and s.isPdfMerged in ('Y') and rownum<=?1 order by to_date(policyDate,'MM/DD/YYYY')")
	List<FirstgenPolicy> findErrorPdfUploadedPolicies(Long thresholdFrequencyLevel);
	
}
