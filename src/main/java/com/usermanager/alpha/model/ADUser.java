package com.usermanager.alpha.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entry(base = "ou=testdom", objectClasses = {"person", "organizationalPerson", "user", "top"})
public final class ADUser extends User {

    @Id
    @JsonIgnore
    private Name id;

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 0)
    private String username;

    @Attribute(name = "ou")
    private String ou;

    @Attribute(name = "givenName")
    private String firstName;

    @Attribute(name = "sn")
    private String lastName;

    @Attribute(name = "sAMAccountName")
    private String login;

    @Attribute(name = "userPassword")
    private String password;

    @Attribute(name = "memberOf")
    private List<String> groups;

/*
    public ADUser(String username, String ou, String firstName, String lastName, String login, String password) {
        this.username = username;
        this.ou = ou;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.password = password;
    }

    public ADUser(String username, String ou, String firstName, String lastName, String login, String password, List<String> groups) {
        this.username = username;
        this.ou = ou;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.password = password;
        this.groups = groups;
    }
*/

    @Override
    public String toString() {
        if (this.lastName == null) {
            return "ADUser " + this.firstName;
        }
        return "ADUser " + this.firstName + " " + this.lastName;
    }
}
