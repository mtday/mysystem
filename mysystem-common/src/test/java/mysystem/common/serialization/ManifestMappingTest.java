package mysystem.common.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import mysystem.common.model.Company;
import mysystem.common.model.ModelBuilder;

import java.util.Optional;

/**
 * Perform testing on the {@link ManifestMapping} class.
 */
public class ManifestMappingTest {
    @Test
    public void testGetManifest() {
        final Optional<String> manifest = new ManifestMapping().getManifest(Company.class);
        assertTrue(manifest.isPresent());
        assertEquals(Company.class.getSimpleName(), manifest.get());
    }

    @Test
    public void testGetManifestUnrecognized() {
        final Optional<String> manifest = new ManifestMapping().getManifest(String.class);
        assertFalse(manifest.isPresent());
    }

    @Test
    public void testGetBuilder() {
        final Optional<ModelBuilder<?>> builder = new ManifestMapping().getBuilder(Company.class.getSimpleName());
        assertTrue(builder.isPresent());
        assertEquals(Company.Builder.class, builder.get().getClass());
    }

    @Test
    public void testGetBuilderUnrecognized() {
        final Optional<ModelBuilder<?>> builder = new ManifestMapping().getBuilder("unrecognized");
        assertFalse(builder.isPresent());
    }
}
