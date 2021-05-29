package me.realized.duels.util.validate;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;

public final class Validators {

    @SafeVarargs
    public static <T> ImmutableList<Validator<T>> constructValidators(final Validator<T>... validators) {
        final ImmutableList.Builder<Validator<T>> builder = ImmutableList.builder();

        for (final Validator<T> validator : validators) {
            if (validator.shouldValidate()) {
                builder.add(validator);
            }
        }

        return builder.build();
    }

    @SafeVarargs
    public static <T1, T2> ImmutableList<BiValidator<T1, T2>> constructBiValidators(final BiValidator<T1, T2>... biValidators) {
        final ImmutableList.Builder<BiValidator<T1, T2>> builder = ImmutableList.builder();

        for (final BiValidator<T1, T2> biValidator : biValidators) {
            if (biValidator.shouldValidate()) {
                builder.add(biValidator);
            }
        }

        return builder.build();
    }

    @SafeVarargs
    public static <T> boolean validate(final ImmutableList<Validator<T>> validators, final T validated, final Class<? extends Validator<T>>... excludes) {
        final List<Class<? extends Validator<T>>> excluded = Arrays.asList(excludes);
        return validators.stream().filter(validator -> !excluded.contains(validator.getClass())).allMatch(validator -> validator.validate(validated));
    }

    @SafeVarargs
    public static <T1, T2> boolean validate(final ImmutableList<BiValidator<T1, T2>> validators, final T1 first, final T2 second,
        final Class<? extends BiValidator<T1, T2>>... excludes) {
        final List<Class<? extends BiValidator<T1, T2>>> excluded = Arrays.asList(excludes);
        return validators.stream().filter(validator -> !excluded.contains(validator.getClass())).allMatch(validator -> validator.validate(first, second));
    }

    private Validators() {}
}
