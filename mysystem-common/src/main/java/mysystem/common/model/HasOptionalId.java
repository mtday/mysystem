package mysystem.common.model;

import java.util.Optional;

/**
 * Defines the interface required for objects that optionally have unique identifiers.
 */
public interface HasOptionalId {
    /**
     * @return the unique identifier for the object, possibly empty
     */
    Optional<Integer> getId();
}
