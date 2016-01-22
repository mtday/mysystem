package mysystem.common.model;

/**
 * Defines the interface required for objects that may or may not be active in the system.
 */
public interface HasActive {
    /**
     * @return whether the object is currently in an active state or not
     */
    boolean isActive();
}
