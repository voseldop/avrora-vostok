package info.podlesov.avroravostok.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOError;
import java.io.IOException;

import info.podlesov.avroravostok.networking.OfficeHelper;

/**
 * Created by voseldop on 27/09/2017.
 */

public class AccountInfo {
    private static final String OWNER_NAME_PROPERTY = "FIO";
    private static final String ACCOUNT_NUMBER_PROPERTY = "DisplayNumber";
    private static final String DEBT_PROPERTY = "Debt";
    private static final String CHARGE_PROPERTY = "Charge";
    private String mOwnerName;
    private String mAccountNumber;
    private String mBalanceStatus;
    public String getAccountNumber() {
        return mAccountNumber;
    }

    public String getOwnerName() {
        return mOwnerName;
    }

    public String getBalanceStatus() {
        return mBalanceStatus;
    }

    public static AccountInfo get(OfficeHelper helper) throws IOException, JSONException {
        return  AccountInfo.fromString(helper.status());
    }

    public static AccountInfo fromString(String str) throws JSONException  {
        AccountInfo res = new AccountInfo();
        res.parseString(str);
        return res;
    }

    private void parseString(String str) throws JSONException {
        JSONObject obj = new JSONObject(str);
        mOwnerName = obj.getString(OWNER_NAME_PROPERTY);
        mAccountNumber = obj.getString(ACCOUNT_NUMBER_PROPERTY);
        double balance = obj.getDouble(DEBT_PROPERTY) + obj.getDouble(CHARGE_PROPERTY);
        mBalanceStatus = Double.toString(balance);
    }
}
