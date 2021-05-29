package me.realized.duels.util.validate;

public interface Validator<T> {

    boolean shouldValidate();

    boolean validate(final T validated);
}
