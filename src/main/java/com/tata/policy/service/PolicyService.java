package com.tata.policy.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import com.tata.policy.entity.Policy;

import io.jsonwebtoken.Claims;

public interface PolicyService {

	public Claims authenticateToken(String jwtToken, String SECRET_KEY);
	
	public boolean checkMobileNumber(String mobileNumber, String otp, Date exp, Date now);
	
	public Policy getPolicyInfo(String policyNumber, Claims claims);
	
	public String validateEmailAndDOB(String emailId, String dateOfBirth);

	public String validateMobileNumberAndDOB(String mobileNo, String dateOfBirth);

	public boolean updateMobileNumber(String newMobileNo, String policyNumber, Claims claims);

	public boolean updateEmailAddress(String newEmailId, String policyNumber, Claims claims);

	public boolean updatePANnumber(String newPANno, String policyNumber, Claims claims);

	public boolean mobileNoOptinStatus(String mobileNo);

	public boolean checkMobileNo(String mobileNo);

	public String sendOTPsms(int OTP, String mobileNo) throws MalformedURLException, IOException;
	
}