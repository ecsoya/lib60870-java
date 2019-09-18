package org.ecsoya.iec60870.cs104;

import java.util.ArrayList;

import javax.security.cert.X509Certificate;

public class TlsSecurityInformation {
	private X509Certificate ownCertificate;

	private ArrayList<X509Certificate> otherCertificates;

	private ArrayList<X509Certificate> caCertificates;

	private String targetHostName = null;

	// Check certificate chain validity with registered CAs
	private boolean chainValidation = true;

	private boolean allowOnlySpecificCertificates = false;

	public TlsSecurityInformation(String targetHostName, X509Certificate ownCertificate) {

		this.targetHostName = targetHostName;
		this.ownCertificate = ownCertificate;

		otherCertificates = new ArrayList<X509Certificate>();
		caCertificates = new ArrayList<X509Certificate>();
	}

	public TlsSecurityInformation(X509Certificate ownCertificate) {
		this.ownCertificate = ownCertificate;

		otherCertificates = new ArrayList<X509Certificate>();
		caCertificates = new ArrayList<X509Certificate>();
	}

	public final void AddAllowedCertificate(X509Certificate allowedCertificate) {
		otherCertificates.add(allowedCertificate);
	}

	public final void AddCA(X509Certificate caCertificate) {
		caCertificates.add(caCertificate);
	}

	public final ArrayList<X509Certificate> getAllowedCertificates() {
		return this.otherCertificates;
	}

	/**
	 * Gets or sets a value indicating whether this
	 * <see cref="lib60870.TlsSecurityInformation"/> allow only specific
	 * certificates.
	 * 
	 * <value><c>true</c> if allow only specific certificates; otherwise,
	 * <c>false</c>.</value>
	 */
	public final boolean getAllowOnlySpecificCertificates() {
		return this.allowOnlySpecificCertificates;
	}

	public final ArrayList<X509Certificate> getCaCertificates() {
		return this.caCertificates;
	}

	/**
	 * Gets or sets a value indicating whether this
	 * <see cref="lib60870.TlsSecurityInformation"/> performs a X509 chain
	 * validation against the registered CA certificates
	 * 
	 * <value><c>true</c> if chain validation; otherwise, <c>false</c>.</value>
	 */
	public final boolean getChainValidation() {
		return this.chainValidation;
	}

	public final X509Certificate getOwnCertificate() {
		return this.ownCertificate;
	}

	public final String getTargetHostName() {
		return this.targetHostName;
	}

	public final void setAllowedCertificates(ArrayList<X509Certificate> value) {
		otherCertificates = value;
	}

	public final void setAllowOnlySpecificCertificates(boolean value) {
		allowOnlySpecificCertificates = value;
	}

	public final void setChainValidation(boolean value) {
		chainValidation = value;
	}

	public final void setOwnCertificate(X509Certificate value) {
		ownCertificate = value;
	}
}