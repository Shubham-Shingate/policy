package com.tata.policy.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tata.policy.dao.PolicyDAO;
import com.tata.policy.entity.Policy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class PolicyServiceImpl implements PolicyService {

	@Value("${smsAPIkey}")
	private String smsAPIkey;
	
	@Autowired
	private PolicyDAO policyDAO;

	@Override // This is just a utility method to authenticate JWT. No need of @Transactional
	public Claims authenticateToken(String jwtToken, String SECRET_KEY) {
		Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
				.parseClaimsJws(jwtToken).getBody();
		return claims;
	}

	@Override
	@Transactional
	public boolean checkMobileNumber(String mobileNumber, String otp, Date exp, Date now) {
		boolean result = policyDAO.checkMobileNumber(mobileNumber, otp, exp, now);
		return result;
	}

	@Override
	@Transactional
	public Policy getPolicyInfo(String policyNumber, Claims claims) {
		Policy policyInfo = policyDAO.getPolicyInfo(policyNumber, claims);
		return policyInfo;
	}

	@Override
	@Transactional
	public String validateEmailAndDOB(String emailId, String dateOfBirth) {
		String emailAddress = policyDAO.validateEmailAndDOB(emailId, dateOfBirth);
		return emailAddress;
	}

	@Override
	@Transactional
	public String validateMobileNumberAndDOB(String mobileNo, String dateOfBirth) {
		String mobileNumber = policyDAO.validateMobileNumberAndDOB(mobileNo, dateOfBirth);
		return mobileNumber;
	}

	@Override
	@Transactional
	public boolean updateMobileNumber(String newMobileNo, String policyNumber, Claims claims) {
		boolean mobileNumberUpdated = policyDAO.updateMobileNumber(newMobileNo, policyNumber, claims);
		return mobileNumberUpdated;
	}

	@Override
	@Transactional
	public boolean updateEmailAddress(String newEmailId, String policyNumber, Claims claims) {
		boolean emailAddressUpdated = policyDAO.updateEmailAddress(newEmailId, policyNumber, claims);
		return emailAddressUpdated;
	}

	@Override
	@Transactional
	public boolean updatePANnumber(String newPANno, String policyNumber, Claims claims) {
		boolean panNumberUpdated = policyDAO.updatePANnumber(newPANno, policyNumber, claims);
		return panNumberUpdated;
	}

	@Override
	@Transactional
	public boolean mobileNoOptinStatus(String mobileNo) {
		boolean optinStatus = policyDAO.mobileNoOptinStatus(mobileNo);
		return optinStatus;
	}

	@Override
	@Transactional
	public boolean checkMobileNo(String mobileNo) {
		boolean mobileNumberRegistered = policyDAO.checkMobileNo(mobileNo);
		return mobileNumberRegistered;
	}

	@Override // This is just a utility method to authenticate JWT. No need of @Transactional
	public String sendOTPsms(int OTP, String mobileNo) throws MalformedURLException, IOException {

		// appending country code to the number
		String mobileNumber = "91" + mobileNo;

		// Construct data
		String apiKey = "apikey=" + smsAPIkey;
		String message = "&message=" + "Your One Time Password for whatsapp opt-in is- "+OTP;
		String sender = "&sender=" + "TXTLCL";
		String numbers = "&numbers=" + mobileNumber;
		String test = "&test=" + "true";

		// Send data
		HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
		String data = apiKey + numbers + message + sender + test;
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
		conn.getOutputStream().write(data.getBytes("UTF-8"));
		final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		final StringBuffer stringBuffer = new StringBuffer();
		String line;
		while ((line = rd.readLine()) != null) {
			stringBuffer.append(line);
		}
		rd.close();
		return stringBuffer.toString();
	}
}