package com.usermanager.alpha.services;

import com.usermanager.alpha.model.ADUser;
import com.usermanager.alpha.repositories.ADUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class ADUserService {

    private final ADUserRepository ADuserRepository;
    private final LdapTemplate ldapTemplate;

    @Autowired
    public ADUserService(ADUserRepository ADuserRepository, LdapTemplate ldapTemplate) {
        this.ADuserRepository = ADuserRepository;
        this.ldapTemplate = ldapTemplate;
    }

    private byte[] encodePassword(String password) {
        String quotedPassword = "\"" + password + "\"";
        return quotedPassword.getBytes(StandardCharsets.UTF_16LE);
    }

    public Optional<List<ADUser>> findAll() {
        return Optional.of(ADuserRepository.findAll(LdapQueryBuilder.query()
                .base("ou=testdom").where("objectClass").is("user")));
    }

/*    public ADUser addUser(ADUser user) {
        ADUser savedUser = ADuserRepository.save(user);
        setUserPassword(user.getUsername(), user.getPassword());
        return ADuserRepository.save(savedUser);
    }*/

    public void addUser(ADUser user) {
        Name dn = LdapNameBuilder.newInstance("OU=testdom")
                .add("cn", user.getUsername())
                .build();
        DirContextAdapter context = new DirContextAdapter(dn);
        context.setAttributeValues(
                "objectclass",
                new String[]{"top", "person", "organizationalPerson", "user"});
        context.setAttributeValue("cn", user.getUsername());
        context.setAttributeValue("givenName", user.getFirstName());
        context.setAttributeValue("sn", user.getLastName());
        context.setAttributeValue("sAMAccountName", user.getLogin());
        context.setAttributeValue("unicodePwd", encodePassword(user.getPassword()));

        int userAccountControl = 0;
        userAccountControl |= 0x0200;  // NORMAL_ACCOUNT
        userAccountControl |= 0x10000; // DONT_EXPIRE_PASSWORD


        context.setAttributeValue("userAccountControl", String.valueOf(userAccountControl));

        ldapTemplate.bind(context);
    }
}