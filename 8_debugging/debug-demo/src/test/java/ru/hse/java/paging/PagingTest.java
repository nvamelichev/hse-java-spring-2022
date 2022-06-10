package ru.hse.java.paging;

import com.google.common.base.Stopwatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.stream.Collectors.toCollection;

public class PagingTest {
    private static SortedSet<Integer> ints;

    @BeforeClass
    public static void init() {
        ints = ThreadLocalRandom.current()
                .ints()
                .limit(100_000)
                .boxed()
                .collect(toCollection(TreeSet::new));
    }

    @AfterClass
    public static void tearDown() {
        ints = null;
    }

    @Test
    public void pageFunny() {
        Stopwatch sw = Stopwatch.createStarted();

        int pageSize = 10;
        int pageNumber = 1;
        List<Integer> page;
        do {
            page = Paging.page(ints, pageSize, pageNumber);
            page.subList(0, Math.max(0, page.size() - 1)).forEach(System.out::println);
            pageNumber++;
        } while (!page.isEmpty());

        System.err.println("elapsed: " + sw.stop());
    }

    @Test
    public void pageFunny2() {
        Stopwatch sw = Stopwatch.createStarted();

        int pageSize = 10;
        Integer last = null;
        List<Integer> page;
        do {
            page = Paging.page(ints, pageSize, last);
            last = page.get(page.size() - 1);
            page.subList(0, page.size() - 1).forEach(System.out::println);
        } while (page.size() >= pageSize);

        System.err.println("elapsed: " + sw.stop());
    }
}
