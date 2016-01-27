package mysystem.common.model;

/**
 * Defines the available roles for cluster nodes in the system.
 */
public enum SystemRole {
    /**
     * A core actor system component providing the standard system functionality.
     */
    SYSTEM,

    /**
     * A shell actor system connected to the cluster for console interactivity.
     */
    SHELL
}
