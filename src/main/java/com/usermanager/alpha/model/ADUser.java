package com.usermanager.alpha.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public final class ADUser extends User {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    private String dn;

    @NotBlank(message = "Login cannot be empty")
    private String login;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    private String firstName;
    private String lastName;
    private List<String> groups;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password cannot be empty")
    private String password;


    public static ADUser fromEntry(Entry entry) {
        ADUser user = new ADUser();

        user.setId(getUuidFromEntry(entry));
        user.setDn(entry.getDN());
        user.setUsername(entry.getAttributeValue("cn"));
        user.setFirstName(entry.getAttributeValue("givenName"));
        user.setLastName(entry.getAttributeValue("sn"));
        user.setLogin(entry.getAttributeValue("sAMAccountName"));
        user.setGroups(entry.getAttributeValues("memberOf") == null ? null :
                List.of(entry.getAttributeValues("memberOf")));

        return user;
    }

    public Entry toEntry() {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("objectClass", "person", "organizationalPerson", "user", "top"));
        attributes.add(new Attribute("cn", this.username));
        if (this.firstName != null) {
            attributes.add(new Attribute("givenName", this.firstName));
        }
        if (this.lastName != null) {
            attributes.add(new Attribute("sn", this.lastName));
        }
        if (this.login != null) {
            attributes.add(new Attribute("sAMAccountName", this.login));
        }
        if (this.password != null) {
            attributes.add(new Attribute("unicodePwd", encodePassword(this.password)));
        }
        if (this.groups != null && !this.groups.isEmpty()) {
            attributes.add(new Attribute("memberOf", this.groups));
        }
        
        return new Entry(this.dn, attributes);
    }

    private String encodePassword(String password) {
        String quotedPassword = "\"" + password + "\"";
        return new String(quotedPassword.getBytes(java.nio.charset.StandardCharsets.UTF_16LE));
    }

    private static UUID getUuidFromEntry(Entry entry) {
        Attribute attribute = entry.getAttribute("objectGUID");
        if (attribute != null) {
            byte[] guidBytes = attribute.getValueByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(guidBytes);
            long high = buffer.getLong();
            long low = buffer.getLong();
            return new UUID(high, low);
        }
        return null;
    }

    @Override
    public String toString() {
        if (this.lastName == null) {
            return "ADUser " + this.firstName;
        }
        return "ADUser " + this.firstName + " " + this.lastName;
    }
}
