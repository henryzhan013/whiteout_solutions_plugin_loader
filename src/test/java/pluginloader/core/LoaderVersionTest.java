package pluginloader.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoaderVersionTest {

    @Test
    void exactVersionMatch() {
        assertTrue(LoaderVersion.isCompatible("1.0.0"));
    }

    @Test
    void loaderNewerThanRequired() {
        // Loader is 1.0.0, plugin requires 0.9.0 - should work
        assertTrue(LoaderVersion.isCompatible("0.9.0"));
    }

    @Test
    void loaderOlderThanRequired() {
        // Loader is 1.0.0, plugin requires 2.0.0 - should fail
        assertFalse(LoaderVersion.isCompatible("2.0.0"));
    }

    @Test
    void handlesMissingPatchVersion() {
        assertTrue(LoaderVersion.isCompatible("1.0"));
    }

    @Test
    void handlesMissingMinorAndPatch() {
        assertTrue(LoaderVersion.isCompatible("1"));
    }

    @Test
    void nullRequirementIsCompatible() {
        assertTrue(LoaderVersion.isCompatible(null));
    }

    @Test
    void emptyRequirementIsCompatible() {
        assertTrue(LoaderVersion.isCompatible(""));
    }

    @Test
    void malformedVersionDoesNotCrash() {
        // Should not throw, just return some result
        LoaderVersion.isCompatible("not.a.version");
    }

    @Test
    void versionWithExtraParts() {
        assertTrue(LoaderVersion.isCompatible("1.0.0.0"));
    }
}
