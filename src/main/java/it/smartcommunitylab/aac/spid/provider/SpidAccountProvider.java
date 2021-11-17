package it.smartcommunitylab.aac.spid.provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.common.NoSuchUserException;
import it.smartcommunitylab.aac.core.base.AbstractProvider;
import it.smartcommunitylab.aac.core.provider.AccountProvider;
import it.smartcommunitylab.aac.spid.persistence.SpidUserAccount;
import it.smartcommunitylab.aac.spid.persistence.SpidUserAccountRepository;

@Transactional
public class SpidAccountProvider extends AbstractProvider implements AccountProvider {

    private final SpidUserAccountRepository accountRepository;
    private final SpidIdentityProviderConfig providerConfig;

    protected SpidAccountProvider(String providerId, SpidUserAccountRepository accountRepository,
            SpidIdentityProviderConfig config,
            String realm) {
        super(SystemKeys.AUTHORITY_SPID, providerId, realm);
        Assert.notNull(accountRepository, "account repository is mandatory");
        Assert.notNull(config, "provider config is mandatory");

        this.providerConfig = config;
        this.accountRepository = accountRepository;
    }

    @Override
    public String getType() {
        return SystemKeys.RESOURCE_ACCOUNT;
    }

    @Transactional(readOnly = true)
    public SpidUserAccount getAccount(String userId) throws NoSuchUserException {
        String id = parseResourceId(userId);
        String realm = getRealm();
        String provider = getProvider();

        SpidUserAccount account = accountRepository.findByRealmAndProviderAndUserId(realm, provider, id);
        if (account == null) {
            throw new NoSuchUserException(
                    "Spid user with userId " + id + " does not exist for realm " + realm);
        }

        // detach the entity, we don't want modifications to be persisted via a
        // read-only interface
        // for example eraseCredentials will reset the password in db
        account = accountRepository.detach(account);

        // rewrite internal userId
        account.setUserId(exportInternalId(id));

        return account;
    }

    @Override
    @Transactional(readOnly = true)
    public SpidUserAccount getByIdentifyingAttributes(Map<String, String> attributes) throws NoSuchUserException {
        String realm = getRealm();
        String provider = getProvider();

        // check if passed map contains at least one valid set and fetch account
        // TODO rewrite less hardcoded
        // note AVOID reflection, we want native image support
        SpidUserAccount account = null;
        if (attributes.containsKey("userId")) {
            String userId = parseResourceId(attributes.get("userId"));
            account = accountRepository.findByRealmAndProviderAndUserId(realm, provider, userId);
        }

        if (account == null
                && attributes.keySet().containsAll(Arrays.asList("realm", "provider", "userId"))
                && realm.equals(attributes.get("realm"))
                && provider.equals(attributes.get("provider"))) {
            String userId = parseResourceId(attributes.get("userId"));
            account = accountRepository.findByRealmAndProviderAndUserId(realm, provider, userId);
        }

        // TODO add find by usernameAttribute as set in providerConfig

        if (account == null) {
            throw new NoSuchUserException("No user found matching attributes");
        }

        // detach the entity, we don't want modifications to be persisted via a
        // read-only interface
        // for example eraseCredentials will reset the password in db
        account = accountRepository.detach(account);

        // rewrite internal userId
        account.setUserId(exportInternalId(account.getUserId()));

        return account;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<SpidUserAccount> listAccounts(String subject) {
        List<SpidUserAccount> accounts = accountRepository.findBySubjectAndRealmAndProvider(subject, getRealm(),
                getProvider());

        // we need to fix ids and detach
        return accounts.stream().map(a -> {
            a = accountRepository.detach(a);
            a.setUserId(exportInternalId(a.getUserId()));
            return a;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteAccount(String userId) throws NoSuchUserException {
        String id = parseResourceId(userId);
        String realm = getRealm();
        String provider = getProvider();

        SpidUserAccount account = accountRepository.findByRealmAndProviderAndUserId(realm, provider, id);

        if (account != null) {
            // remove account
            accountRepository.delete(account);
        }
    }

}
