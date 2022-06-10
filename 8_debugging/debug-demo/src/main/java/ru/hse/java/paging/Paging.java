package ru.hse.java.paging;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import static java.util.stream.Collectors.toList;

public class Paging {
    /**
     * @param coll       collection
     * @param pageSize   page size > 0
     * @param pageNumber 1..N
     * @param <T>        page item type
     * @return page items as a list
     */
    @Nonnull
    public static <T> List<T> page(@Nonnull Collection<T> coll, int pageSize, int pageNumber) {
        Preconditions.checkArgument(pageSize > 0, "pageSize > 0");
        Preconditions.checkArgument(pageNumber >= 1, "pageNumber >= 1");

        return coll.stream()
                .skip(((long) (pageNumber - 1)) * ((long) pageSize))
                .limit(pageSize + 1)
                .collect(toList());
    }

    /**
     * @param set                sorted set
     * @param pageSize           page size > 0
     * @param lastBoundExclusive first element of the page, if listing 2nd and other pages
     * @param <T>                page item type
     * @return page items as a list
     */
    @Nonnull
    public static <T> List<T> page(@Nonnull SortedSet<T> set, int pageSize, @Nullable T lastBoundExclusive) {
        Preconditions.checkArgument(pageSize > 0, "pageSize > 0");
        if (lastBoundExclusive == null) {
            return set.stream().limit(pageSize + 1).collect(toList());
        } else {
            return set.tailSet(lastBoundExclusive).stream().limit(pageSize + 1).collect(toList());
        }
    }
}
