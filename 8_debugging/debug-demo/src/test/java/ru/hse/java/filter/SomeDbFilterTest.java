package ru.hse.java.filter;

import ru.hse.java.sql.Repository;
import ru.hse.java.sql.somedb.SomeDbRepository;

public class SomeDbFilterTest extends FilterTest {
    @Override
    protected Repository<Project> createRepository() {
        return new SomeDbRepository<>("projects");
    }
}
