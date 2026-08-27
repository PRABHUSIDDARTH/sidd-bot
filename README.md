# SiddBot

A modular AI chatbot framework and SDK written in Java.

## Project Structure

```
sidd-bot/
├── pom.xml
├── README.md
├── LICENSE
├── .gitignore
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── io/
│   │           └── github/
│   │               └── prabhusiddarth/
│   │                   └── sidd_bot/
│   │                       │
│   │                       ├── SiddBot.java
│   │                       │
│   │                       ├── conversation/
│   │                       │   ├── Conversation.java
│   │                       │   ├── ConversationManager.java
│   │                       │   ├── Message.java
│   │                       │   └── Role.java
│   │                       │
│   │                       ├── model/
│   │                       │   ├── Model.java
│   │                       │   ├── Provider.java
│   │                       │   └── ModelManager.java
│   │                       │
│   │                       ├── memory/
│   │                       │   ├── ConversationStore.java
│   │                       │   └── InMemoryConversationStore.java
│   │                       │
│   │                       ├── security/
│   │                       │   └── PromptGuard.java
│   │                       │
│   │                       └── examples/
│   │                           ├── BasicChat.java
│   │                           ├── MultiConversation.java
│   │                           └── RuntimeModelSwitch.java
│   │
│   └── test/
│       └── java/
│           └── io/
│               └── github/
│                   └── prabhusiddarth/
│                       └── sidd_bot/
│                           │
│                           ├── SiddBotTest.java
│                           │
│                           ├── conversation/
│                           │   ├── ConversationTest.java
│                           │   └── ConversationManagerTest.java
│                           │
│                           ├── model/
│                           │   └── ModelManagerTest.java
│                           │
│                           └── memory/
│                               └── InMemoryConversationStoreTest.java
```

## Features

- **Conversation Management**: Multi-session conversation tracking with `ConversationManager`.
- **Model Switching**: Dynamic provider/model switching via `ModelManager`.
- **Pluggable Storage**: Extensible `ConversationStore` interface with `InMemoryConversationStore` and persistent `SqliteConversationStore`.
- **Generation Controls & Token Usage**: Fine-grained temperature/maxTokens control via `GenerationConfig` and per-request token usage tracking via `TokenUsage`.
- **Prompt Security**: Basic prompt sanitization & injection check with `PromptGuard`.

## Getting Started

### Build the Project
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](file:///mnt/D/sidd-bot/LICENSE) file for details.
