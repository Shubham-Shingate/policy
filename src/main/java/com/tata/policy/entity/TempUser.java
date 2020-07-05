package com.tata.policy.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "TEMP_USER")
public class TempUser {

	@Id
	@Column(name = "MOBILE_NUMBER")
	private String mobileNumber;

	@Column(name = "OTP")
	private String otp;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EXPIRE_TIME")
	private Date expireTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_TIME")
	private Date createdTime;

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public TempUser() {

	}

	public TempUser(String mobileNumber, String otp, Date expireTime, Date createdTime) {
		this.mobileNumber = mobileNumber;
		this.otp = otp;
		this.expireTime = expireTime;
		this.createdTime = createdTime;
	}

	@Override
	public String toString() {
		return "TempUser [mobileNumber=" + mobileNumber + ", otp=" + otp + ", expireTime=" + expireTime
				+ ", createdTime=" + createdTime + "]";
	}
}