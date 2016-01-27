package mysystem.common.util.cluster;

import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.Member;
import mysystem.common.model.SystemRole;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Performs operations.
 */
public class ClusterUtils {
    /**
     * Retrieve a random {@link Address} from within the provided cluster that has the specified role.
     *
     * @param cluster the {@link Cluster} from which the member address will be retrieved
     * @param role the {@link SystemRole} to find in the cluster
     * @return a random {@link Address} from the identified nodes in the cluster with the specified role, possibly
     * empty if a node with the specified role was not available in the cluster
     */
    public Optional<Address> getRandomNode(final Cluster cluster, final SystemRole role) {
        Objects.requireNonNull(cluster);
        Objects.requireNonNull(role);

        final List<Member> members = new ArrayList<>();
        JavaConversions.asJavaCollection(cluster.state().members()).forEach(members::add);
        final List<Member> withRole = members.stream().filter(m -> m.hasRole(role.name())).collect(Collectors.toList());
        if (withRole.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(withRole.get(new Random().nextInt(withRole.size())).address());
    }
}
