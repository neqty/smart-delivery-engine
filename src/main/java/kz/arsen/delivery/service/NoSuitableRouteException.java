package kz.arsen.delivery.service;

import java.util.List;
import java.util.Map;

public final class NoSuitableRouteException extends RuntimeException {
    private final Map<String, List<String>> rejections;
    public NoSuitableRouteException(String message, Map<String, List<String>> rejections) {
        super(message);
        this.rejections = rejections.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey, e -> List.copyOf(e.getValue())));
    }
    public Map<String, List<String>> getRejections() { return rejections; }
}
