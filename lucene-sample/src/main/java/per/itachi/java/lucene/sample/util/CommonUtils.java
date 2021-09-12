package per.itachi.java.lucene.sample.util;

import org.springframework.util.StringUtils;
import per.itachi.java.lucene.sample.entity.html.UrlInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class CommonUtils {

    public static UrlInfo generateUrlInfo(String addressUrl) {
        try {
            URL url = new URL(addressUrl);
            // file name
            String strPaths = url.getPath();
            String strFileName = null;
            if (StringUtils.hasText(strPaths)) {
                strFileName = strPaths.substring(strPaths.lastIndexOf("/") + 1);
            }
            // paths
            List<String> paths = Collections.emptyList();
            if (StringUtils.hasText(strPaths) && strPaths.length() > 1) {
                paths = Arrays.asList(strPaths.split("/"));
            }
            // query
            String strQuery = url.getQuery();
            Map<String, String> mapParams = Collections.emptyMap();
            if (StringUtils.hasText(strQuery)) {
                mapParams = new HashMap<>();
                String[] arrParams = strQuery.split("&");
                for (String param : arrParams) {
                    int idx = param.indexOf("=");
                    mapParams.put(param.substring(0, idx), param.substring(idx + 1));
                }
            }
            UrlInfo urlInfo = new UrlInfo();
            urlInfo.setHost(url.getHost());
            urlInfo.setBaseUri(url.getPort() <= 0 ? String.format("%s://%s", url.getProtocol(), url.getHost())
                    : String.format("%s://%s:%d", url.getProtocol(), url.getHost(), url.getPort()));
            urlInfo.setPaths(paths);
            urlInfo.setFileName(strFileName);
            urlInfo.setParams(mapParams);
            return urlInfo;
        }
        catch (MalformedURLException e) {
            return new UrlInfo();
        }
    }

    private CommonUtils() {}
}
