package com.rmjtromp.chatemojis.utils;

import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantValue")
@UtilityClass
public class ReflectionUtils {

    public static Field searchField(Object o, Function<FieldSearchData.FieldSearchDataBuilder, FieldSearchData> query) throws NoSuchFieldException {
        return searchFields(o, query, 1).iterator().next();
    }

    public static Set<Field> searchFields(Object o, Function<FieldSearchData.FieldSearchDataBuilder, FieldSearchData> query) throws NoSuchFieldException {
        return searchFields(o, query, -1);
    }

    private static Set<Field> searchFields(Object o, Function<FieldSearchData.FieldSearchDataBuilder, FieldSearchData> query, int limit) throws NoSuchFieldException {
        FieldSearchData data = query.apply(FieldSearchData.builder()
            .target(o instanceof Class<?>
                ? (Class<?>) o
                : o.getClass()
            ));

        if(data.type == null && data.names.isEmpty() && data.filter == null)
            throw new IllegalArgumentException("type, names, and filter cannot both be empty");

        Set<Field> fields = new HashSet<>(Arrays.asList(data.target.getDeclaredFields()));

        if(!data.names.isEmpty())
            fields.removeIf(field -> !data.names.contains(field.getName()));

        if(data.type != null)
            fields.removeIf(field -> !data.type.isAssignableFrom(field.getType()));

        if(data.filter != null)
            fields.removeIf(field -> !data.filter.apply(field));

        if(fields.isEmpty()) {
            if(data.recursive && data.target.getSuperclass() != null)
                return searchFields(data.target.getSuperclass(), query, limit);

            throw new NoSuchFieldException(String.format("No field in '%s' matching the following query found: %s", data.target.getSimpleName(), data));
        } else if(limit > 0 && fields.size() > limit)
            throw new RuntimeException(String.format("Found %s fields matching the query: %s\n[%s]", fields.size(), data, fields.stream().map(Field::toString).collect(Collectors.joining("\n"))));

        if(data.makeAccessible) {
            for (Field field : fields) {
                if(!field.isAccessible()) field.setAccessible(true);

                if(Modifier.isFinal(field.getModifiers())) {
                    try {
                        Field modifiersField = Field.class.getDeclaredField("modifiers");
                        modifiersField.setAccessible(true);
                        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                    } catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return fields;
    }

    public static Method searchMethod(Object o, Function<MethodSearchData.MethodSearchDataBuilder, MethodSearchData> query) throws NoSuchMethodException {
        return searchMethods(o, query, 1).iterator().next();
    }

    public static Set<Method> searchMethods(Object o, Function<MethodSearchData.MethodSearchDataBuilder, MethodSearchData> query) throws NoSuchMethodException {
        return searchMethods(o, query, -1);
    }

    private static Set<Method> searchMethods(Object o, Function<MethodSearchData.MethodSearchDataBuilder, MethodSearchData> query, int limit) throws NoSuchMethodException {
        MethodSearchData data = query.apply(MethodSearchData.builder()
            .target(o instanceof Class<?>
                ? (Class<?>) o
                : o.getClass()
            ));

        Set<Method> methods = new HashSet<>(Arrays.asList(data.target.getDeclaredMethods()));

        if(!(!data.names.isEmpty() || data.returnType != null || data.paramTypes != null || data.paramCount != -1 || data.filter != null))
            throw new IllegalArgumentException("names, returnType, paramTypes, paramCount, and filter cannot all be empty");

        if(!data.names.isEmpty())
            methods.removeIf(method -> !data.names.contains(method.getName()));

        if(data.returnType != null)
            methods.removeIf(method -> !data.returnType.isAssignableFrom(method.getReturnType()));

        if(data.paramTypes != null)
            methods.removeIf(method -> {
                Class<?>[] parameters = method.getParameterTypes();
                if(parameters.length != data.paramTypes.length) return true;

                for(int i = 0; i < parameters.length; i++) {
                    if(!data.paramTypes[i].isAssignableFrom(parameters[i]))
                        return true;
                }

                return false;
            });
        else if(data.paramCount != -1)
            methods.removeIf(method -> method.getParameterCount() != data.paramCount);

        if(data.filter != null)
            methods.removeIf(method -> !data.filter.apply(method));

        // remove duplicates
        new HashMap<Integer, Set<Method>>() {{
            for (Method method : methods) {
                int hash = Objects.hash(method.getName(), Arrays.hashCode(method.getParameterTypes()));
                if(containsKey(hash)) get(hash).add(method);
                else put(hash, new HashSet<>(Collections.singletonList(method)));
            }

            for (Entry<Integer, Set<Method>> entry : new ArrayList<>(entrySet()))
                if(entry.getValue().size() <= 1) remove(entry.getKey());

            for (Set<Method> dupes : new ArrayList<>(values())) {
                try {
                    Method dupe = dupes.iterator().next();
                    Method m = data.target.getDeclaredMethod(dupe.getName(), dupe.getParameterTypes());
                    methods.removeAll(dupes);
                    methods.add(m);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }};

        if(methods.isEmpty()) {
            if(data.recursive && data.target.getSuperclass() != null)
                return searchMethods(data.target.getSuperclass(), query, limit);

            throw new NoSuchMethodException(String.format("No method in '%s' matching the following query found: %s", data.target.getSimpleName(), data));
        } else if(limit > 0 && methods.size() > limit)
            throw new RuntimeException(String.format("Found %s methods matching the query: %s\n[%s]", methods.size(), data, methods.stream().map(Method::toString).collect(Collectors.joining("\n"))));

        if(data.makeAccessible) {
            for (Method method : methods) {
                if(!method.isAccessible()) method.setAccessible(true);
            }
        }

        return methods;
    }

    @Builder
    @ToString
    public static final class FieldSearchData {
        @NotNull
        private final Class<?> target;

        @Singular
        private final Set<String> names;

        @Builder.Default
        private final boolean recursive = false;

        @Nullable
        @Builder.Default
        private final Class<?> type = null;

        @Nullable
        @Builder.Default
        private final Function<Field, Boolean> filter = null;

        @Builder.Default
        private final boolean makeAccessible = false;
    }

    @Builder
    @ToString
    public static final class MethodSearchData {
        @NotNull
        private final Class<?> target;

        @Singular
        private final Set<String> names;

        @Builder.Default
        private final boolean recursive = false;

        @Nullable
        @Builder.Default
        private final Class<?> returnType = null;

        private final Class<?>[] paramTypes;

        @Builder.Default
        private final int paramCount = -1;

        @Nullable
        @Builder.Default
        private final Function<Method, Boolean> filter = null;

        @Builder.Default
        private final boolean makeAccessible = false;
    }

}
