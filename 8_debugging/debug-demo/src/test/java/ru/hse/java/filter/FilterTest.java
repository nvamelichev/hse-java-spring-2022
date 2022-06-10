package ru.hse.java.filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.hse.java.sql.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.hse.java.filter.Filter.field;
import static ru.hse.java.filter.Filter.isNotEqualTo;

public abstract class FilterTest {
    private Repository<Project> repository;

    @Before
    public void setUp() {
        repository = createRepository();
    }

    @After
    public void tearDown() {
        if (repository != null) {
            repository.close();
            repository = null;
        }
    }

    protected abstract Repository<Project> createRepository();

    @Test
    public void findAllWithLeadNotAnatoliy() {
        Person anatoliy = new Person(Person.Id.generate(), "Anatoliy", "UzhosUzhos");
        Person managerok = new Person(Person.Id.generate(), "Managerok", "Ivanov");
        Person sergey = new Person(Person.Id.generate(), "Sergey", "Petrov");

        Project anatoliy1 = new Project(Project.Id.generate(), managerok.id(), anatoliy.id());
        Project nonAnatoliy = new Project(Project.Id.generate(), managerok.id(), sergey.id());
        Project noLead = new Project(Project.Id.generate(), managerok.id(), null);

        var found = repository
                .add(anatoliy1)
                .add(nonAnatoliy)
                .add(noLead)
                .query(isNotEqualTo(field(Project.class, "leadId"), anatoliy.id()), 100);
        assertThat(found).containsOnly(nonAnatoliy, noLead);
    }

    public record Project(@Nonnull Project.Id id, @Nonnull Person.Id managerId, @Nullable Person.Id leadId) {
        public Project {
            requireNonNull(id, "id");
            requireNonNull(managerId, "managerId");
        }

        public record Id(String value) {
            public Id {
                requireNonNull(value, "value");
            }

            public static Id generate() {
                return new Id(UUID.randomUUID().toString());
            }

            @Override
            public String toString() {
                return value;
            }
        }
    }

    public record Person(@Nonnull Person.Id id, @Nonnull String firstName, @Nonnull String lastName) {
        public Person {
            requireNonNull(id, "id");
            requireNonNull(firstName, "firstName");
            requireNonNull(lastName, "lastName");
        }

        public record Id(@Nonnull String value) {
            public Id {
                requireNonNull(value, "value");
            }

            public static Id generate() {
                return new Id(UUID.randomUUID().toString());
            }

            @Override
            public String toString() {
                return value;
            }
        }
    }
}
