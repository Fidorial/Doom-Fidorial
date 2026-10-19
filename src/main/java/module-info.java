import org.jspecify.annotations.NullMarked;

@NullMarked
module fr.fidorial.doom {
    requires fr.fidorial;
    requires java.desktop;
    requires java.net.http;
    requires jdk.httpserver;
    requires net.kyori.adventure.api;
    requires net.kyori.adventure.key;
    requires net.kyori.adventure.text.logger.slf4j;
    requires net.kyori.adventure.text.serializer.plain;
}
