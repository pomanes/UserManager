package com.usermanager.alpha.repositories;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.usermanager.alpha.config.LdapConnectionProvider;
import com.usermanager.alpha.model.ADUser;
import net.tirasa.adsddl.ntsd.SDDL;
import net.tirasa.adsddl.ntsd.utils.SDDLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Repository
public class ADUserRepository {

    private final LdapConnectionProvider ldapConnectionProvider;
    private final String baseDn;

    @Autowired
    public ADUserRepository(LdapConnectionProvider ldapConnectionProvider,
                            @Value("${ldap.baseDN}") String baseDn) {
        this.ldapConnectionProvider = ldapConnectionProvider;
        this.baseDn = baseDn;
    }

    public byte[] getBytesFromUuid(UUID id) {
        ByteBuffer bytes = ByteBuffer.wrap(new byte[16]);
        bytes.putLong(id.getMostSignificantBits());
        bytes.putLong(id.getLeastSignificantBits());
        return bytes.array();
    }

    private SearchRequest standardSearchRequest(Filter filter) throws LDAPException {
        return new SearchRequest(
                "OU=testdom" + "," + baseDn,
                SearchScope.SUB,
                filter,
                "objectGUID", "sAMAccountName", "givenName", "sn", "cn", "memberOf"
        );
    }

    private <T> T executeLdapOperation(Function<LDAPConnection, T> operation) throws LDAPException {
        LDAPConnection connection = null;
        try {
            connection = ldapConnectionProvider.getLdapConnection();
            ldapConnectionProvider.bindConnection();
            return operation.apply(connection);
        } catch (LDAPException e) {
            if (e.getResultCode() == ResultCode.CONNECT_ERROR ||
                    e.getResultCode() == ResultCode.SERVER_DOWN ||
                    e.getResultCode() == ResultCode.TIMEOUT) {
                ldapConnectionProvider.releaseDefunctConnection(connection);
                connection = null;
            } else {
                ldapConnectionProvider.releaseConnection(connection);
            }
            throw e;
        } finally {
            if (connection != null) {
                ldapConnectionProvider.releaseConnection(connection);
            }
        }
    }

    private int uACSetNormalAcc(int uAC) {
        return uAC |= 512;
    }

    private int uACSetDontExpirePass(int uAC) {
        return uAC |= 65536;
    }

    private void setCannotChangePass(String dn) throws LDAPException {
        executeLdapOperation(connection -> {
            try {
                Entry entry = connection.getEntry(dn, "nTSecurityDescriptor");
                byte[] nTSecurityDescriptor = entry.getAttributeValueBytes( "nTSecurityDescriptor" );

                SDDL sddl = new SDDL(nTSecurityDescriptor);
                nTSecurityDescriptor = SDDLHelper.userCannotChangePassword(sddl,true).toByteArray();

                Modification modification = new Modification(ModificationType.REPLACE, "nTSecurityDescriptor", nTSecurityDescriptor);
                connection.modify(dn, modification);
                return null;
            } catch (LDAPException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<ADUser> findAll() throws LDAPException {
        return executeLdapOperation(connection -> {
            try {
                Filter filter = Filter.create("(&(objectClass=user)(objectCategory=person))");
                SearchResult searchResult = connection.search(standardSearchRequest(filter));
                List<ADUser> users = new ArrayList<>();
                for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                    users.add(ADUser.fromEntry(entry));
                }
                return users;
            } catch (LDAPException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public ADUser findByUsername(String login) throws LDAPException {
        return executeLdapOperation(connection -> {
            try {
                Filter filter = Filter.create("(&(sAMAccountName=" + login + ")(objectClass=user)(objectCategory=person))");
                SearchResult searchResult = connection.search(standardSearchRequest(filter));
                if (searchResult.getEntryCount() > 0) {
                    return ADUser.fromEntry(searchResult.getSearchEntries().getFirst());
                } else {
                    return null;
                }
            } catch (LDAPException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public ADUser findByUuid(UUID id) throws LDAPException {
        return executeLdapOperation(connection -> {
            try {
                byte[] bytes = getBytesFromUuid(id);

                Filter filter = Filter.createEqualityFilter("objectGUID", bytes);
                SearchResult searchResult = connection.search(standardSearchRequest(filter));
                if (searchResult.getEntryCount() > 0) {
                    return ADUser.fromEntry(searchResult.getSearchEntries().getFirst());
                } else {
                    return null;
                }
            } catch (LDAPException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public ADUser create(ADUser user) throws LDAPException {
        return executeLdapOperation(connection -> {
            try {
                String userDn = "CN=" + user.getUsername() + ",OU=testdom" + "," + baseDn;
                user.setDn(userDn);
                Entry entryToAdd = user.toEntry();

                int uAC = 0;
                uAC = uACSetNormalAcc(uAC);
                uAC = uACSetDontExpirePass(uAC);
                entryToAdd.addAttribute(new Attribute("userAccountControl", String.valueOf(uAC)));
                AddRequest addRequest = new AddRequest(entryToAdd);

                connection.add(addRequest);

                ADUser createdUser = findByUsername(user.getLogin());
                setCannotChangePass(createdUser.getDn());
                return createdUser;
            } catch (LDAPException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
