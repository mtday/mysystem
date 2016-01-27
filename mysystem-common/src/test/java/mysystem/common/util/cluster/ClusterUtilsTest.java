package mysystem.common.util.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.cluster.UniqueAddress;
import mysystem.common.model.SystemRole;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.immutable.HashSet;
import scala.collection.immutable.TreeSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Perform testing on the {@link ClusterUtils} class.
 */
public class ClusterUtilsTest {
    private Member getMember(final String host, final int port, final SystemRole role) {
        final Address address = new Address("akka.tcp", "mysystem", host, port);
        final UniqueAddress uniqueAddress = new UniqueAddress(address, 1);
        final Set<String> roles = Sets.newHashSet("0.0.0-SNAPSHOT", role.name());
        scala.collection.immutable.Set<String> immutableRoles = JavaConversions.asScalaSet(roles).toSet();
        return new Member(uniqueAddress, 1, MemberStatus.up(), immutableRoles);
    }

    private Cluster mockClusterState(final List<Member> members) {
        TreeSet<Member> memberSet = new TreeSet<>(Member.ordering());
        for (final Member member : members) {
            memberSet = memberSet.insert(member);
        }

        final HashSet<Member> unreachable = new HashSet<>();
        final HashSet<Address> seenBy = new HashSet<>();
        final Option<Address> leader = members.isEmpty() ? Option.empty() : Option.apply(members.get(0).address());

        final ClusterEvent.CurrentClusterState state =
                new ClusterEvent.CurrentClusterState(memberSet, unreachable, seenBy, leader, null);

        final Cluster cluster = Mockito.mock(Cluster.class);
        Mockito.when(cluster.state()).thenReturn(state);
        return cluster;
    }

    @Test
    public void testGetRandomNodeNoMembers() {
        final Cluster cluster = mockClusterState(Collections.emptyList());
        final Optional<Address> response = new ClusterUtils().getRandomNode(cluster, SystemRole.SYSTEM);
        assertFalse(response.isPresent());
    }

    @Test
    public void testGetRandomNodeOneMember() {
        final Member member = getMember("127.0.0.1", 2551, SystemRole.SYSTEM);
        final Cluster cluster = mockClusterState(Arrays.asList(member));

        final Optional<Address> response = new ClusterUtils().getRandomNode(cluster, SystemRole.SYSTEM);
        assertTrue(response.isPresent());
        assertEquals("akka.tcp://mysystem@127.0.0.1:2551", response.get().toString());
    }

    @Test
    public void testGetRandomNodeMultipleMembersNoSystem() {
        final Member member1 = getMember("127.0.0.1", 2551, SystemRole.SHELL);
        final Member member2 = getMember("127.0.0.2", 2551, SystemRole.SHELL);
        final Member member3 = getMember("127.0.0.3", 2551, SystemRole.SHELL);
        final Cluster cluster = mockClusterState(Arrays.asList(member1, member2, member3));

        final Optional<Address> response = new ClusterUtils().getRandomNode(cluster, SystemRole.SYSTEM);
        assertFalse(response.isPresent());
    }

    @Test
    public void testGetRandomNodeMultipleMembersOneSystem() {
        final Member member1 = getMember("127.0.0.1", 2551, SystemRole.SHELL);
        final Member member2 = getMember("127.0.0.2", 2551, SystemRole.SYSTEM);
        final Member member3 = getMember("127.0.0.3", 2551, SystemRole.SHELL);
        final Cluster cluster = mockClusterState(Arrays.asList(member1, member2, member3));

        final Optional<Address> response = new ClusterUtils().getRandomNode(cluster, SystemRole.SYSTEM);
        assertTrue(response.isPresent());
        assertEquals("akka.tcp://mysystem@127.0.0.2:2551", response.get().toString());
    }
}
