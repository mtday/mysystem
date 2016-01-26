package mysystem.common.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import mysystem.common.model.Company;
import mysystem.common.model.Model;
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

    @Test
    public void testAsTripleNoManifest() {
        final Optional<Triple<String, Class<? extends Model>, Class<? extends ModelBuilder>>> opt =
                new ManifestMapping().asTriple(Pair.of(SimpleModel.class, SimpleModel.NoDefaultConstructorBuilder.class));
        assertFalse(opt.isPresent());
    }

    @Test
    public void testGetSerializationManifestBuilderNoDefaultConstructor() {
        final Optional<String> opt = new ManifestMapping().getSerializationManifest(
                Pair.of(SimpleModel.class, SimpleModel.NoDefaultConstructorBuilder.class));
        assertFalse(opt.isPresent());
    }

    @Test
    public void testGetBuilderInstantiationException() {
        final ManifestMapping mapping = new ManifestMapping();
        mapping.putTriple(Triple.of("manifest", SimpleModel.class, SimpleModel.NoDefaultConstructorBuilder.class));
        final Optional<ModelBuilder<? extends Model>> opt = mapping.getBuilder("manifest");
        assertFalse(opt.isPresent());
    }

    public static class SimpleModel implements Model {
        @Override
        public String getSerializationManifest() {
            return getClass().getSimpleName();
        }

        @Override
        public JsonObject toJson() {
            return new JsonObject();
        }

        public static class NoDefaultConstructorBuilder implements ModelBuilder<SimpleModel> {
            private final String manifest;

            public NoDefaultConstructorBuilder(final String manifest) {
                this.manifest = manifest;
            }

            @Override
            public String getSerializationManifest() {
                return manifest;
            }

            @Override
            public ModelBuilder<SimpleModel> fromJson(final ManifestMapping mapping, final JsonObject json) {
                return this;
            }

            @Override
            public SimpleModel build() {
                return new SimpleModel();
            }
        }
    }
}
