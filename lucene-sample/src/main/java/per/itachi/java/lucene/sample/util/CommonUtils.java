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

    public static String generateHtmlFileName(UrlInfo urlInfo, List<String> params) {
        if (CollectionUtils.isEmpty(urlInfo.getPaths()) && CollectionUtils.isEmpty(urlInfo.getParams())) {
            return UUID.randomUUID().toString();
        }
        String strLastPath = urlInfo.getPaths().get(urlInfo.getPaths().size() - 1);
        int idx = strLastPath.indexOf(".");
        StringBuilder builder = new StringBuilder(100);
        // path part
        if (idx > 0) {
            builder.append(strLastPath.substring(0, idx));
        }
        else {
            builder.append(strLastPath);
        }
        // param path
        for (String param : params) {
            builder.append("-").append(param).append(urlInfo.getParams().get(param));
        }
        builder.append(".html");
        return builder.toString();
    }

    private CommonUtils() {}
}
