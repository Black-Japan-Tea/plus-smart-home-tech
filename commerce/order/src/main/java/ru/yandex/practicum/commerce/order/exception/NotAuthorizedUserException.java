package ru.yandex.practicum.commerce.order.exception;

public class NotAuthorizedUserException extends RuntimeException {

    public NotAuthorizedUserException() {
        super("Имя пользователя не должно быть пустым");
    }
}

