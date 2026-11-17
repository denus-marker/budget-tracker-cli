package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    private static final LocalDate DATE = LocalDate.of(2024, 2, 29);

    private static Transaction sample() {
        return new Transaction("Salary", 1250.75, "Work", TransactionType.INCOME, DATE);
    }

    @Test
    void exposesEveryFieldAndReadableRepresentation() {
        Transaction t = sample();
        assertAll(
                () -> assertEquals("Salary", t.getDescription()),
                () -> assertEquals(1250.75, t.getAmount()),
                () -> assertEquals("Work", t.getCategory()),
                () -> assertEquals(TransactionType.INCOME, t.getType()),
                () -> assertEquals(DATE, t.getDate()),
                () -> assertEquals("2024-02-29 | INCOME | Work | 1250.75 | Salary", t.toString()));
    }

    @Test
    void equalValuesHaveConsistentHashCodesAndWorkInSets() {
        Transaction a = sample();
        Transaction b = sample();
        Transaction c = sample();
        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(b, c);
        assertEquals(a, c);
        assertEquals(Objects.hash("Salary", 1250.75, "Work", TransactionType.INCOME, DATE), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(1, new HashSet<>(List.of(a, b, c)).size());
    }

    static Stream<Object> unequalValues() {
        return Stream.of(
                "Salary", new Object(),
                new Transaction("Changed", 1250.75, "Work", TransactionType.INCOME, DATE),
                new Transaction("Salary", 1250.76, "Work", TransactionType.INCOME, DATE),
                new Transaction("Salary", 1250.75, "Other", TransactionType.INCOME, DATE),
                new Transaction("Salary", 1250.75, "Work", TransactionType.EXPENSE, DATE),
                new Transaction("Salary", 1250.75, "Work", TransactionType.INCOME, DATE.plusDays(1)),
                new Transaction(null, 1250.75, "Work", TransactionType.INCOME, DATE),
                new Transaction("Salary", 1250.75, null, TransactionType.INCOME, DATE),
                new Transaction("Salary", 1250.75, "Work", null, DATE),
                new Transaction("Salary", 1250.75, "Work", TransactionType.INCOME, null),
                new Transaction("Salary", 1250.75, "Work", TransactionType.INCOME, DATE) { });
    }

    @ParameterizedTest
    @MethodSource("unequalValues")
    void equalityRejectsDifferentFieldsAndClasses(Object other) {
        assertNotEquals(sample(), other);
        assertNotEquals(other, sample());
    }

    @Test
    void nullFieldsAreComparedAndRenderedWithoutThrowing() {
        Transaction a = new Transaction(null, 0, null, null, null);
        Transaction b = new Transaction(null, 0, null, null, null);
        assertNotEquals(null, a);
        assertFalse(a.equals(null));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals("null | null | null | 0.0 | null", a.toString());
        assertNull(a.getDescription());
        assertNull(a.getCategory());
        assertNull(a.getType());
        assertNull(a.getDate());
    }

    @Test
    void floatingPointEqualityUsesDoubleCompareSemantics() {
        assertNotEquals(new Transaction("x", 0.0, "x", null, DATE),
                new Transaction("x", -0.0, "x", null, DATE));
        assertEquals(new Transaction("x", Double.NaN, "x", null, DATE),
                new Transaction("x", Double.NaN, "x", null, DATE));
    }

    @Test
    void transactionTypesSupportNamesAndLookup() {
        assertArrayEquals(new TransactionType[]{TransactionType.INCOME, TransactionType.EXPENSE}, TransactionType.values());
        assertEquals(TransactionType.INCOME, TransactionType.valueOf("INCOME"));
        assertEquals(TransactionType.EXPENSE, TransactionType.valueOf("EXPENSE"));
        assertThrows(IllegalArgumentException.class, () -> TransactionType.valueOf("income"));
    }
}
