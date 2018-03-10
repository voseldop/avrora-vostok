package info.podlesov.avroravostok;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import info.podlesov.avroravostok.networking.OfficeHelper;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class, URLConnection.class, HttpURLConnection.class, OfficeHelper.class})
public class OfficeHelperTest {

    URL loginUrl;
    HttpURLConnection connection;

    @Test
    public void checkLogin() throws Exception {
        loginUrl = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withArguments(Matchers.anyString()).thenReturn(loginUrl);
        connection = mock(HttpURLConnection.class);

        String status = "status";
        String loginCode = "0|0";
        when(loginUrl.openConnection()).thenReturn(connection);


        OfficeHelper helper = OfficeHelper.getInstance();

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        helper.init();


        when(connection.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(loginCode.getBytes()));
        helper.setUsername("user").setPassword("password").setTsgCode("tsgCode").login();
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(status.getBytes()));

        assert(status.equals(helper.status()));
    }
}