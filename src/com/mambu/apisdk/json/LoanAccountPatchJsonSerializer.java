package com.mambu.apisdk.json;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.loans.shared.model.DisbursementDetails;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.PrincipalPaymentAccountSettings;

/**
 * LoanAccountPatchJsonSerializer implements custom JsonSerializer for PATCH Loan Account API requests. It specifies
 * PATCH API fields inclusion strategy as well as providing custom JSON formating as expected by Mambu API specification
 * 
 * For more details on Mambu PATCH Loan API specification see MBU-7758, MBU-11481 and @see
 * <a href="https://developer.mambu.com/customer/en/portal/articles/1617482-loans-api">Loans API</a>
 * 
 * @author mdanilkis
 * 
 */
public class LoanAccountPatchJsonSerializer implements JsonSerializer<LoanAccount> {

	/**
	 * A list of fields supported by the PATCH loan account API. See MBU-7758, MBU-11481, MBU-12143, MBU-13376
	 * 
	 * Note, since 4.0 the EXPECTED_DISBURSEMENT_DATE and FIRST_REPAYMENT_DATE are part of the
	 * DisbursementDetails.class, see MBU-11481
	 * 
	 */
	private final static Set<String> modifiableLoanAccountFields = new HashSet<String>(Arrays.asList(
			APIData.LOAN_AMOUNT, APIData.INTEREST_RATE, APIData.INTEREST_RATE_SPREAD, APIData.REPAYMENT_INSTALLMENTS,
			APIData.REPAYMENT_PERIOD_COUNT, APIData.REPAYMENT_PERIOD_UNIT, APIData.GRACE_PERIOD,
			APIData.PRNICIPAL_REPAYMENT_INTERVAL, APIData.PENALTY_RATE, APIData.PERIODIC_PAYMENT,
			APIData.DISBURSEMENT_DETAILS, APIData.PRINCIPAL_PAYMENT_SETTINGS, APIData.ARREARS_TOLERANCE_PERIOD));

	/**
	 * A list of fields from the DisbursementDetails class supported by PATCH loan account API.
	 * 
	 * See MBU-11515 and MBU-11481: Should specify the "expectedDisbursementDate" and "firstRepaymentDate", as before,
	 * at loan account level
	 */
	private final static Set<String> disbursementFields = new HashSet<String>(Arrays.asList(
			APIData.EXPECTED_DISBURSEMENT_DATE, APIData.FIRST_REPAYMENT_DATE));

	/**
	 * A list of fields from PrincipalPaymentAccountSettings supported by the PATCH loan API.
	 * 
	 * See MBU-12143: "principalPaymentSettings":{"amount":"100.00"} and
	 * "principalPaymentSettings":{"percentage":"20.00"}
	 */
	private final static Set<String> principalPaymentSettingsFields = new HashSet<String>(Arrays.asList(APIData.AMOUNT,
			APIData.PERCENTAGE));

	// Create inclusion strategy for Loan PATCH API. Include allowed LoanAccount, DisbursementDetails and
	// PrincipalPaymentAccountSettings fields
	private final static JsonFieldsInclusionStrategy loanPatchInclusionStrategy;
	static {
		loanPatchInclusionStrategy = new JsonFieldsInclusionStrategy(LoanAccount.class, modifiableLoanAccountFields);
		loanPatchInclusionStrategy.addInclusion(DisbursementDetails.class, disbursementFields);
		loanPatchInclusionStrategy.addInclusion(PrincipalPaymentAccountSettings.class, principalPaymentSettingsFields);
	}

	public LoanAccountPatchJsonSerializer() {

	}

	// Serialize LoanAccount using custom inclusion strategy as well as helper methods to extract DisbursementDetails
	// and PrincipalPaymentAccountSettings fields
	@Override
	public JsonElement serialize(LoanAccount loanAccount, Type typeOfSrc, JsonSerializationContext context) {

		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder();
		// Add our loanPatchInclusionStrategy
		gsonBuilder.addSerializationExclusionStrategy(loanPatchInclusionStrategy);

		Gson gson = gsonBuilder.create();
		JsonElement loanAccountJsonElement = gson.toJsonTree(loanAccount);
		JsonObject loanResult = loanAccountJsonElement.getAsJsonObject();

		// Return as "{\"loanAccount\":" +{ accountFields + "}}";
		JsonObject loanSubsetObject = new JsonObject();
		loanSubsetObject.add(APIData.LOAN_ACCOUNT, loanResult);
		return loanSubsetObject;
	}
}
