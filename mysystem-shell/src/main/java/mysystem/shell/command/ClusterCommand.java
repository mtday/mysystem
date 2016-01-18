package mysystem.shell.command;

import org.apache.commons.lang3.StringUtils;

import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.model.ClusterMember;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

/**
 * This actor implements the {@code cluster} commands in the shell.
 */
public class ClusterCommand extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final Cluster cluster = Cluster.get(context().system());
    private final Set<ClusterMember> members = new TreeSet<>();

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
    public void preStart() {
        final List<Class<?>> classes = new LinkedList<>();
        classes.add(ClusterEvent.MemberEvent.class);
        classes.add(ClusterEvent.UnreachableMember.class);
        getCluster().subscribe(self(), ClusterEvent.initialStateAsEvents(), classes.toArray(new Class[0]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() {
        getCluster().unsubscribe(self());
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

    private void handleRegistrationRequest() {
        final String description = "provides information about the system cluster and its members";
        final CommandPath cluster = new CommandPath.Builder("cluster", "list").build();
        final Registration reg = new Registration.Builder(self(), cluster).setDescription(description).build();
        sender().tell(new RegistrationResponse.Builder().add(reg).build(), self());
    }

    protected void handleCommand() {
        getCurrentState().forEach(s -> sender().tell(new ConsoleOutput.Builder(s).setHasMore(true).build(), self()));
        sender().tell(new ConsoleOutput.Builder().build(), self());
    }

    protected List<String> getCurrentState() {
        final List<String> output = new LinkedList<>();
        final ClusterEvent.CurrentClusterState state = getCluster().state();

        final Set<Member> members = new LinkedHashSet<>();
        state.getMembers().forEach(m -> members.add(m));

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
