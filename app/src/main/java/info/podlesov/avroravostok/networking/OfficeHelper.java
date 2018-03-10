package info.podlesov.avroravostok.networking;

import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import info.podlesov.avroravostok.data.CounterInfo;

/**
 * Created by voseldop on 26/09/2017.
 */

public class OfficeHelper {
    private final static String COOKIE_HOST = "http://avrora-vostok.ru";
    private final static String OFFICE_HELPER_URL = "http://avrora-vostok.ru/privoff/OfficeHelper.php";
    private final static String OFFICE_LOGIN_URL = "http://avrora-vostok.ru/privoff";
    private final static String OFFICE_COUNTER_DATA = "http://avrora-vostok.ru/privoff/requestCntrData.php?_search=false&nd=1507178569427&rows=10000&page=1&sidx=id&sord=asc";
    private final static String OFFICE_CHARGES_DATA = "http://avrora-vostok.ru/privoff/requestChargesData.php?_search=false&nd=1507178569427&rows=10000&page=1&sidx=id&sord=asc";
    private CookieManager cookieManager;
    private String token;
    private String username;
    private String password;
    private String tsgCode;

    private static OfficeHelper instance;

    private OfficeHelper() {
        cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    };

    public static OfficeHelper getInstance() {
        if (instance == null) {
            instance = new OfficeHelper();
        }
        return instance;
    }

    private String readFullyAsString(InputStream inputStream, String encoding) throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    private ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }

    public void init() throws IOException {
        cookieManager.getCookieStore().removeAll();

        HttpURLConnection connection = (HttpURLConnection) new URL(OFFICE_LOGIN_URL).openConnection();
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Http Error");
        }
        connection.disconnect();
    }

    public void login() throws IOException, URISyntaxException {
        login(username, password, tsgCode);
    }

    private void login(String user, String password, String tsgCode) throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) new URL(OFFICE_HELPER_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        if (cookieManager.getCookieStore().getCookies().size() > 0) {
            // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
            connection.setRequestProperty("Cookie",
                    TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
        }

        String formdata="method=login&tsgcode="+tsgCode+"&login="+user+"&password="+password;
        byte [] output = formdata.getBytes();
        connection.setRequestProperty("Content-Length",
                Integer.toString(output.length));

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(output);
        out.flush();

        int reponseCode = connection.getResponseCode();
        if (reponseCode!=HttpURLConnection.HTTP_OK) {
            throw new IOException("Http code " + reponseCode);

        }

        String response = readFullyAsString(connection.getInputStream(), "UTF-8");
        String [] reply = response.split("\\|");
        String loginCode = reply[0].replaceAll("[^\\x00-\\x7F]", "");

        int code = Integer.parseInt(loginCode);
        if (code != 0) {
            throw new IOException("Office error " + reply[0]);
        }

        token = reply[1];
        cookieManager.getCookieStore().add(new URI(COOKIE_HOST), new HttpCookie("token", reply[1]));
        cookieManager.getCookieStore().add(new URI(COOKIE_HOST), new HttpCookie("tsg", tsgCode));
        connection.disconnect();
    }

    private String getRequest(String url, String formData) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url+"?"+formData).openConnection();
        connection.setDoInput(true);
        connection.setUseCaches(false);

        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        if (cookieManager.getCookieStore().getCookies().size() > 0) {
            // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
            connection.setRequestProperty("Cookie",
                    TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
        }


        int reponseCode = connection.getResponseCode();
        if (reponseCode!=HttpURLConnection.HTTP_OK) {
            throw new IOException("Http code " + reponseCode);

        }
        String info = readFullyAsString(connection.getInputStream(), "UTF-8");
        connection.disconnect();
        return info;
    }

    private String postRequest(String url, String formData) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        if (cookieManager.getCookieStore().getCookies().size() > 0) {
            // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
            connection.setRequestProperty("Cookie",
                    TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
        }

        byte [] output = formData.getBytes();
        connection.setRequestProperty("Content-Length",
                Integer.toString(output.length));

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(output);
        out.flush();

        int reponseCode = connection.getResponseCode();
        if (reponseCode!=HttpURLConnection.HTTP_OK) {
            throw new IOException("Http code " + reponseCode);

        }
        String info = readFullyAsString(connection.getInputStream(), "UTF-8");
        connection.disconnect();
        return info;
    }

    public String getCounterData() throws IOException {
        String formData = "_search=false&nd=1507178569427&rows=10000&page=1&sidx=id&sord=asc";
        return getRequest(OFFICE_COUNTER_DATA, formData);
    }

    public String getChargesData() throws IOException {
        String formData = "_search=false&nd=1507178569427&rows=10000&page=1&sidx=id&sord=asc";
        return getRequest(OFFICE_CHARGES_DATA, formData);
    }

    public String status() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(OFFICE_HELPER_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        if (cookieManager.getCookieStore().getCookies().size() > 0) {
            // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
            connection.setRequestProperty("Cookie",
                    TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
        }

        String formdata="method=getAccountInfo";
        byte [] output = formdata.getBytes();
        connection.setRequestProperty("Content-Length",
                Integer.toString(output.length));

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(output);
        out.flush();

        int reponseCode = connection.getResponseCode();
        if (reponseCode!=HttpURLConnection.HTTP_OK) {
            throw new IOException("Http code " + reponseCode);

        }
        String info = readFullyAsString(connection.getInputStream(), "UTF-8");
        connection.disconnect();
        return info;
    }

    public OfficeHelper setUsername(String username) {
        this.username = username;

        return this;
    }

    public OfficeHelper setPassword(String password) {
        this.password = password;
        return this;
    }

    public OfficeHelper setTsgCode(String tsgCode) {
        this.tsgCode = tsgCode;
        return this;
    }

    public boolean isLoggedIn() {
        String formData = "method=isLoggedIn";
        try {
            String result = postRequest(OFFICE_HELPER_URL, formData);
            String resultCode = result.replaceAll("[^\\x00-\\x7F]", "");

            return StringUtils.equals("1", resultCode);
        }
        catch (IOException|NumberFormatException e) {
            return false;
        }
    }

    public void submitCounter(CounterInfo.CounterEntry entry, int value) throws IOException {
        String formData = "method=saveNewVal&dataId="+ URLEncoder.encode(entry.getId(), "UTF-8")+"&newVal="+Integer.toString(value);
        String result = postRequest(OFFICE_HELPER_URL, formData);
    }
}
