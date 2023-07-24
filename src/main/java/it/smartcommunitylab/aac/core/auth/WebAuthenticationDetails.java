package it.smartcommunitylab.aac.core.auth;

import it.smartcommunitylab.aac.SystemKeys;
import java.util.Calendar;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public class WebAuthenticationDetails extends org.springframework.security.web.authentication.WebAuthenticationDetails {

    private static final long serialVersionUID = SystemKeys.AAC_CORE_SERIAL_VERSION;

    private final long timestamp;
    private final String scheme;
    private final String protocol;
    private final Locale locale;
    private final String userAgent;

    // TODO evaluate new "client hints" as per google proposal
    // TODO evaluate fallback to spring mobile for device detect

    public WebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.timestamp = Calendar.getInstance().getTimeInMillis();
        this.scheme = request.getScheme();
        this.locale = request.getLocale();
        this.protocol = request.getProtocol();
        this.userAgent = request.getHeader(HttpHeaders.USER_AGENT);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getScheme() {
        return scheme;
    }

    public String getProtocol() {
        return protocol;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return (locale != null ? locale.getLanguage() : null);
    }

    public String getUserAgent() {
        return userAgent;
    }
}
