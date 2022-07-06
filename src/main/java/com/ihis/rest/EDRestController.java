package com.ihis.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ihis.bindings.EligibilityDtls;
import com.ihis.service.EDService;

@RestController
public class EDRestController {

	@Autowired
	private EDService service;

	@GetMapping("/eligibility/{caseNo}")
	public EligibilityDtls determineEligibility(@PathVariable("caseNo") int caseNo) {
		return service.determineEligibility(caseNo);
	}
}
