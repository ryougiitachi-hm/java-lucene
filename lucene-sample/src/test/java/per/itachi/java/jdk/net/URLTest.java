package per.itachi.java.jdk.net;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(MockitoJUnitRunner.class)
public class URLTest {

    @Test
    public void testURL() throws MalformedURLException {
        URL url = new URL("ftp://jkkjk.jjl.com?ui=1&ui=1&ai=1&");
        System.out.printf("url=%s, protocol=%s, host=%s, port=%s, path=%s, query=%s, defaultPort=%d, ref=%s %n",
                url, url.getProtocol(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getDefaultPort(), url.getRef());
        float f = 978.95f;
        double d = 978.95;
        System.out.printf("float=%f, double=%f %n", f, d);System.out.println(f);System.out.println(d);
    }
}
