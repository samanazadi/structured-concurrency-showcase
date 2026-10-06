package com.example;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

public class FastestMirror {

    static void main() throws InterruptedException {
        var mirrors = List.of(
                new Mirror("mirror-A", Duration.ofSeconds(1), true),
                new Mirror("mirror-B", Duration.ofSeconds(2), true),
                new Mirror("mirror-C", Duration.ofSeconds(3), true)
        );

        long start = System.nanoTime();

        try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulOrThrow())) {
            mirrors.forEach(mirror -> scope.fork(() -> download(mirror)));
            IO.println("Fastest mirror: " + scope.join());
        } catch (ExecutionException _) {
            IO.println("All mirrors failed");
        }

        IO.println("Took: " + Duration.ofNanos(System.nanoTime() - start).toMillis() + "ms");
    }

    static String download(Mirror mirror) throws InterruptedException, DownloadFailedException {
        Thread.sleep(mirror.downloadTime());
        if (mirror.fails()) {
            throw new DownloadFailedException();
        }
        return mirror.name();
    }

    record Mirror(String name, Duration downloadTime, boolean fails) {
    }

    static class DownloadFailedException extends Exception {
    }
}
