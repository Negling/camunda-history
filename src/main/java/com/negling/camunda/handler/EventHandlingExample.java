package com.negling.camunda.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Reads history events on application bootstrap and pass them to engine HistoryEventHandler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandlingExample extends SpringBootProcessEnginePlugin {
    @Qualifier("polymorphicObjectMapper")
    private final ObjectMapper objectMapper;

    /**
     * Emulates logic of history events being consumed one-by-one through message queue, rest call, etc.
     * After this method is completed you should observe missing history events on 'localhost:8082/engine-rest/engine/yesCamundaHistoryService/history/activity-instance'.
     * Mainly it's missing activity 'Event_ReviewerFourCompleted' but sometimes more. Behavior is random & unstable.
     */
    @Override
    @SneakyThrows
    public void postProcessEngineBuild(ProcessEngineImpl processEngine) {
        try (InputStream json = getClass().getClassLoader().getResourceAsStream("data/events.json")) {
            // read events from json one-by-one
            for (JsonNode node : objectMapper.readTree(json)) {
                var historyEvent = objectMapper.treeToValue(node, HistoryEvent.class);
                var processEngineConfiguration = processEngine.getProcessEngineConfiguration();

                processEngineConfiguration.getCommandExecutorTxRequired().execute(commandContext -> {
                    processEngineConfiguration.getHistoryEventHandler().handleEvent(historyEvent);
                    LOG.info("Processed history event: {}", historyEvent);

                    return null;
                });
                // simulate delay between events
                Thread.sleep((long) (Math.random() * 500 + 1));
            }
        } finally {
            LOG.info("Finished processing history events!");
        }
    }
}