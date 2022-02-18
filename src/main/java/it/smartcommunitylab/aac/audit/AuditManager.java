package it.smartcommunitylab.aac.audit;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.audit.store.AuditEventStore;

@Service
@PreAuthorize("hasAuthority('" + Config.R_ADMIN + "')"
        + " or hasAuthority(#realm+':" + Config.R_ADMIN + "')")
public class AuditManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuditEventStore auditStore;

    public long countRealmEvents(String realm, String type, Date after, Date before) {
        Instant a = after == null ? null : after.toInstant();
        Instant b = before == null ? null : before.toInstant();

        logger.debug("count audit events for realm " + StringUtils.trimAllWhitespace(realm) + " type "
                + StringUtils.trimAllWhitespace(String.valueOf(type)) + " interval after "
                + String.valueOf(a) + " before " + String.valueOf(b));
        return auditStore.countByRealm(realm, a, b, type);
    }

    public long countPrincipalEvents(String realm, String principal, String type, Date after, Date before) {
        Instant a = after == null ? null : after.toInstant();
        Instant b = before == null ? null : before.toInstant();

        logger.debug(
                "count audit events for principal " + StringUtils.trimAllWhitespace(principal) + " type "
                        + StringUtils.trimAllWhitespace(String.valueOf(type)) + " interval after "
                        + String.valueOf(a) + " before " + String.valueOf(b));
        return auditStore.countByPrincipal(principal, a, b, type);
    }

    public List<RealmAuditEvent> findRealmEvents(String realm, String type, Date after, Date before) {
        Instant a = after == null ? null : after.toInstant();
        Instant b = before == null ? null : before.toInstant();

        logger.debug("find audit events for realm " + StringUtils.trimAllWhitespace(realm) + " type "
                + StringUtils.trimAllWhitespace(String.valueOf(type)) + " interval after "
                + String.valueOf(a) + " before " + String.valueOf(b));
        return auditStore.findByRealm(realm, a, b, type);
    }

    public List<AuditEvent> findPrincipalEvents(String realm, String principal, String type, Date after, Date before) {
        Instant a = after == null ? null : after.toInstant();
        Instant b = before == null ? null : before.toInstant();

        logger.debug(
                "find audit events for principal " + StringUtils.trimAllWhitespace(principal) + " type "
                        + StringUtils.trimAllWhitespace(String.valueOf(type)) + " interval after "
                        + String.valueOf(a) + " before " + String.valueOf(b));
        return auditStore.findByPrincipal(principal, a, b, type);
    }
}
