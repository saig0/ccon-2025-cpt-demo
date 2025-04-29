# ccon-2025-cpt-demo

Demo application with Camunda Process Test (CPT) for CCon 2025.

## Scenario

![sign-up process](assets/Sign-up%20process%20(with%20pools).png)

## Content

- Spring Boot application: [Application.java](src/main/java/io/camunda/demo/Application.java)
    - Model: [model/](src/main/java/io/camunda/demo/model)
    - Services: [services/](src/main/java/io/camunda/demo/services)
    - Job workers: [workers/](src/main/java/io/camunda/demo/workers)
    - BPMN process: [Sign-up process.bpmn](src/main/resources/bpmn/Sign-up%20process.bpmn)
- Process tests with CPT:
    - With mock job
      worker: [ProcessWithMockWorkersTests.java](src/test/java/io/camunda/demo/ProcessWithMockWorkersTests.java)
    - With mock
      services: [ProcessWithMockServicesTests.java](src/test/java/io/camunda/demo/ProcessWithMockServicesTests.java)
    - With Connectors: [ProcessWithConnectorsTests.java](src/test/java/io/camunda/demo/ProcessWithConnectorsTests.java)

## Resources

- [Camunda 8 docs: Camunda Process Test](https://docs.camunda.io/docs/apis-tools/testing/getting-started/)
