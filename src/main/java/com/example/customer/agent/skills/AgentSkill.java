package com.example.customer.agent.skills;

import java.util.function.Function;

/**
 * Интерфейс для всех инструментов (скиллов) агентов.
 * Позволяет динамически расширять возможности MAS.
 */
public interface AgentSkill<I, O> extends Function<I, O> {
    String getName();
    String getDescription();
    Class<I> getInputType();
}
