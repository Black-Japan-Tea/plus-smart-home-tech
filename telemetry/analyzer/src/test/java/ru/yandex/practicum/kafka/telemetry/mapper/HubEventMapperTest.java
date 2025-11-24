package ru.yandex.practicum.kafka.telemetry.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.yandex.practicum.kafka.telemetry.entity.Action;
import ru.yandex.practicum.kafka.telemetry.entity.Condition;
import ru.yandex.practicum.kafka.telemetry.entity.Scenario;
import ru.yandex.practicum.kafka.telemetry.entity.Sensor;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.repository.ActionRepository;
import ru.yandex.practicum.kafka.telemetry.repository.ConditionRepository;
import ru.yandex.practicum.kafka.telemetry.repository.SensorRepository;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class HubEventMapperTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private ConditionRepository conditionRepository;

    @Mock
    private ActionRepository actionRepository;

    private HubEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new HubEventMapper(sensorRepository, conditionRepository, actionRepository);
    }

    @Test
    void shouldMapDeviceAddedEventToSensor() {
        DeviceAddedEventAvro event = DeviceAddedEventAvro.newBuilder()
            .setId("device-1")
            .setType(DeviceTypeAvro.TEMPERATURE_SENSOR)
            .build();

        when(sensorRepository.findById("device-1")).thenReturn(Optional.empty());

        Sensor sensor = mapper.toSensor("hub-1", event);

        assertThat(sensor.getId()).isEqualTo("device-1");
        assertThat(sensor.getHubId()).isEqualTo("hub-1");
    }

    @Test
    void shouldUpdateExistingSensor() {
        DeviceAddedEventAvro event = DeviceAddedEventAvro.newBuilder()
            .setId("device-1")
            .setType(DeviceTypeAvro.TEMPERATURE_SENSOR)
            .build();

        Sensor existing = new Sensor();
        existing.setId("device-1");
        existing.setHubId("hub-2");

        when(sensorRepository.findById("device-1")).thenReturn(Optional.of(existing));

        Sensor sensor = mapper.toSensor("hub-1", event);

        assertThat(sensor.getId()).isEqualTo("device-1");
        assertThat(sensor.getHubId()).isEqualTo("hub-1");
    }

    @Test
    void shouldMapScenarioAddedEventToScenario() {
        // Подготовка данных
        Sensor sensor1 = new Sensor();
        sensor1.setId("sensor-1");
        sensor1.setHubId("hub-1");

        Sensor sensor2 = new Sensor();
        sensor2.setId("sensor-2");
        sensor2.setHubId("hub-1");

        when(sensorRepository.findById("sensor-1")).thenReturn(Optional.of(sensor1));
        when(sensorRepository.findById("sensor-2")).thenReturn(Optional.of(sensor2));

        Condition existingCondition = new Condition();
        existingCondition.setType("TEMPERATURE");
        existingCondition.setOperation("GREATER_THAN");
        existingCondition.setValue(20);
        when(conditionRepository.findAll()).thenReturn(List.of(existingCondition));

        Action existingAction = new Action();
        existingAction.setType("ACTIVATE");
        existingAction.setValue(1);
        when(actionRepository.findAll()).thenReturn(List.of(existingAction));

        ScenarioAddedEventAvro event = ScenarioAddedEventAvro.newBuilder()
            .setName("test-scenario")
            .setConditions(List.of(
                ScenarioConditionAvro.newBuilder()
                    .setSensorId("sensor-1")
                    .setType(ConditionTypeAvro.TEMPERATURE)
                    .setOperation(ConditionOperationAvro.GREATER_THAN)
                    .setValue(20)
                    .build()
            ))
            .setActions(List.of(
                DeviceActionAvro.newBuilder()
                    .setSensorId("sensor-2")
                    .setType(ActionTypeAvro.ACTIVATE)
                    .setValue(1)
                    .build()
            ))
            .build();

        Scenario scenario = mapper.toScenario("hub-1", event);

        assertThat(scenario.getHubId()).isEqualTo("hub-1");
        assertThat(scenario.getName()).isEqualTo("test-scenario");
        assertThat(scenario.getConditions()).hasSize(1);
        assertThat(scenario.getConditions().get(0).getSensor().getId()).isEqualTo("sensor-1");
        assertThat(scenario.getConditions().get(0).getCondition().getType()).isEqualTo("TEMPERATURE");
        assertThat(scenario.getConditions().get(0).getCondition().getOperation()).isEqualTo("GREATER_THAN");
        assertThat(scenario.getConditions().get(0).getCondition().getValue()).isEqualTo(20);
        assertThat(scenario.getActions()).hasSize(1);
        assertThat(scenario.getActions().get(0).getSensor().getId()).isEqualTo("sensor-2");
        assertThat(scenario.getActions().get(0).getAction().getType()).isEqualTo("ACTIVATE");
        assertThat(scenario.getActions().get(0).getAction().getValue()).isEqualTo(1);
    }

    @Test
    void shouldCreateNewConditionIfNotExists() {
        Sensor sensor1 = new Sensor();
        sensor1.setId("sensor-1");
        sensor1.setHubId("hub-1");

        when(sensorRepository.findById("sensor-1")).thenReturn(Optional.of(sensor1));
        when(conditionRepository.findAll()).thenReturn(new ArrayList<>());

        Condition newCondition = new Condition();
        newCondition.setType("TEMPERATURE");
        newCondition.setOperation("GREATER_THAN");
        newCondition.setValue(20);
        when(conditionRepository.save(any(Condition.class))).thenReturn(newCondition);

        ScenarioAddedEventAvro event = ScenarioAddedEventAvro.newBuilder()
            .setName("test-scenario")
            .setConditions(List.of(
                ScenarioConditionAvro.newBuilder()
                    .setSensorId("sensor-1")
                    .setType(ConditionTypeAvro.TEMPERATURE)
                    .setOperation(ConditionOperationAvro.GREATER_THAN)
                    .setValue(20)
                    .build()
            ))
            .setActions(new ArrayList<>())
            .build();

        Scenario scenario = mapper.toScenario("hub-1", event);

        assertThat(scenario.getConditions()).hasSize(1);
    }

    @Test
    void shouldCreateNewActionIfNotExists() {
        Sensor sensor2 = new Sensor();
        sensor2.setId("sensor-2");
        sensor2.setHubId("hub-1");

        when(sensorRepository.findById("sensor-2")).thenReturn(Optional.of(sensor2));
        when(actionRepository.findAll()).thenReturn(new ArrayList<>());

        Action newAction = new Action();
        newAction.setType("ACTIVATE");
        newAction.setValue(1);
        when(actionRepository.save(any(Action.class))).thenReturn(newAction);

        ScenarioAddedEventAvro event = ScenarioAddedEventAvro.newBuilder()
            .setName("test-scenario")
            .setConditions(new ArrayList<>())
            .setActions(List.of(
                DeviceActionAvro.newBuilder()
                    .setSensorId("sensor-2")
                    .setType(ActionTypeAvro.ACTIVATE)
                    .setValue(1)
                    .build()
            ))
            .build();

        Scenario scenario = mapper.toScenario("hub-1", event);

        assertThat(scenario.getActions()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenSensorNotFound() {
        when(sensorRepository.findById("sensor-1")).thenReturn(Optional.empty());

        ScenarioAddedEventAvro event = ScenarioAddedEventAvro.newBuilder()
            .setName("test-scenario")
            .setConditions(List.of(
                ScenarioConditionAvro.newBuilder()
                    .setSensorId("sensor-1")
                    .setType(ConditionTypeAvro.TEMPERATURE)
                    .setOperation(ConditionOperationAvro.GREATER_THAN)
                    .setValue(20)
                    .build()
            ))
            .setActions(new ArrayList<>())
            .build();

        assertThatThrownBy(() -> mapper.toScenario("hub-1", event))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Sensor not found");
    }

    @Test
    void shouldMapBooleanConditionValue() {
        Sensor sensor1 = new Sensor();
        sensor1.setId("sensor-1");
        sensor1.setHubId("hub-1");

        when(sensorRepository.findById("sensor-1")).thenReturn(Optional.of(sensor1));
        when(conditionRepository.findAll()).thenReturn(new ArrayList<>());

        Condition newCondition = new Condition();
        newCondition.setType("SWITCH");
        newCondition.setOperation("EQUALS");
        newCondition.setValue(1); // true -> 1
        when(conditionRepository.save(any(Condition.class))).thenReturn(newCondition);

        ScenarioAddedEventAvro event = ScenarioAddedEventAvro.newBuilder()
            .setName("test-scenario")
            .setConditions(List.of(
                ScenarioConditionAvro.newBuilder()
                    .setSensorId("sensor-1")
                    .setType(ConditionTypeAvro.SWITCH)
                    .setOperation(ConditionOperationAvro.EQUALS)
                    .setValue(true) // boolean value
                    .build()
            ))
            .setActions(new ArrayList<>())
            .build();

        Scenario scenario = mapper.toScenario("hub-1", event);

        assertThat(scenario.getConditions()).hasSize(1);
        assertThat(scenario.getConditions().get(0).getCondition().getValue()).isEqualTo(1);
    }
}

