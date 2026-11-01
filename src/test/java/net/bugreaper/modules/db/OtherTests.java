package net.bugreaper.modules.db;


import net.bugreaper.core.config.YamlUtils;
import net.bugreaper.core.exceptions.ConfigException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OtherTests {

    protected static final String PROPERTY = "bugreaperEnv";
    private static String runtimeProperty;

    @BeforeAll
    static void getProperty() {
        runtimeProperty = System.getProperty(PROPERTY);
    }

    @AfterAll
    static void rollbackProperty() {
        YamlUtils.clearCache();
        if (runtimeProperty == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, runtimeProperty);
        }
    }

    @BeforeEach
    void clean() {
        YamlUtils.clearCache();
    }

    @Test
    void notSupportedHikariOptionTest() {
        System.setProperty(PROPERTY, "other");

        Throwable exception = assertThrows(ConfigException.class, () ->
                CustomDb.getInstance("-n1"));

        assertEquals("Unknown setup properties for 'modules.db.custom-db-n1.hikari': [someKey]",
                exception.getMessage());
    }

}
