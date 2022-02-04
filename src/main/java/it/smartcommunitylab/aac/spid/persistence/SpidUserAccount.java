package it.smartcommunitylab.aac.spid.persistence;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.core.model.UserAccount;

@Entity
@IdClass(SpidUserAccountId.class)
@Table(name = "spid_users", uniqueConstraints = @UniqueConstraint(columnNames = { "realm", "provider_id", "user_id" }))
@EntityListeners(AuditingEntityListener.class)
public class SpidUserAccount implements UserAccount, Serializable {

    private static final long serialVersionUID = SystemKeys.AAC_SPID_SERIAL_VERSION;

    @Id
    @NotBlank
    @Column(name = "provider_id")
    private String provider;

    @Id
    @NotBlank
    private String realm;

    @Id
    @NotBlank
    @Column(name = "user_id")
    private String userId;

    @NotNull
    @Column(name = "subject_id")
    private String subject;

    @Column(name = "username")
    private String username;
    private String issuer;

    // attributes
    private String email;
    private String name;
    private String surname;

    // audit
    @CreatedDate
    @Column(name = "created_date")
    private Date createDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Date modifiedDate;

    @Override
    public String getType() {
        return SystemKeys.RESOURCE_ACCOUNT;
    }

    @Override
    public String getAuthority() {
        return SystemKeys.AUTHORITY_SPID;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public String getProvider() {
        return provider;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        userId = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmailAddress() {
        return email;
    }

    /*
     * fields
     */

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        // if email is set we trust it
        return StringUtils.hasText(email);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

}
