package io.camunda.demo;

import io.camunda.client.CamundaClient;
import io.camunda.demo.model.SignUpForm;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;

@SpringBootTest(
    properties = {
      "camunda.client.worker.defaults.enabled=false" // disable all job workers
    })
@CamundaSpringProcessTest
class ProcessUnitTests {

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @Test
  void happyPath() {
    // given
    final var signUp = new SignUpForm( "Demo", "demo@camunda.com", true);

    final var processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId("sign-up")
            .latestVersion()
            .variable("signUp", signUp)
            .send()
            .join();

    // when

    // then
    assertThat(processInstance).isActive().hasActiveElements(byName("Create account"));
  }
}
