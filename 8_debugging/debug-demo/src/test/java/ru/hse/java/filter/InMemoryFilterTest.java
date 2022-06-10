package ru.hse.java.filter;

import ru.hse.java.sql.Repository;
import ru.hse.java.sql.fake.FakeRepository;

public class InMemoryFilterTest extends FilterTest {
    @Override
    protected Repository<Project> createRepository() {
        return new FakeRepository<>();
    }
}
