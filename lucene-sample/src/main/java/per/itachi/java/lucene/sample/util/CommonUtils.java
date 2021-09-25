package per.itachi.java.lucene.sample.util;

import org.springframework.util.CollectionUtils;
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
                paths = Arrays.asList(strPaths.substring(1).split("/"));
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

            // base uri
            String strBaseUri;
            if (url.getPort() <= 0) {
                strBaseUri = String.format("%s://%s", url.getProtocol(), url.getHost());
            }
            else {
                strBaseUri = String.format("%s://%s:%d", url.getProtocol(), url.getHost(), url.getPort());
            }

            // base relative uri
            String strBaseRelativeUri = strBaseUri;
            if (!CollectionUtils.isEmpty(paths) && paths.size() >= 2) {
                StringBuilder builder = new StringBuilder(strBaseRelativeUri);
                for(int i = 0, length = paths.size(); i < length - 1; ++i) {
                    builder.append("/").append(paths.get(i));
                }
                strBaseRelativeUri = builder.toString();
            }

            UrlInfo urlInfo = new UrlInfo();
            urlInfo.setHost(url.getHost());
            urlInfo.setBaseUri(strBaseUri);
            urlInfo.setBaseRelativeUri(strBaseRelativeUri);
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