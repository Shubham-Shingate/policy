package com.tata.policy.dao;

import java.util.Date;

import com.tata.policy.entity.Policy;

import io.jsonwebtoken.Claims;

public interface PolicyDAO {

	public boolean checkMobileNumber(String mobileNumber, String otp, Date exp, Date now);

	public Policy getPolicyInfo(String policyNumber, Claims claims);

	public String validateEmailAndDOB(String emailId, String dateOfBirth);

	public String validateMobileNumberAndDOB(String mobileNo, String dateOfBirth);

	public boolean updateMobileNumber(String newMobileNo, String policyNumber, Claims claims);

	public boolean updateEmailAddress(String newEmailId, String policyNumber, Claims claims);

	public boolean updatePANnumber(String newPANno, String policyNumber, Claims claims);

	public boolean mobileNoOptinStatus(String mobileNo);

	public boolean checkMobileNo(String mobileNo);

}