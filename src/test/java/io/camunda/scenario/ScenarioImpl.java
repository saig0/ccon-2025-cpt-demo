package io.camunda.scenario;

import io.camunda.client.CamundaClient;
import org.assertj.core.api.ThrowingConsumer;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScenarioImpl implements Scenario {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final CamundaClient camundaClient;

    public ScenarioImpl(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    @Override
    public ScenarioInstruction when(ThrowingConsumer<CamundaClient> condition) {
        return when(() -> condition.accept(camundaClient));
    }

    @Override
    public ScenarioInstruction when(ThrowingRunnable condition) {
        return new ScenarioInstructionImpl(executorService, condition);
    }

    private static class ScenarioInstructionImpl implements ScenarioInstruction {

        private final ExecutorService executorService;
        private final ThrowingRunnable condition;

        public ScenarioInstructionImpl(ExecutorService executorService, ThrowingRunnable condition) {
            this.executorService = executorService;
            this.condition = condition;
        }

        @Override
        public void then(ThrowingRunnable action) {
            executorService.execute(() -> {
                Awaitility.await("Wait until scenario condition is fulfilled")
                        .pollInSameThread()
                        .forever()
                        .untilAsserted(condition);

                try {
                    action.run();
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to execute scenario action", e);
                }
            });
        }
    }
}
