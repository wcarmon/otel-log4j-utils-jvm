/** module decl for otel */
module io.github.wcarmon.otel.log4j {
    exports io.github.wcarmon.otel.log4j;

    requires io.opentelemetry.api;
    requires io.opentelemetry.context;
    requires io.opentelemetry.sdk.trace;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j;
    requires org.jetbrains.annotations;
}
