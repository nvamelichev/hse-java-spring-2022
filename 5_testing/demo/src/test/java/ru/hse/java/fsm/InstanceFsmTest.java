package ru.hse.java.fsm;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class InstanceFsmTest {
    @Test
    public void example() {
        Instance i = Instance.create();
        assertThat(i.getState()).isEqualTo(InstanceState.STARTING);

        i = i.deployDone();
        assertThat(i.getState()).isEqualTo(InstanceState.STARTED_ACTUAL);

        i = i.delete();
        assertThat(i.getState()).isEqualTo(InstanceState.DELETING);

        i = i.deployDone();
        assertThat(i.getState()).isEqualTo(InstanceState.DELETED);

        assertThatIllegalStateException()
                .isThrownBy(i::deployDone);
        assertThatIllegalStateException()
                .isThrownBy(i::delete);
        assertThatIllegalStateException()
                .isThrownBy(i::stop);
        assertThatIllegalStateException()
                .isThrownBy(i::update);
    }
}
