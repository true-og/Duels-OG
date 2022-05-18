package me.realized.duels.util.validate;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

public final class Validators {
    
    @SafeVarargs
    public static <T> ImmutableList<Validator<T>> buildChain(final Validator<T>... validators) {
        final ImmutableList.Builder<Validator<T>> builder = ImmutableList.builder();

        for (final Validator<T> validator : validators) {
            if (validator.shouldValidate()) {
                builder.add(validator);
            }
        }

        return builder.build();
    }

    @SafeVarargs
    public static <T1, T2> ImmutableList<BiValidator<T1, T2>> buildChain(final BiValidator<T1, T2>... biValidators) {
        final ImmutableList.Builder<BiValidator<T1, T2>> builder = ImmutableList.builder();

        for (final BiValidator<T1, T2> biValidator : biValidators) {
            if (biValidator.shouldValidate()) {
                builder.add(biValidator);
            }
        }

        return builder.build();
    }

    @SafeVarargs
    public static <T1, T2, T3> ImmutableList<TriValidator<T1, T2, T3>> buildChain(final TriValidator<T1, T2, T3>... triValidators) {
        final ImmutableList.Builder<TriValidator<T1, T2, T3>> builder = ImmutableList.builder();

        for (final TriValidator<T1, T2, T3> triValidator : triValidators) {
            if (triValidator.shouldValidate()) {
                builder.add(triValidator);
            }
        }

        return builder.build();
    }

    @SafeVarargs
    public static <T> boolean validate(final ImmutableList<Validator<T>> chain, final T validated, final Class<? extends Validator<T>>... excludes) {
        final List<Class<? extends Validator<T>>> excluded = Arrays.asList(excludes);
        return chain.stream().filter(validator -> !excluded.contains(validator.getClass())).allMatch(validator -> validator.validate(validated));
    }

    @SafeVarargs
    public static <T1, T2> boolean validate(final ImmutableList<BiValidator<T1, T2>> chain, final T1 first, final T2 second,
        final Class<? extends BiValidator<T1, T2>>... excludes) {
        final List<Class<? extends BiValidator<T1, T2>>> excluded = Arrays.asList(excludes);
        return chain.stream().filter(validator -> !excluded.contains(validator.getClass())).allMatch(validator -> validator.validate(first, second));
    }

    @SafeVarargs
    public static <T1, T2, T3> boolean validate(final ImmutableList<TriValidator<T1, T2, T3>> chain, final T1 first, final T2 second, final T3 third,
        final Class<? extends TriValidator<T1, T2, T3>>... excludes) {
        final List<Class<? extends TriValidator<T1, T2, T3>>> excluded = Arrays.asList(excludes);
        return chain.stream().filter(validator -> !excluded.contains(validator.getClass())).allMatch(validator -> validator.validate(first, second, third));
    }

    private Validators() {}
}
