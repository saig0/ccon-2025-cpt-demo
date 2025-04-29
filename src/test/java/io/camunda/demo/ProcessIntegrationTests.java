package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.search.response.Variable;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = {"io.camunda.process.test.connectors-enabled=true"})
@TestPropertySource(locations = {"/connector-secrets.yml"})
@CamundaSpringProcessTest
class ProcessIntegrationTests {

  private static final String PROCESS_ID = "sign-up";

  private static final String USER_NAME = "Demo";
  private static final String EMAIL = "demo@camunda.com";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @DisplayName("Should create account and subscribe to newsletter")
  @Test
  void shouldCreateAccountWithSubscription() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, true);

    // mock backend invocation: new sign-up
    final var processInstance = createProcessInstance(signUpForm);

    // when
    assertThat(processInstance)
        .isActive()
        .hasActiveElements(byName("Await email activation"))
        .hasVariableNames("account");

    final Account account =
        getProcessInstanceVariable(
            processInstance.getProcessInstanceKey(), "account", Account.class);

    // mock backend invocation: email confirmed
    client
        .newPublishMessageCommand()
        .messageName("backend:email-confirmed")
        .correlationKey(account.id())
        .send()
        .join();

    // then
    assertThat(processInstance)
        .isCompleted()
        .hasCompletedElementsInOrder(
            byName("New sign-up"),
            byName("Create account"),
            byName("Send activation email"),
            byName("Send confirmation"),
            byName("Email confirmed"),
            byName("Activate account"),
            byName("Subscribe to newsletter"),
            byName("Account created"));
  }

  private ProcessInstanceEvent createProcessInstance(final SignUpForm signUpForm) {
    return client
        .newCreateInstanceCommand()
        .bpmnProcessId(PROCESS_ID)
        .latestVersion()
        .variable("signUpForm", signUpForm)
        .send()
        .join();
  }

  private <T> T getProcessInstanceVariable(
      final long processInstanceKey, final String variableName, final Class<T> variableClass) {

    final List<Variable> variables =
        client
            .newVariableSearchRequest()
            .filter(filter -> filter.processInstanceKey(processInstanceKey).name(variableName))
            .send()
            .join()
            .items();

    Assertions.assertThat(variables).isNotEmpty();

    final String variableValue = variables.getFirst().getValue();
    return client.getConfiguration().getJsonMapper().fromJson(variableValue, variableClass);
  }
}
