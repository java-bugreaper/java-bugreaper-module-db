package io.bugreaper.modules.db.mapper;

import io.bugreaper.modules.db.exceptions.QueryBuilderException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;

import static io.bugreaper.modules.db.mapper.JsonToArrayMapper.jsonToObjectArray;
import static org.junit.jupiter.api.Assertions.*;

class JsonToArrayMapperTests {

    @Test
    void testBuildObjectWithDifferentTypes() {
        String json = """
                {
                   "id": 1,
                   "name": "alex",
                   "isAdmin": true,
                   "amount": null,
                   "price": 10.71828,
                   "price2": 10.00,
                   "my_obj": {
                      "id2": 2,
                      "array_c": [1, 2, 3]
                   },
                   "array_d": ["one", "two"]
                }""";

        Object[][] result = jsonToObjectArray(json, true);


        assertEquals(
                "id",
                result[0][0]
        );

        assertEquals(
                1,
                result[0][1]
        );

        assertEquals(
                "alex",
                result[1][1]
        );
        assertEquals(
                true,
                result[2][1]
        );

        assertNull(result[3][1]);

        assertEquals(
                10.71828d,
                result[4][1]
        );
        assertEquals(
                10d,
                result[5][1]
        );

        assertEquals(
                """
                   {"id2":2,"array_c":[1,2,3]}""",
                result[6][1]
        );

        assertEquals(
                """
                   ["one","two"]""",
                result[7][1]
        );
    }

    @Test
        //for inserts
    void testBuildObjectWithTwoElements() {
        String json = """
                {"name": "alex"}""";

        Object[][] result = jsonToObjectArray(json, true);

        assertEquals(
                "name",
                result[0][0]
        );

        assertEquals(
                "alex",
                result[0][1]
        );

    }

    @Test
        //for inserts oracle
    void testBuildObjectWithThreeElements() {
        String json = """
                {"name:LIKE": "alex"}""";

        Object[][] result = jsonToObjectArray(json, true);

        assertEquals(
                "name",
                result[0][0]
        );

        assertEquals(
                "alex",
                result[0][1]
        );

        assertEquals(
                "LIKE",
                result[0][2]
        );
    }

    @Test
        //for asserts
    void testBuildObjectWithTwoElementsOtherOrder() {
        String json = """
                {"name": "alex"}""";

        Object[][] result = jsonToObjectArray(json, false);

        assertEquals(
                "name",
                result[0][0]
        );

        assertEquals(
                "alex",
                result[0][1]
        );
    }

    @Test
        //for asserts
    void testBuildObjectWithThreeElementsOtherOrder() {
        String json = """
                {"name:LIKE": "alex"}""";

        Object[][] result = jsonToObjectArray(json, false);

        assertEquals(
                "name",
                result[0][0]
        );

        assertEquals(
                "LIKE",
                result[0][1]
        );

        assertEquals(
                "alex",
                result[0][2]
        );
    }

    @Test
        //for asserts
    void testBuildObjectWithFourElementsOtherOrder() {
        String json = """
                {"name:LIKE:THREE": "alex"}""";

        Object[][] result = jsonToObjectArray(json, false);

        assertEquals(
                "name",
                result[0][0]
        );

        assertEquals(
                "LIKE",
                result[0][1]
        );

        assertEquals(
                "THREE",
                result[0][2]
        );

        assertEquals(
                "alex",
                result[0][3]
        );
    }

    @Test
    void testBuildObjectError() {
        String json = """
                [{
                   "id": 1,
                   "name": "alex"
                }]""";

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                jsonToObjectArray(json,true));

        MatcherAssert.assertThat(
                "JsonArray provided",
                exception.getMessage(),
                StringContains.containsString("Failed to convert JSON to Object[][]: JSON must be a flat object"));

    }

    @Test
    void testBuildTooManyOptionsError() {
        String json = """
                {
                   "id": 1,
                   "name:IS:LIKE:WRONG": "alex"
                }""";

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                jsonToObjectArray(json,true));

        MatcherAssert.assertThat(
                "Too many options provided",
                exception.getMessage(),
                StringContains.containsString("Failed to convert JSON to Object[][]: Maximum supported key format is FIELD:OPT1:OPT2, but got: name:IS:LIKE:WRONG"));

    }

}

