package ru.hse.util;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class RangeTest {
    @Test
    public void happy() {
        var range = new Range(10, 15);
        assertThat(range).containsExactly(10, 11, 12, 13, 14);
    }

    @Test
    public void validation() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Range(15, 10));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Range(0, -1));
    }

    @Test
    public void exact() {
        assertThat(new Range(Integer.MIN_VALUE, Integer.MAX_VALUE)
                .size()).isEqualTo((1L << 32) - 1);
    }

    @Test
    public void empty() {
        var emptyRange = new Range(0, 0);
        assertThat(emptyRange.size()).isZero();
        assertThat(emptyRange.isEmpty()).isTrue();
        assertThat(emptyRange).isEmpty();
    }

    @Test
    public void iterator() {
        var range = new Range(10, 10);
        var iter = range.iterator();

        assertThat(iter.hasNext()).isFalse();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(iter::next);
    }

    @Test
    public void contains_happy() {
        var range = new Range(10, 15);
        assertThat(range.contains(12)).isTrue();
        assertThat(range).contains(12);
    }

    @Test
    public void contains_happy2() {
        var range = new Range(10, 15);
        assertThat(range.contains(9)).isFalse();
        assertThat(range.contains(-1)).isFalse();
        assertThat(range.contains(100_500)).isFalse();
        assertThat(range.contains(Integer.MIN_VALUE)).isFalse();
        assertThat(range.contains(Integer.MAX_VALUE)).isFalse();
    }

    @Test
    public void contains_min_inclusive() {
        var range = new Range(10, 15);
        assertThat(range.contains(10)).isTrue();
    }

    @Test
    public void contains_max_exclusive() {
        var range = new Range(10, 15);
        assertThat(range.contains(15)).isFalse();
    }

    @Test
    public void contains_large() {
        var range = new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertThat(range.contains(Integer.MIN_VALUE)).isTrue();
        assertThat(range.contains(Integer.MAX_VALUE)).isFalse();
        assertThat(range.contains(Integer.MAX_VALUE - 1)).isTrue();
    }

    @Test
    public void contains_range_empty_1() {
        var range = new Range(10, 15);

        assertThat(range.contains(new Range(0, 0))).isTrue();
        assertThat(range.contains(new Range(10, 10))).isTrue();
        assertThat(range.contains(new Range(15, 15))).isTrue();
    }

    @Test
    public void contains_range_empty_2() {
        var range = new Range(10, 10);
        assertThat(range.contains(new Range(0, 0))).isTrue();
        assertThat(range.contains(new Range(10, 10))).isTrue();
        assertThat(range.contains(new Range(15, 15))).isTrue();

        assertThat(range.contains(new Range(10, 15))).isFalse();
        assertThat(range.contains(new Range(10, 11))).isFalse();
        assertThat(range.contains(new Range(9, 10))).isFalse();
        assertThat(range.contains(new Range(11, 12))).isFalse();
    }

    @Test
    public void contains_range_equals() {
        var range = new Range(10, 15);
        assertThat(range.contains(range)).isTrue();
    }

    @Test
    public void contains_range_subrange() {
        var range = new Range(10, 15);
        assertThat(range.contains(new Range(10, 11))).isTrue();
        assertThat(range.contains(new Range(14, 15))).isTrue();
        assertThat(range.contains(new Range(12, 14))).isTrue();
    }

    @Test
    public void contains_range_intersection_left() {
        var range = new Range(10, 15);
        assertThat(range.contains(new Range(9, 12))).isFalse();
    }

    @Test
    public void contains_range_intersection_right() {
        var range = new Range(10, 15);
        assertThat(range.contains(new Range(13, 16))).isFalse();
    }

    @Test
    public void contains_range_no_intersection_left() {
        var range = new Range(10, 15);
        assertThat(range.contains(new Range(8, 10))).isFalse();
    }

    @Test
    public void contains_range_no_intersection_right() {
        var range = new Range(10, 15);
        assertThat(range.contains(new Range(15, 18))).isFalse();
    }

    @Test
    public void range_equals() {
        // Check auto-generated equals(), for completeness :-)
        var range = new Range(10, 15);
        assertThat(range).isEqualTo(range);

        var rangeCopy = new Range(10, 15);
        assertThat(range).isEqualTo(rangeCopy);
        assertThat(rangeCopy).isEqualTo(range);

        var otherRange = new Range(5, 9);
        assertThat(range).isNotEqualTo(otherRange);
        assertThat(otherRange).isNotEqualTo(range);
    }
}
