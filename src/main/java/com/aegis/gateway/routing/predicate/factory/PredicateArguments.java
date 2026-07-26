package com.aegis.gateway.routing.predicate.factory;

import com.aegis.gateway.routing.exceptions.InvalidRouteConfigurationException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class PredicateArguments {

    private PredicateArguments() {}

    static String requiredString(Map<String, Object> args, String key, String predicateName) {

        Object value = args.get(key);
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return stringValue;
        }

        throw new InvalidRouteConfigurationException("Predicate '%s' requires non-empty String arg '%s'".formatted(predicateName, key));
    }

    static List<String> requiredStringList(Map<String, Object> args, String key, String predicateName) {

        Object value = args.get(key);
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return List.of(stringValue);
        }

        if (value instanceof Collection<?> collection) {
            List<String> values = collection.stream()
                    .map(item -> item == null ?  "" :  item.toString().trim())
                    .filter(item -> !item.isBlank())
                    .toList();

            if (!values.isEmpty()) {
                return values;
            }
        }

        if (value instanceof Map<?,?> map) {
            List<String> values = map.entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(PredicateArguments::mapKeyAsInt))
                    .map(Map.Entry::getValue)
                    .map(PredicateArguments::toNonBlankString)
                    .filter(val -> !val.isBlank())
                    .toList();

            if (!values.isEmpty()) {
                return values;
            }
        }

        throw new InvalidRouteConfigurationException("Predicate '%s' requires non-empty String list arg '%s'.%nActual value type: %s.%nReceived args: %s"
                        .formatted(predicateName, key, value == null ? "null" : value.getClass().getName(), args));
    }

    private static String toNonBlankString(Object value) {
        if (value == null) {
            return "";
        }

        return value.toString().trim();
    }

    private static int mapKeyAsInt(Map.Entry<?, ?> entry) {
        try {
            return Integer.parseInt(entry.getKey().toString());
        } catch (NumberFormatException exception) {
            return Integer.MAX_VALUE;
        }
    }

}