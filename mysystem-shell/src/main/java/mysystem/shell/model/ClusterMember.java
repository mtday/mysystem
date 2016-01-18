package mysystem.shell.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import akka.cluster.Member;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable class representing a member of the cluster.
 */
public class ClusterMember implements Comparable<ClusterMember>, Serializable {
    private final static long serialVersionUID = 1L;

    private final String host;
    private final int port;
    private final String protocol;
    private final String status;
    private final SortedSet<String> roles;

    private ClusterMember(final String host, final int port, final String protocol, final Collection<String> roles, final String status) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.status = status;
        this.roles = new TreeSet<>(roles);
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getStatus() {
        return this.status;
    }

    public SortedSet<String> getRoles() {
        return Collections.unmodifiableSortedSet(this.roles);
    }

    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("host", getHost());
        str.append("port", getPort());
        str.append("protocol", getProtocol());
        str.append("status", getStatus());
        str.append("roles", getRoles());
        return str.build();
    }

    @Override
    public int compareTo(final ClusterMember other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getHost(), other.getHost());
        cmp.append(getPort(), other.getPort());
        cmp.append(getProtocol(), other.getProtocol());
        cmp.append(getStatus(), other.getStatus());

        final Iterator<String> rolesA = getRoles().iterator();
        final Iterator<String> rolesB = other.getRoles().iterator();
        while (rolesA.hasNext() && rolesB.hasNext() && cmp.toComparison() == 0) {
            cmp.append(rolesA.next(), rolesB.next());
        }
        if (cmp.toComparison() == 0) {
            if (rolesA.hasNext()) {
                return 1;
            } else if (rolesB.hasNext()) {
                return -1;
            }
        }

        return cmp.toComparison();
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof ClusterMember) && compareTo((ClusterMember) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getHost());
        hash.append(getPort());
        hash.append(getProtocol());
        hash.append(getStatus());
        hash.append(getRoles());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link ClusterMember} instances.
     */
    public static class Builder {
        private final String host;
        private final int port;
        private final String protocol;
        private final Set<String> roles;
        private final String status;

        public Builder(final Member member) {
            this.host = member.address().host().get();
            this.port = Integer.parseInt(String.valueOf(member.address().port().get()));
            this.protocol = member.address().protocol();
            this.roles = new TreeSet<>(member.getRoles());
            this.status = member.status().toString();
        }

        public ClusterMember build() {
            return new ClusterMember(this.host, this.port, this.protocol, this.roles, this.status);
        }
    }
}
