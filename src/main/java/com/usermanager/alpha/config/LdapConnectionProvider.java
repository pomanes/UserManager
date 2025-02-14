package com.usermanager.alpha.config;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.HostNameSSLSocketVerifier;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLSocketFactory;

@Configuration
public class LdapConnectionProvider {

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.baseDN}")
    private String baseDn;

    @Value("${ldap.userDN}")
    private String userDn;

    @Value("${ldap.password}")
    private String password;

    @Value("${ldap.referral}")
    private String referralPolicy;

    private LDAPConnectionPool ldapConnectionPool;

    @Bean
    public LDAPConnectionPool getConnection() throws Exception {

        if (ldapConnectionPool == null || ldapConnectionPool.isClosed()) {
            try {
                LDAPConnectionOptions options = new LDAPConnectionOptions();
                options.setSSLSocketVerifier(new HostNameSSLSocketVerifier(true));
                options.setConnectTimeoutMillis(10000);
                options.setResponseTimeoutMillis(10000);

                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

                LDAPConnection connection = new LDAPConnection(sslSocketFactory, options, ldapUrl.split(":")[1].substring(2),
                        Integer.parseInt(ldapUrl.split(":")[2]));
                ldapConnectionPool = new LDAPConnectionPool(connection, 1, 5);
                bindConnection();
            } catch (Exception e) {
                System.err.println("Error while connect to LDAP: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        return ldapConnectionPool;
    }

    public void bindConnection() throws LDAPException {
        this.ldapConnectionPool.bind(userDn, password);
    }

    public LDAPConnection getLdapConnection() throws LDAPException {
        return ldapConnectionPool.getConnection();
    }

    public void releaseConnection(LDAPConnection connection) {
        ldapConnectionPool.releaseConnection(connection);
    }

    public void releaseDefunctConnection(LDAPConnection connection) {
        ldapConnectionPool.releaseDefunctConnection(connection);
    }

    @PreDestroy
    public void closeConnectionPool() {
        if (ldapConnectionPool != null && !ldapConnectionPool.isClosed()) {
            ldapConnectionPool.close();
        }
    }
}
