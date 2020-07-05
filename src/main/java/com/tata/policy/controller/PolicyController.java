package com.tata.policy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tata.policy.constants.PolicyAppConstants;
import com.tata.policy.entity.Policy;
import com.tata.policy.service.PolicyService;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.io.IOException;
import java.security.Key;

import io.jsonwebtoken.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/policy")
public class PolicyController {

	@Value("${SECRET_KEY}")
	private String SECRET_KEY;

	@Autowired
	private PolicyService policyService;
	
	/* -----------------------------OBTAIN JWT TOKEN----------------------------- */
	
	@PostMapping("/securityJwtToken")
	public Map<String, Object> createJWT(@RequestParam String id, @RequestParam String issuer,
			@RequestParam String subject, @RequestParam long ttlMillis, @RequestParam String mobileNumber,
			@RequestParam String otp) {

		Map<String, Object> jsonResponseMap = new HashMap<String, Object>();

		// Current and token expiry Date and Time evaluation
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		Date exp = null;
		if (ttlMillis > 0) {
			long expMillis = nowMillis + ttlMillis;
			exp = new Date(expMillis);
		}

		// Check mobile number in POLICY_MASTER and create entry in TEMP_USER for new token holder.
		boolean result = policyService.checkMobileNumber(mobileNumber, otp, exp, now);

		if (result == true) {
			// The JWT signature algorithm we will be using to sign the token
			SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

			// We will sign our JWT with our ApiKey secret
			byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
			Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

			// Let's set the JWT Claims
			JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).setIssuer(issuer)
					.signWith(signatureAlgorithm, signingKey);

			// if it has been specified, let's add the expiration
			builder.setExpiration(exp);
			
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.SERVICE_REQ_SUCCESS);
			jsonResponseMap.put(PolicyAppConstants.TOKEN, builder.compact());
			jsonResponseMap.put(PolicyAppConstants.REFRESH, null);
			return jsonResponseMap;
		} else {
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Invalid Mobile Number");
			return jsonResponseMap;
		}
	}
	
	/* -----------------------------FETCH CUSTOMER POLICY INFO----------------------------- */
	
	@PostMapping("/customerPolicyInfo")
	public Map<String, Object> getCustomerPolicyInfo(@RequestParam String policyNumber, @RequestParam String jwtToken) {

		Map<String, Object> jsonResponseMap = new HashMap<String, Object>();
		Claims claims = null;

		try {
			// This line will throw an exception if it is not a Valid Token (as expected)
			claims = policyService.authenticateToken(jwtToken, SECRET_KEY);
		} catch (SignatureException | ExpiredJwtException e) {
			e.printStackTrace();
			System.out.println("INVALID TOKEN");
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Invalid Token");
			return jsonResponseMap;
		}

		if (claims != null) {
			// Here return Policy object if policy number is valid or return null from DAO
			Policy policyInfo = policyService.getPolicyInfo(policyNumber, claims);
			if (policyInfo != null) {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.GET_DATA_REQ_SUCCESS);
				jsonResponseMap.put(PolicyAppConstants.DATA, policyInfo);
				return jsonResponseMap;
			} else {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
				jsonResponseMap.put(PolicyAppConstants.ERROR,
						"Token is valid but Policy no. does not exist OR unauthorized to access info of this Policy no.");
				return jsonResponseMap;
			}
		} else {
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Token valid but claim is null");
			return jsonResponseMap;
		}
	}

	/* -----------------------------CUSTOMER EMAIL VALIDATION----------------------------- */
	
	@PostMapping("/emailValidation")
	public Map<String, Object> validateEmail(@RequestParam String emailId, @RequestParam String dateOfBirth,
			@RequestParam String jwtToken) {

		Map<String, Object> jsonResponseMap = new HashMap<String, Object>();
		Claims claims = null;

		try {
			// This line will throw an exception if it is not a Valid Token (as expected)
			claims = policyService.authenticateToken(jwtToken, SECRET_KEY);
		} catch (SignatureException | ExpiredJwtException e) {
			e.printStackTrace();
			System.out.println("INVALID TOKEN");
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Invalid Token");
			return jsonResponseMap;
		}

		if (claims != null) {
			// This line returns EmailId if given emailId and DOB are valid in POLICY_MASTER table.
			String emailAddress = policyService.validateEmailAndDOB(emailId, dateOfBirth);
			if (emailAddress != null) {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.VALIDATION_REQ_SUCCESS);
				jsonResponseMap.put(PolicyAppConstants.DATA, emailAddress);
				return jsonResponseMap;
			} else {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
				jsonResponseMap.put(PolicyAppConstants.ERROR, "Token is valid but Email Id or Date of Birth Invalid");
				return jsonResponseMap;
			}
		} else {
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Token valid but claim is null");
			return jsonResponseMap;
		}
	}
	
	/* -----------------------------CUSTOMER MOBILE NUMBER VALIDATION----------------------------- */
	
	@PostMapping("/mobileNumberValidation")
	public Map<String, Object> validateMobileNumber(@RequestParam String mobileNo, @RequestParam String dateOfBirth,
			@RequestParam String jwtToken) {

		Map<String, Object> jsonResponseMap = new HashMap<String, Object>();
		Claims claims = null;

		try {
			// This line will throw an exception if it is not a Valid Token (as expected)
			claims = policyService.authenticateToken(jwtToken, SECRET_KEY);
		} catch (SignatureException | ExpiredJwtException e) {
			e.printStackTrace();
			System.out.println("INVALID TOKEN");
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Invalid Token");
			return jsonResponseMap;
		}

		if (claims != null) {
			// This line returns Mobile Number if given Mobile Number and DOB are valid in POLICY_MASTER table.
			String mobileNumber = policyService.validateMobileNumberAndDOB(mobileNo, dateOfBirth);
			if (mobileNumber != null) {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.VALIDATION_REQ_SUCCESS);
				jsonResponseMap.put(PolicyAppConstants.DATA, mobileNumber);
				return jsonResponseMap;
			} else {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
				jsonResponseMap.put(PolicyAppConstants.ERROR,
						"Token is valid but Mobile Number or Date of Birth Invalid");
				return jsonResponseMap;
			}
		} else {
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Token valid but claim is null");
			return jsonResponseMap;
		}
	}

	/* -----------------------------CUSTOMER MOBILE NUMBER UPDATION----------------------------- */
	
	@PutMapping("/mobileNumberUpdation")
	public Map<String, Object> updateMobileNumber(@RequestParam String newMobileNo, @RequestParam String policyNumber,
			@RequestParam String jwtToken) {

		Map<String, Object> jsonResponseMap = new HashMap<String, Object>();
		Claims claims = null;

		try {
			// This line will throw an exception if it is not a Valid Token (as expected)
			claims = policyService.authenticateToken(jwtToken, SECRET_KEY);
		} catch (SignatureException | ExpiredJwtException e) {
			e.printStackTrace();
			System.out.println("INVALID TOKEN");
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Invalid Token");
			return jsonResponseMap;
		}

		if (claims != null) {
			// This line updates the mobile number against a policy if policy number exists and
			// "token owner" = "policy owner".
			boolean mobileNumberUpdated = policyService.updateMobileNumber(newMobileNo, policyNumber, claims);
			if (mobileNumberUpdated == true) {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.SERVICE_REQ_SUCCESS);
				jsonResponseMap.put(PolicyAppConstants.DATA, "SR10001");
				jsonResponseMap.put(PolicyAppConstants.MESSAGE, "Service Request for Mobile Number Updation Generated");
				return jsonResponseMap;
			} else {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
				jsonResponseMap.put(PolicyAppConstants.ERROR,
						"Token is valid but Policy no. does not exist OR unauthorized to update info of this Policy no.");
				return jsonResponseMap;
			}
		} else {
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Token valid but claim is null");
			return jsonResponseMap;
		}
	}
	
	/* -----------------------------CUSTOMER EMAIL ADDRESS UPDATION----------------------------- */
	
	@PutMapping("/emailAddressUpdation")
	public Map<String, Object> updateEmailAddress(@RequestParam String newEmailId, @RequestParam String policyNumber,
			@RequestParam String jwtToken) {

		Map<String, Object> jsonResponseMap = new HashMap<String, Object>();
		Claims claims = null;

		try {
			// This line will throw an exception if it is not a Valid Token (as expected)
			claims = policyService.authenticateToken(jwtToken, SECRET_KEY);
		} catch (SignatureException | ExpiredJwtException e) {
			e.printStackTrace();
			System.out.println("INVALID TOKEN");
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Invalid Token");
			return jsonResponseMap;
		}

		if (claims != null) {
			// Here update the email address against a policy if policy number exists and
			// "token owner" = "policy owner".
			boolean emailAddressUpdated = policyService.updateEmailAddress(newEmailId, policyNumber, claims);
			if (emailAddressUpdated == true) {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.SERVICE_REQ_SUCCESS);
				jsonResponseMap.put(PolicyAppConstants.DATA, "SR10002");
				jsonResponseMap.put(PolicyAppConstants.MESSAGE, "Service Request for Email Address Updation Generated");
				return jsonResponseMap;
			} else {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
				jsonResponseMap.put(PolicyAppConstants.ERROR,
						"Token is valid but Policy no. does not exist OR unauthorized to update info of this Policy no.");
				return jsonResponseMap;
			}
		} else {
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Token valid but claim is null");
			return jsonResponseMap;
		}
	}
	
	/* -----------------------------CUSTOMER PAN NO. UPDATION----------------------------- */
	
	@PutMapping("/PANcardUpdation")
	public Map<String, Object> updatePANnumber(@RequestParam String newPANno, @RequestParam String policyNumber,
			@RequestParam String jwtToken) {

		Map<String, Object> jsonResponseMap = new HashMap<String, Object>();
		Claims claims = null;

		try {
			// This line will throw an exception if it is not a Valid Token (as expected)
			claims = policyService.authenticateToken(jwtToken, SECRET_KEY);
		} catch (SignatureException | ExpiredJwtException e) {
			e.printStackTrace();
			System.out.println("INVALID TOKEN");
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Invalid Token");
			return jsonResponseMap;
		}

		if (claims != null) {
			// This line updates the PAN number against a policy if policy number exists and
			// "token owner" = "policy owner".
			boolean panNumberUpdated = policyService.updatePANnumber(newPANno, policyNumber, claims);
			if (panNumberUpdated == true) {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.SERVICE_REQ_SUCCESS);
				jsonResponseMap.put(PolicyAppConstants.DATA, "SR10003");
				jsonResponseMap.put(PolicyAppConstants.MESSAGE, "Service Request for PAN Number Updation Generated");
				return jsonResponseMap;
			} else {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
				jsonResponseMap.put(PolicyAppConstants.ERROR,
						"Token is valid but Policy no. does not exist OR unauthorized to update info of this Policy no.");
				return jsonResponseMap;
			}
		} else {
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
			jsonResponseMap.put(PolicyAppConstants.ERROR, "Token valid but claim is null");
			return jsonResponseMap;
		}
	}
	
	/* -----------------------------WHATSAPP OPT-IN TWO STEP PROCESS (step-1)----------------------------- */
	
	@PostMapping("/whatsappOptin1")
	public Map<String, Object> whatsappOptin1(@RequestParam String mobileNo) {

		Map<String, Object> jsonResponseMap = new HashMap<String, Object>();

		// Check if mobile number is present already in WHATSAPP_OPT_IN table, return
		// true if yes and false if No.
		boolean optinStatus = policyService.mobileNoOptinStatus(mobileNo);

		if (optinStatus == true) { //means this Mobile No. is already opted in. (Old User!)
			jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.OLD_USER);
			jsonResponseMap.put(PolicyAppConstants.MESSAGE, "Thank you, this mobile number is already Opted In");
			return jsonResponseMap;
		} else { // means Mobile No. not opted in. (New User!) hence check the number in POLICY_MASTER whether he/she is valid policy holder. 
			boolean mobileNumberRegistered = policyService.checkMobileNo(mobileNo);
			
			if (mobileNumberRegistered == true) { //means mobile no. is present in POLICY_MASTER, hence valid user ! SEND THE OTP.
				/* Create the OTP */
				int OTP = (new Random()).nextInt(9999);
				
				/* Send OTP SMS*/
				try {
					policyService.sendOTPsms(OTP, mobileNo);
				} catch (IOException e) {
					e.printStackTrace();
					jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
					jsonResponseMap.put(PolicyAppConstants.ERROR, "An Error occured while sending OTP");
					return jsonResponseMap;
				}
				
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.OTP_SENT);
				jsonResponseMap.put(PolicyAppConstants.MESSAGE, "Whatsapp Opt-In OTP sent on the mobile number");
				return jsonResponseMap;
			} else {
				jsonResponseMap.put(PolicyAppConstants.STATUS, PolicyAppConstants.REQ_UNSUCCESS);
				jsonResponseMap.put(PolicyAppConstants.ERROR, "Invalid Mobile Number");
				return jsonResponseMap;
			}		
		}
	}
}