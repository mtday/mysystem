package mysystem.db.model;

/**
 * This interface is used on database requests to determine the type of data for which the request is intended.
 */
public interface HasDataType {
    /**
     * @return the {@link DataType} describing the type of data for which the request is intended
     */
    DataType getDataType();
}
