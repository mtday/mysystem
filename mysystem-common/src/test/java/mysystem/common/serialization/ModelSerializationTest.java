package mysystem.common.serialization;

import static org.junit.Assert.assertEquals;

import com.google.gson.JsonObject;

import org.junit.Test;

import mysystem.common.model.Company;
import mysystem.common.model.Model;

/**
 * Perform testing on the {@link ModelSerialization} class.
 */
public class ModelSerializationTest {
    @Test
    public void testIdentifier() {
        assertEquals(48151623, new ModelSerialization().identifier());
    }

    @Test
    public void testManifest() {
        final Company company = new Company.Builder().setName("a").build();
        assertEquals(Company.class.getSimpleName(), new ModelSerialization().manifest(company));
    }

    private final static class UnrecognizedModel implements Model {
        @Override
        public String getSerializationManifest() {
            return getClass().getSimpleName();
        }

        @Override
        public JsonObject toJson() {
            return new JsonObject();
        }
    }

    @Test(expected = RuntimeException.class)
    public void testManifestUnrecognizedModel() {
        final UnrecognizedModel model = new UnrecognizedModel();
        new ModelSerialization().manifest(model);
    }

    @Test(expected = RuntimeException.class)
    public void testManifestUnrecognized() {
        new ModelSerialization().manifest("unrecognized");
    }

    @Test
    public void testRoundTripCompany() {
        final Company company = new Company.Builder().setId(1).setName("Name").build();

        final ModelSerialization serialization = new ModelSerialization();
        final String manifest = serialization.manifest(company);
        final byte[] serialized = serialization.toBinary(company);
        final Object deserialized = serialization.fromBinary(serialized, manifest);

        assertEquals(company, deserialized);
    }

    @Test(expected = RuntimeException.class)
    public void testFromBinaryUnrecognized() {
        new ModelSerialization().fromBinary(new byte[0], "unrecognized");
    }

    @Test(expected = RuntimeException.class)
    public void testToBinaryUnrecognized() {
        new ModelSerialization().toBinary("unrecognized");
    }
}
