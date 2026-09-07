package io.github.prabhusiddarth.sidd_bot.conversation;

import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConversationThreadSafetyTest {

    @Test
    void testConcurrentAddMessages() throws InterruptedException {
        Conversation conversation = new Conversation("thread-safe-session");
        int numThreads = 10;
        int messagesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    conversation.addMessage(Role.USER, "Concurrent Message");
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(numThreads * messagesPerThread, conversation.getMessages().size(),
                "All messages should be added correctly without ConcurrentModificationException or loss");
    }
}
