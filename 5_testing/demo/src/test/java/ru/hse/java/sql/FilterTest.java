package ru.hse.java.sql;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.hse.java.sql.Filter.eq;
import static ru.hse.java.sql.Filter.neq;

public class FilterTest {
    @Test
    public void leaf() {
        var filter = eq("$param1", null);
        assertThat(filter.toSql()).isEqualTo("$param1 IS NULL");
        assertThat(filter).isEqualTo(filter);
        assertThat(filter.negate().toSql()).isEqualTo("$param1 IS NOT NULL");
        assertThat(filter.negate().negate()).isEqualTo(filter);
    }

    @Test
    public void composite() {
        var filter = eq("$param1", "xyzzy")
                .and(neq("$param2", "uzhos").or(neq("$answer", 42L)));
        assertThat(filter.toSql()).isEqualTo("($param1=xyzzy) AND (($param2<>uzhos) OR ($answer<>42))");
        assertThat(filter.negate().toSql()).isEqualTo("NOT (($param1=xyzzy) AND (($param2<>uzhos) OR ($answer<>42)))");
        assertThat(filter.negate().negate()).isEqualTo(filter);
        assertThat(filter).isEqualTo(filter);
        assertThat(filter).isEqualTo(eq("$param1", "xyzzy").and(neq("$param2", "uzhos").or(neq("$answer", 42L))));
    }
}
