package com.ihis.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ihis.bindings.EligibilityDtls;
import com.ihis.entities.CitizenApplicationEntity;
import com.ihis.entities.CitizenChildDtlsEntity;
import com.ihis.entities.CitizenGraduationDtlsEntity;
import com.ihis.entities.CitizenIncomeDtlsEntity;
import com.ihis.entities.CitizenPlansEntity;
import com.ihis.entities.CoTriggersEntity;
import com.ihis.entities.EligibilityDetailsEntity;
import com.ihis.repo.AppPlanRepository;
import com.ihis.repo.CitizenApplicationRepository;
import com.ihis.repo.CitizenChildDtlsRepository;
import com.ihis.repo.CitizenGraduationDtlsRepository;
import com.ihis.repo.CitizenIncomeDtlsRepository;
import com.ihis.repo.CitizenPlansRepository;
import com.ihis.repo.CoTriggersRepository;
import com.ihis.repo.EligibilityDetailsRepository;

@Service
public class EDSeviceImpl implements EDService {

	@Autowired
	private CitizenPlansRepository citizenPlanRepo;

	@Autowired
	private CitizenApplicationRepository appRepo;

	@Autowired
	private CitizenIncomeDtlsRepository incomeRepo;

	@Autowired
	private CitizenGraduationDtlsRepository eduRepo;

	@Autowired
	private CitizenChildDtlsRepository childRepo;

	@Autowired
	private EligibilityDetailsRepository eligRepo;

	@Autowired
	private CoTriggersRepository triggersRepo;

	@Autowired
	private AppPlanRepository planRepo;

	@Override
	public EligibilityDtls determineEligibility(int caseNo) {

		// get planId
		CitizenPlansEntity planEntity = citizenPlanRepo.findByCaseNo(caseNo);
		int planId = planEntity.getPlanId();

		// get monthlyIncome & propertyIncome
		CitizenIncomeDtlsEntity incomeEntity = incomeRepo.findByCaseNo(caseNo);
		double monthlyIncome = incomeEntity.getMonthlyIncome();
		double propertyIncome = incomeEntity.getPropertyIncome();

		// get citizenAge
		CitizenApplicationEntity appEntity = appRepo.findByCaseNo(caseNo);
		Period period = Period.between(appEntity.getDob(), LocalDate.now());
		int age = period.getYears();

		CitizenGraduationDtlsEntity gradEntity = eduRepo.findByCaseNo(caseNo);

		EligibilityDtls eligdtls = new EligibilityDtls();

		// Eligibility for planName "SNAP"
		if (planRepo.findByPlanName("SNAP").equals(planId)) {
			eligdtls.setPlanName("SNAP");

			if (monthlyIncome <= 300) {

				eligdtls.setPlanStatus("APPROVED");
			} else {

				eligdtls.setPlanStatus("DENIED");
				eligdtls.setDenialReason("MONTHLY INCOME IS MORE THAN 300$");
			}
		} else

		// Eligibility for planName "CCAP"
		if (planRepo.findByPlanName("CCAP").equals(planId)) {

			List<CitizenChildDtlsEntity> childEntity = childRepo.findByCaseNo(caseNo);
			eligdtls.setPlanName("SNAP");

			if (monthlyIncome <= 300 && childEntity.size() > 0) {
				childEntity.forEach(child -> {
					Period childAge = Period.between(child.getKidsDob(), LocalDate.now());
					if (childAge.getYears() > 16) {

						eligdtls.setPlanStatus("DENIED");
						eligdtls.setDenialReason("KIDS AGE IS MORE THAN 16");
					} else {

						eligdtls.setPlanStatus("APPROVED");
					}
				});
			} else {
				eligdtls.setPlanStatus("DENIED");
				eligdtls.setDenialReason("INCOME MORE THAN 300$ OR NO KIDS");
			}

		} else

		if (planRepo.findByPlanName("Medicaid").equals(planId)) {

			eligdtls.setPlanName("Medicaid");
			if (monthlyIncome <= 300 && propertyIncome == 0) {

				eligdtls.setPlanStatus("APPROVED");
			} else {

				eligdtls.setPlanStatus("DENIED");
				eligdtls.setDenialReason("MONTHLY INCOME IS MORE THAN 300$ AND PROPERTY INCOME IS NOT ZERO");
			}

		} else

		if (planRepo.findByPlanName("Medicare").equals(planId)) {

			if (age >= 65) {

				eligdtls.setPlanStatus("APPROVED");
			} else {

				eligdtls.setPlanStatus("DENIED");
				eligdtls.setDenialReason("CITIZEN AGE IS MORE THAN 65");
			}
			eligdtls.setPlanName("Medicare");

		} else

		if (planRepo.findByPlanName("QHP").equals(planId)) {

			eligdtls.setPlanStatus("APPROVED");
			eligdtls.setPlanName("QHP");

		} else

		if (planRepo.findByPlanName("NJW").equals(planId)) {

			if ("UNEMPLOYED".equals(appEntity.getEmploymentStatus())
					&& "GRADUATED".equals(gradEntity.getHighestDegree())) {

				eligdtls.setPlanStatus("APPROVED");

			} else {

				eligdtls.setPlanStatus("DENIED");
				eligdtls.setDenialReason("CITIZEN IS NOT GRADUATED OR EMPLOYED");
			}
			eligdtls.setPlanName("NJW");

		}

		if ("APPROVED".equals(eligdtls.getPlanStatus())) {
			eligdtls.setStartDate(LocalDate.now());
			eligdtls.setEndDate(LocalDate.of(2022, 10, 29));
			eligdtls.setBenefitAmount(1500.00);
		}

		eligdtls.setCaseNo(caseNo);
		saveEligibilityDtls(eligdtls);
		setCorrespondenceTrigger(eligdtls);
		return eligdtls;
	}

	private void saveEligibilityDtls(EligibilityDtls eligdtls) {

		EligibilityDetailsEntity entity = new EligibilityDetailsEntity();
		BeanUtils.copyProperties(eligdtls, entity);
		eligRepo.save(entity);
	}

	private void setCorrespondenceTrigger(EligibilityDtls eligdtls) {

		CoTriggersEntity entity = new CoTriggersEntity();
		entity.setCaseNo(eligdtls.getCaseNo());
		entity.setTrgStatus('P');

		triggersRepo.save(entity);

	}
}
