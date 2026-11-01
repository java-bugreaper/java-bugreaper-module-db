package net.bugreaper.modules.db;

import net.bugreaper.modules.db.allurereporter.AllureBuilder;
import net.bugreaper.modules.db.allurereporter.AllureReporter;
import net.bugreaper.modules.db.logger.Log;
import net.bugreaper.modules.db.mapper.JsonToArrayMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilityClassesTests {

    @Test
    void utilityJsonToArrayMapperTest() throws NoSuchMethodException {
        Constructor<JsonToArrayMapper> constructor = JsonToArrayMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = thrown.getCause();
        assert (cause instanceof IllegalStateException);
        assert ("Utility class".equals(cause.getMessage()));
    }

    @Test
    void utilityAllureBuilderTest() throws NoSuchMethodException {
        Constructor<AllureBuilder> constructor = AllureBuilder.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = thrown.getCause();
        assert (cause instanceof IllegalStateException);
        assert ("Utility class".equals(cause.getMessage()));
    }

    @Test
    void utilityAllureReporterTest() throws NoSuchMethodException {
        Constructor<AllureReporter> constructor = AllureReporter.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = thrown.getCause();
        assert (cause instanceof IllegalStateException);
        assert ("Utility class".equals(cause.getMessage()));
    }

    @Test
    void utilityLogTest() throws NoSuchMethodException {
        Constructor<Log> constructor = Log.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = thrown.getCause();
        assert (cause instanceof IllegalStateException);
        assert ("Utility class".equals(cause.getMessage()));
    }

}
