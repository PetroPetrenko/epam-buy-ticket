package com.example.customer.agent.skills;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class IntegrationSkill implements AgentSkill<IntegrationSkill.Input, IntegrationSkill.Output> {

    public static class Input {
        private final String platform;
        private final String action;
        private final Map<String, Object> params;

        public Input(String platform, String action, Map<String, Object> params) {
            this.platform = platform;
            this.action = action;
            this.params = params;
        }

        public String platform() { return platform; }
        public String action() { return action; }
        public Map<String, Object> params() { return params; }
    }

    public static class Output {
        private final String status;
        private final String externalId;
        private final String message;

        public Output(String status, String externalId, String message) {
            this.status = status;
            this.externalId = externalId;
            this.message = message;
        }

        public String status() { return status; }
        public String externalId() { return externalId; }
        public String message() { return message; }
    }

    @Override
    public String getName() {
        return "externalIntegration";
    }

    @Override
    public String getDescription() {
        return "Интеграция с внешними платформами: ComfyUI (генерация), n8n/Zapier (воркфлоу), Viewy (просмотр)";
    }

    @Override
    public Class<Input> getInputType() {
        return Input.class;
    }

    @Override
    public Output apply(Input input) {
        log.info("Executing integration with {}: action={}", input.platform(), input.action());
        
        // Имитация вызова внешних систем
        String externalId;
        String platform = input.platform().toLowerCase();
        if (platform.equals("comfyui")) {
            externalId = "workflow-" + System.currentTimeMillis();
        } else if (platform.equals("n8n") || platform.equals("zapier")) {
            externalId = "hook-" + System.currentTimeMillis();
        } else {
            externalId = "ext-" + System.currentTimeMillis();
        }

        return new Output(
            "SUCCESS",
            externalId,
            "Successfully triggered " + input.action() + " on " + input.platform()
        );
    }
}
