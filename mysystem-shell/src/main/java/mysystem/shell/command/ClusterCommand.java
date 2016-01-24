package mysystem.shell.command;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;

import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

/**
 * This actor implements the {@code cluster} commands in the shell.
 */
public class ClusterCommand extends UntypedActor {
    private final Cluster cluster;

    /**
     * Default constructor.
     */
    public ClusterCommand() {
        this.cluster = Cluster.get(context().system());
    }

    /**
     * @param cluster the {@link Cluster} from which cluster information will be retrieved
     */
    @VisibleForTesting
    protected ClusterCommand(final Cluster cluster) {
        this.cluster = Objects.requireNonNull(cluster);
    }

    /**
     * @return the {@link Cluster} instance used to track and manage cluster members
     */
    protected Cluster getCluster() {
        return this.cluster;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof RegistrationRequest) {
            handleRegistrationRequest();
        } else if (message instanceof Command) {
            handleCommand();
        } else {
            unhandled(message);
        }
    }

    protected void handleRegistrationRequest() {
        final String description = "provides information about the system cluster and its members";
        final CommandPath cluster = new CommandPath.Builder("cluster", "list").build();
        final Registration reg =
                new Registration.Builder().setActorPath(self()).setPath(cluster).setDescription(description).build();
        sender().tell(new RegistrationResponse.Builder().add(reg).build(), self());
    }

    protected void handleCommand() {
        getCurrentState().forEach(s -> sender().tell(new ConsoleOutput.Builder(s).setHasMore(true).build(), self()));
        sender().tell(new ConsoleOutput.Builder().build(), self());
    }

    protected List<String> getCurrentState() {
        final List<String> output = new LinkedList<>();
        final ClusterEvent.CurrentClusterState state = getCluster().state();

        if (state == null) {
            return output;
        }

        final Set<Member> members = new LinkedHashSet<>();
        state.getMembers().forEach(members::add);

        output.add(String.format("  Cluster Members: %d", members.size()));

        // Get the longest member field lengths for better formatting.
        final OptionalInt longestAddress = members.stream().mapToInt(m -> m.address().toString().length()).max();
        final OptionalInt longestStatus = members.stream().mapToInt(m -> m.status().toString().length()).max();

        for (final Member member : members) {
            final String address = StringUtils.rightPad(member.address().toString(), longestAddress.getAsInt());
            final String status = StringUtils.rightPad(member.status().toString(), longestStatus.getAsInt());
            final boolean isLeader = member.address().equals(state.getLeader());
            final String leader = StringUtils.rightPad(isLeader ? "leader" : "", 6);
            final String roles = String.join(" ", member.getRoles());
            output.add(String.format("    %s  %s  %s  %s", address, status, leader, roles));
        }

        return output;
    }
}
