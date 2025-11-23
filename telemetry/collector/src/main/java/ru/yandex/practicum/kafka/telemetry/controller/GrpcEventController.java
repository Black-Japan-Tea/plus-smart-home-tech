package ru.yandex.practicum.kafka.telemetry.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.CollectorControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.dto.HubEvent;
import ru.yandex.practicum.kafka.telemetry.dto.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.mapper.ProtobufMapper;
import ru.yandex.practicum.kafka.telemetry.service.CollectorService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcEventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final ProtobufMapper protobufMapper;
    private final CollectorService collectorService;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Received sensor event via gRPC: id={}, hubId={}, payloadCase={}", 
                    request.getId(), request.getHubId(), request.getPayloadCase());
            SensorEvent dto = protobufMapper.toDto(request);
            log.debug("Converted to DTO: type={}, id={}, hubId={}", 
                    dto.getClass().getSimpleName(), dto.getId(), dto.getHubId());
            collectorService.collectSensorEvent(dto);
            
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
            log.debug("Successfully processed sensor event: {}", request.getId());
        } catch (Exception e) {
            log.error("Error processing sensor event: id={}, hubId={}, payloadCase={}, error={}", 
                    request.getId(), request.getHubId(), request.getPayloadCase(), 
                    e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : ""), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage(), e.getCause());
            }
            responseObserver.onError(Status.INTERNAL
                    .withDescription(errorMessage)
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Received hub event via gRPC: {}", request.getHubId());
            HubEvent dto = protobufMapper.toDto(request);
            collectorService.collectHubEvent(dto);
            
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
            log.debug("Successfully processed hub event: {}", request.getHubId());
        } catch (Exception e) {
            log.error("Error processing hub event: {}", request.getHubId(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            responseObserver.onError(Status.INTERNAL
                    .withDescription(errorMessage)
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
