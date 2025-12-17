package net.bugreaper.modules.db.allurereporter;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class AllureBuilderTest {


    @Test
    void utilClassTest() throws NoSuchMethodException {
        Constructor<AllureBuilder> constructor = AllureBuilder.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = thrown.getCause();
        assert (cause instanceof IllegalStateException);
        assert ("Utility class".equals(cause.getMessage()));
    }


}
