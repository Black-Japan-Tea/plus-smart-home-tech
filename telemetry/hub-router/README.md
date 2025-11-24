### Hub Router Mock Testing

#### Postman gRPC Request
1. Запустите сервис `hub-router` (`mvn -pl hub-router spring-boot:run`).
2. В Postman нажмите `New → gRPC Request`.
3. В поле **Server URL** укажите `grpc://localhost:59090`.
4. Во вкладке **Schema** импортируйте `telemetry/serialization/proto-schemas/src/main/protobuf/telemetry/all.proto`.
5. Выберите метод `telemetry.HubRouterController/handleDeviceAction`.
6. Пример сообщения:
```json
{
  "hub_id": "test-hub",
  "scenario_name": "turn_on_light",
  "action": {
    "sensor_id": "switch-1",
    "type": "ACTIVATE",
    "value": 1
  },
  "timestamp": {
    "seconds": 1732440000,
    "nanos": 0
  }
}
```
7. После отправки убедитесь, что ответ имеет статус `OK`. Полученные действия можно посмотреть по REST-эндоинту `GET http://localhost:8085/api/actions`.

#### gRPC UI (grpcui)
1. Благодаря включённой gRPC reflection (`grpc.server.reflection-service-enabled=true`) можно использовать `grpcui`.
2. Быстрый запуск через Docker:
```bash
docker run --rm -p 8086:8080 fullstorydev/grpcui \
  -plaintext host.docker.internal:59090
```
3. Откройте браузер `http://localhost:8086`, выберите метод `handleDeviceAction` и сформируйте запрос в web-форме. 
4. Историю команд также проверяйте через `GET http://localhost:8085/api/actions`.

