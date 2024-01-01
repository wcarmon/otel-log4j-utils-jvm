package io.github.wcarmon.otel.log4j;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;

/** Forwards all Span events to a Log4j2 appender. */
public final class Log4jSpanProcessor implements SpanProcessor {

    private final Appender appender;

    private final OtelToLog4j converter;

    private Log4jSpanProcessor(OtelToLog4j converter, String targetAppenderName) {

        requireNonNull(converter, "converter is required and null.");
        if (targetAppenderName == null || targetAppenderName.isBlank()) {
            throw new IllegalArgumentException("targetAppenderName is required");
        }

        this.converter = converter;

        final var context = (LoggerContext) LogManager.getContext(false);
        final var config = context.getConfiguration();
        appender = config.getAppender(targetAppenderName);

        if (appender == null) {
            throw new IllegalStateException(
                    "failed to lookup appender: name=" + targetAppenderName);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    @Override
    public boolean isStartRequired() {
        return false;
    }

    @Override
    public void onEnd(ReadableSpan span) {

        final var spanData = span.toSpanData();
        final var events = spanData.getEvents();
        if (events == null || events.isEmpty()) {
            // -- Nothing to log
            return;
        }

        converter.convertEvents(spanData).forEach(appender::append);
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {}

    public static class Builder {

        private OtelToLog4j converter;
        private String targetAppenderName;

        Builder() {}

        public Log4jSpanProcessor build() {
            return new Log4jSpanProcessor(this.converter, this.targetAppenderName);
        }

        public Builder converter(OtelToLog4j converter) {
            this.converter = converter;
            return this;
        }

        public Builder targetAppenderName(String targetAppenderName) {
            this.targetAppenderName = targetAppenderName;
            return this;
        }

        public String toString() {
            return "Log4jSpanProcessor.Builder(converter="
                    + this.converter
                    + ", targetAppenderName="
                    + this.targetAppenderName
                    + ")";
        }
    }
}
