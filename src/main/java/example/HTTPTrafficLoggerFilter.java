package example;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * HTTP traffic logging filtter, based on  Doogie Request logging filter
 * (https://stackoverflow.com/a/42023374/365237)
 */
public class HTTPTrafficLoggerFilter extends OncePerRequestFilter {

    private int maxPayloadLength = 1000;

    private String getContentAsString(byte[] buf, int maxLength, String charsetName) {
        if (buf == null || buf.length == 0) return "";
        int length = Math.min(buf.length, this.maxPayloadLength);
        try {
            return new String(buf, 0, length, charsetName);
        } catch (UnsupportedEncodingException ex) {
            return "Unsupported Encoding";
        }
    }

    /**
     * Log each request and response with full Request URI, content payload and duration of the request in ms.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        StringBuffer reqInfo = new StringBuffer()
                .append("[")
                .append(startTime % 10000)  // request ID
                .append("] ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURL());

        String queryString = request.getQueryString();
        if (queryString != null) {
            reqInfo.append("?").append(queryString);
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String requestHeaders = this.headersAsString(wrappedRequest);

        filterChain.doFilter(wrappedRequest, wrappedResponse);

        // requestBody is not available for reading until after request has been sent forward
        // so we don't read it until here
        String requestBody = this.getContentAsString(wrappedRequest.getContentAsByteArray(),
                this.maxPayloadLength, request.getCharacterEncoding());

        this.logger.debug("=> " + reqInfo + "\n" + requestHeaders + (requestBody.length() > 0 ? "\n" + requestBody: ""));

        long duration = System.currentTimeMillis() - startTime;

        String responseHeaders = headersAsString(wrappedResponse);
        this.logger.debug("<= " + reqInfo + "\n" + responseHeaders + "\n" +
                getContentAsString(wrappedResponse.getContentAsByteArray(), this.maxPayloadLength, response.getCharacterEncoding()));
        
        this.logger.debug("<= " + reqInfo + ": returned status=" + response.getStatus() + " in "+duration + "ms");

        wrappedResponse.copyBodyToResponse();  // IMPORTANT: copy content of response back into original response

    }


    private static String headersAsString(HttpServletRequestWrapper wrappedRequest) {
        return headersAsString(() ->  Collections.list(wrappedRequest.getHeaderNames()),
                (headerName) -> Collections.list(wrappedRequest.getHeaders(headerName)));
    }
    private static String headersAsString(HttpServletResponseWrapper wrappedRequest) {
        return headersAsString(() -> new ArrayList(wrappedRequest.getHeaderNames()),
                (headerName) -> new ArrayList(wrappedRequest.getHeaders(headerName)));
    }

    private static String headersAsString(Supplier<List<String>> headerNameSupplier,
                                          Function<String, List<String>> headerValueSupplier) {
        StringBuilder response = new StringBuilder();
        for (String headerName : headerNameSupplier.get()) {
            for (String headerValue: headerValueSupplier.apply(headerName)) {
                response.append(headerName);
                response.append(": ");
                response.append(headerValue);
                response.append("\n");
            }
        }
        return response.toString();
    }

}