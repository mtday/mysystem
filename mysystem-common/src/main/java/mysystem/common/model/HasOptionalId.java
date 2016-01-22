package mysystem.common.model;

import com.google.common.base.Optional;

/**
 * Defines the interface required for objects that optionally have unique identifiers.
 */
public interface HasOptionalId {
    /**
     * @return the unique identifier for the object, possibly empty
     */
    Optional<Integer> getId();
}
