package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.service.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Sort;

class SortDirectionTest {

    @ParameterizedTest(name = "\"{0}\" parses as ascending")
    @ValueSource(strings = {"ascending", "asc", "ASCENDING", "Asc", " Asc ", "\tascending\n"})
    @DisplayName("Ascending aliases parse to ASCENDING")
    void testAscendingAliases(String raw) {
        SortDirection direction = SortDirection.fromString(raw);

        assertEquals(SortDirection.ASCENDING, direction);
        assertEquals(Sort.Direction.ASC, direction.toSpringDirection());
        assertFalse(direction.isDescending());
    }

    @ParameterizedTest(name = "\"{0}\" parses as descending")
    @ValueSource(strings = {"descending", "desc", "DESC", "Descending", " Descending "})
    @DisplayName("Descending aliases parse to DESCENDING")
    void testDescendingAliases(String raw) {
        SortDirection direction = SortDirection.fromString(raw);

        assertEquals(SortDirection.DESCENDING, direction);
        assertEquals(Sort.Direction.DESC, direction.toSpringDirection());
        assertTrue(direction.isDescending());
    }

    @ParameterizedTest(name = "\"{0}\" is rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "sideways", "up", "down", "ascend", "descend", "a", "d", "asc desc"})
    @DisplayName("Missing or unknown directions throw 'Invalid sort direction'")
    void testInvalidDirections(String raw) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SortDirection.fromString(raw)
        );
        assertEquals("Invalid sort direction", exception.getMessage());
    }
}
