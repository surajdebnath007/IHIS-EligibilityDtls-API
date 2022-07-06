package com.ihis.bindings;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EligibilityDtls {

	private int caseNo;

	private String planName;

	private String planStatus;

	private LocalDate startDate;

	private LocalDate endDate;

	private double benefitAmount;

	private String denialReason;

}
