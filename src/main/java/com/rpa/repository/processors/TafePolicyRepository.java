package com.rpa.repository.processors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;

import com.rpa.model.processors.TafePolicy;


public interface TafePolicyRepository  extends JpaRepository<TafePolicy, Integer>{

	List<TafePolicy> findAll();

	@Query("SELECT s.policyNo from TafePolicy s where policyDate = to_char(sysdate-1,'mm/dd/yyyy') and s.backLogFlag <> 'Y'")
	List<String> findYesterdayPolicyNo();

	//@Query("SELECT s from TafePolicy s where s.backLogFlag <> 'Y' and (s.isPolicyUploaded in ('N','D') or s.isProposalUploaded in ('N','D')) and (s.isPolicyDownloaded in ('Y') or s.isProposalDownloaded in ('Y')) and rownum<=?1 order by to_date(policyDate,'MM/DD/YYYY')")
	@Query(value="select * from (SELECT * from tafe_Policy s where s.back_Log_Flag <> 'Y' and (s.is_Policy_Uploaded in ('N','D') or s.is_Proposal_Uploaded in ('N','D')) and (s.is_Policy_Downloaded in ('Y') or s.is_Proposal_Downloaded in ('Y')) order by to_date(policy_Date,'MM/DD/YYYY') ) a where rownum<=?1", nativeQuery = true)
	List<TafePolicy> findPdfUnUploadedPolicies(Long rowCount);

	@Query("SELECT s from TafePolicy s where s.policyNo=?1")
	TafePolicy findByPolicyNo(String policyNo);

	@Procedure
	String UPDATE_MAX_ID(String paramValue);

	@Query("SELECT count(*) from TafePolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') ")
	String getTotalPolicies(String policyDate);

	@Query("SELECT count(*) from TafePolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isPolicyUploaded='Y' ")
	String getUploadedPolicies(String policyDate);

	@Query("SELECT count(*) from TafePolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isProposalUploaded='Y' ")
	String getUploadedProposals(String policyDate);
	
	@Query("SELECT distinct s.policyDate from  TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyUploaded='N' and  s.isProposalUploaded='N' order by to_date(policyDate,'MM/DD/YYYY') ")
	List<String> getMarutiPendingPolicyDateList(String policyMonthYear);
	
	@Query("SELECT distinct s.policyDate from  TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.policyDate not in (SELECT distinct s.policyDate from  TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyUploaded='N' and  s.isProposalUploaded='N') order by to_date(policyDate,'MM/DD/YYYY') ")
	List<String> getMarutiUploadedPoliyDateList(String policyMonthYear);

	@Query("SELECT count(*) from TafePolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isPolicyUploaded='E' ")
	String getErrorPolicies(String policyDate);

	@Query("SELECT count(*) from TafePolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isProposalUploaded='E' ")
	String getErrorProposals(String policyDate);

	@Query("SELECT s.policyNo from TafePolicy s where (to_date(s.policyDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and s.backLogFlag='Y' ")
	List<String> findBacklogPolicyNo(String startDate, String endDate);

	//@Query("SELECT s from TafePolicy s where s.backLogFlag='Y' and (to_date(s.policyDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.isPolicyUploaded in ('N','D') or s.isProposalUploaded in ('N','D')) and ( s.isPolicyDownloaded in ('Y') or s.isProposalDownloaded in ('Y') )and rownum<=?3 order by to_date(policyDate,'MM/DD/YYYY')")
	@Query(value="select * from (SELECT * from tafe_Policy s where s.back_Log_Flag='Y' and (to_date(s.policy_Date,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.is_Policy_Uploaded in ('N','D') or s.is_Proposal_Uploaded in ('N','D')) and ( s.is_Policy_Downloaded in ('Y') or s.is_Proposal_Downloaded in ('Y') ) order by to_date(policy_Date,'MM/DD/YYYY') ) a where rownum<=?3", nativeQuery = true)
	List<TafePolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long rowCount);

	@Query("SELECT distinct s.carType from TafePolicy s")
	List<String> getCarTypeList();

	@Query("SELECT s from TafePolicy s where (to_date(s.policyDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and s.carType=?3 ")
	List<TafePolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	//@Query("SELECT s from TafePolicy s where s.backLogFlag <> 'Y' and (s.isPolicyDownloaded in ('N') or s.isProposalDownloaded in ('N')) and rownum<=?1 order by to_date(policyDate,'MM/DD/YYYY')")
	@Query(value="select * from (SELECT * from tafe_Policy s where s.back_Log_Flag <> 'Y' and (s.is_Policy_Downloaded in ('N') or s.is_Proposal_Downloaded in ('N')) order by to_date(policy_Date,'MM/DD/YYYY') )a where rownum<=?1  ", nativeQuery = true)
	List<TafePolicy> findPdfToBeDownloaded(Long rowCount);

	//@Query("SELECT s from TafePolicy s where s.backLogFlag='Y' and (to_date(s.policyDate,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.isPolicyDownloaded in ('N') or s.isProposalDownloaded in ('N')) and rownum<=?3 order by to_date(policyDate,'MM/DD/YYYY')")
	@Query(value="select * from (SELECT * from tafe_Policy s where s.back_Log_Flag='Y' and (to_date(s.policy_Date,'mm/dd/yyyy') between to_date(?1,'mm/dd/yyyy') and to_date(?2,'mm/dd/yyyy')) and (s.is_Policy_Downloaded in ('N') or s.is_Proposal_Downloaded in ('N')) order by to_date(policy_Date,'MM/DD/YYYY') )a where rownum<=?3  ", nativeQuery = true)
	List<TafePolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long rowCount);

	@Query("SELECT count(*) from TafePolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isPolicyDownloaded='Y' ")
	String getExtractedPolicies(String policyDate);

	@Query("SELECT count(*) from TafePolicy s where to_date(policyDate,'mm/dd/yyyy') = to_date(?1,'mm/dd/yyyy') and s.isProposalDownloaded='Y' ")
	String getExtractedProposals(String policyDate);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 ")
	String getTotalMonthlyPolicies(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyDownloaded='Y' ")
	String getMonthlyExtractedPolicies(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isProposalDownloaded='Y' ")
	String geMonthlytExtractedProposals(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyUploaded='Y' ")
	String getMonthlyUploadedPolicies(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isProposalUploaded='Y' ")
	String getMonthlyUploadedProposals(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isPolicyUploaded='E' ")
	String getMonthlyErrorPolicies(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'mm/yyyy') = ?1 and s.isProposalUploaded='E' ")
	String getMonthlyErrorProposals(String monthYear);
	
	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 ")
	String getTotalYearlyPolicies(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isPolicyDownloaded='Y' ")
	String getYearlyExtractedPolicies(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isProposalDownloaded='Y' ")
	String geYearlytExtractedProposals(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isPolicyUploaded='Y' ")
	String getYearlyUploadedPolicies(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isProposalUploaded='Y' ")
	String getYearlyUploadedProposals(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isPolicyUploaded='E' ")
	String getYearlyErrorPolicies(String monthYear);

	@Query("SELECT count(*) from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yyyy'),'yyyy') = ?1 and s.isProposalUploaded='E' ")
	String getYearlyErrorProposals(String monthYear);

	@Query("SELECT s.policyNo from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yy'),'DD/MM/YYYY') = to_char(to_date(?1,'dd/mm/yyyy'),'DD/MM/YYYY') and s.backLogFlag <> 'Y'")
	List<String> findExistingPolicyList(String policyDate);

	@Query("SELECT s from TafePolicy s where policyNo = ?1 ")
	TafePolicy getUploadReference(String policyNo);

	@Query("SELECT s.policyNo from TafePolicy s where to_char(to_date(policyDate,'mm/dd/yy'),'DD/MM/YYYY') = to_char(to_date(?1,'dd/mm/yyyy'),'DD/MM/YYYY') and s.backLogFlag = 'Y'")
	List<String> findExistingBacklogPolicyList(String policyDate);
	
}
