package uk.gov.justice.hmpps.probationteams.services;

import java.util.function.Supplier;

public class EntityNotFoundException extends RuntimeException implements Supplier<EntityNotFoundException> {
    private static final String DEFAULT_MESSAGE_FOR_ID_FORMAT = "Resource with id [%s] not found.";

    public EntityNotFoundException(final String message) {
        super(message);
    }

    public static EntityNotFoundException withId(final long id) {
        return withId(String.valueOf(id));
    }

    public static EntityNotFoundException withId(final String id) {
        return new EntityNotFoundException(String.format(DEFAULT_MESSAGE_FOR_ID_FORMAT, id));
    }

    public static EntityNotFoundException withMessage(final String message) {
        return new EntityNotFoundException(message);
    }

    public static EntityNotFoundException withMessage(final String message, final Object... args) {
        return new EntityNotFoundException(String.format(message, args));
    }

    @Override
    public EntityNotFoundException get() {
        return new EntityNotFoundException(getMessage());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
