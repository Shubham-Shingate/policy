package com.tata.policy.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.tata.policy.entity.Policy;
import com.tata.policy.entity.TempUser;

import io.jsonwebtoken.Claims;

@Repository
public class PolicyDAOImpl implements PolicyDAO {

	@Autowired
	private EntityManager entityManager;

	@Override
	public boolean checkMobileNumber(String mobileNumber, String otp, Date exp, Date now) {
		Session currentSession = entityManager.unwrap(Session.class);

		String hql = "SELECT mobileNumber FROM POLICY_MASTER WHERE MOBILE_NUMBER=:mobileNumber";
		Query<String> query = currentSession.createQuery(hql, String.class);
		query.setParameter("mobileNumber", mobileNumber);
		List<String> result = query.list();

		if (result.size() != 0) {
			TempUser tempUser = new TempUser(mobileNumber, otp, exp, now);
			// save the temp user to TEMP_USER table
			currentSession.saveOrUpdate(tempUser);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Policy getPolicyInfo(String policyNumber, Claims claims) {
		Session currentSession = entityManager.unwrap(Session.class);

		String hql = "FROM POLICY_MASTER WHERE POLICY_NUMBER=:policyNumber AND CUSTOMER_NAME=:customerName";
		Query<Policy> query = currentSession.createQuery(hql, Policy.class);
		query.setParameter("policyNumber", policyNumber);
		query.setParameter("customerName", claims.getIssuer());
		List<Policy> result = query.list();
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

	@Override
	public String validateEmailAndDOB(String emailId, String dateOfBirth) {
		Session currentSession = entityManager.unwrap(Session.class);

		String hql = "SELECT emailAddress FROM POLICY_MASTER WHERE DOB = TO_DATE('" + dateOfBirth
				+ "', 'DD-MM-YYYY') AND EMAIL_ADDRESS=:emailId";
		Query<String> query = currentSession.createQuery(hql, String.class);
		query.setParameter("emailId", emailId);
		List<String> result = query.list();
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

	@Override
	public String validateMobileNumberAndDOB(String mobileNo, String dateOfBirth) {
		Session currentSession = entityManager.unwrap(Session.class);

		String hql = "SELECT mobileNumber FROM POLICY_MASTER WHERE DOB = TO_DATE('" + dateOfBirth
				+ "', 'DD-MM-YYYY') AND MOBILE_NUMBER=:mobileNo";
		Query<String> query = currentSession.createQuery(hql, String.class);
		query.setParameter("mobileNo", mobileNo);
		List<String> result = query.list();
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

	@Override
	public boolean updateMobileNumber(String newMobileNo, String policyNumber, Claims claims) {
		/*
		 * Here update the mobile number of a policy if (policy number exists and policy
		 * owner is same as token owner) and return successful msg else return failure
		 * msg.
		 */
		Session currentSession = entityManager.unwrap(Session.class);
		// Before updating new mobile number, get the old mobile number so that
		// associations with old number in other tables can be identified.
		String hqlOldMobileNo = "SELECT mobileNumber FROM POLICY_MASTER WHERE POLICY_NUMBER=:policyNumber";
		Query<String> queryA = currentSession.createQuery(hqlOldMobileNo, String.class);
		queryA.setParameter("policyNumber", policyNumber);
		List<String> result = queryA.list();
		String oldMobileNo = null;

		if (!(result.isEmpty())) {
			oldMobileNo = result.get(0);
		}

		// Update the mobile number in POLICY_MASTER table.
		String hqlUpdateMobNo = "UPDATE POLICY_MASTER SET MOBILE_NUMBER=:newMobileNo WHERE POLICY_NUMBER=:policyNumber AND CUSTOMER_NAME=:customerName";
		Query<?> queryB = currentSession.createQuery(hqlUpdateMobNo);
		queryB.setParameter("newMobileNo", newMobileNo);
		queryB.setParameter("policyNumber", policyNumber);
		queryB.setParameter("customerName", claims.getIssuer());
		int policyMasterUpdateResult = queryB.executeUpdate();

		if (policyMasterUpdateResult != 0) { // Means mobile number is now updated in POLICY_MASTER table.
			// Since the mobile number is updated, also update the mobile no. in TEMP_USER
			// Table(used for storing current active token owners identified by their mob
			// no.)
			String hql = "UPDATE TEMP_USER SET MOBILE_NUMBER=:newMobileNo WHERE MOBILE_NUMBER=:oldMobileNo";
			Query<?> queryC = currentSession.createQuery(hql);
			queryC.setParameter("newMobileNo", newMobileNo);
			queryC.setParameter("oldMobileNo", oldMobileNo);
			queryC.executeUpdate();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean updateEmailAddress(String newEmailId, String policyNumber, Claims claims) {
		/*
		 * Here update the Email Address of a policy if (policy number exists and policy
		 * owner is same as token owner) and return successful msg else return failure
		 * msg.
		 */
		Session currentSession = entityManager.unwrap(Session.class);

		// Update the email address in POLICY_MASTER table.
		String hqlUpdateEmailId = "UPDATE POLICY_MASTER SET EMAIL_ADDRESS=:newEmailId WHERE POLICY_NUMBER=:policyNumber AND CUSTOMER_NAME=:customerName";
		Query<?> query = currentSession.createQuery(hqlUpdateEmailId);
		query.setParameter("newEmailId", newEmailId);
		query.setParameter("policyNumber", policyNumber);
		query.setParameter("customerName", claims.getIssuer());
		int policyMasterUpdateResult = query.executeUpdate();

		if (policyMasterUpdateResult != 0) { // Means email address is now updated in POLICY_MASTER table.
			/*
			 * Since the email address is updated in main POLICY_MASTER table, also update
			 * the email address in all the other tables where old Email ID was linked.
			 * Write that update code here !!
			 */
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean updatePANnumber(String newPANno, String policyNumber, Claims claims) {
		/*
		 * Here update the PAN Number of a policy if (policy number exists and policy
		 * owner is same as token owner) and return successful msg else return failure
		 * msg.
		 */
		Session currentSession = entityManager.unwrap(Session.class);

		// Update the PAN Number in POLICY_MASTER table.
		String hqlUpdatePANno = "UPDATE POLICY_MASTER SET CUSTOMER_PAN_NO=:newPANno WHERE POLICY_NUMBER=:policyNumber AND CUSTOMER_NAME=:customerName";
		Query<?> query = currentSession.createQuery(hqlUpdatePANno);
		query.setParameter("newPANno", newPANno);
		query.setParameter("policyNumber", policyNumber);
		query.setParameter("customerName", claims.getIssuer());
		int policyMasterUpdateResult = query.executeUpdate();

		if (policyMasterUpdateResult != 0) { // Means PAN Number is now updated in POLICY_MASTER table.
			/*
			 * Since the PAN number is updated in main POLICY_MASTER table, also update the
			 * PAN number in all the other tables where old PAN number was linked. Write
			 * that update code here !!
			 */
			return true;
		} else {
			return false;
		}
	}

	/* Methods related to Whatsapp Opt-In functionality */
	@Override
	public boolean mobileNoOptinStatus(String mobileNo) {
		return checkMobileNoInTable(mobileNo, "WHATSAPP_OPT_IN");
	}

	@Override
	public boolean checkMobileNo(String mobileNo) {
		return checkMobileNoInTable(mobileNo, "POLICY_MASTER");
	}
	
	public boolean checkMobileNoInTable(String mobileNo, String entityName) {
		Session currentSession = entityManager.unwrap(Session.class);
		String hql = "SELECT mobileNumber FROM "+entityName+" WHERE MOBILE_NUMBER=:mobileNo";
		Query<String> query = currentSession.createQuery(hql, String.class);
		query.setParameter("mobileNo", mobileNo);
		List<String> result = query.list();
		if(result.isEmpty())
		{
			return false;
		}else {
			return true;
		}
	}
}