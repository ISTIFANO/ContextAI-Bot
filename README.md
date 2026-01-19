# Chatbot API with LangChain4j + Gemini AI + Spring Boot

A production-ready chatbot REST API built with Spring Boot, LangChain4j, and Google's Gemini AI. Features automatic context management, conversation history, and multi-session support.

## Features

- **Automatic Context Management**: LangChain4j handles system prompts and conversation history internally
- **Multi-Session Support**: Handle multiple concurrent user conversations
- **Memory Window**: Maintains last 30 messages per conversation to optimize token usage
- **RESTful API**: Clean endpoints for chat interactions and session management
- **Thread-Safe**: Concurrent conversation handling with ConcurrentHashMap
- **Token Optimization**: Automatic context window management

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Google Gemini API Key
- PostgreSQL (optional, for persistence)

##  Tech Stack

- **Spring Boot 3.1.6**: Application framework
- **LangChain4j 1.10.0**: AI orchestration framework
- **Google Gemini AI**: Language model (gemini-2.0-flash-exp)
- **PostgreSQL**: Database (optional)
- **Lombok**: Boilerplate reduction
- **Jakarta Validation**: Request validation

## Installation

1. **Clone the repository**
```bash
git clone https://github.com/ISTIFANO/ContextAI-Bot.git
cd chatbot-project
```

2. **Configure API Key**

Edit `src/main/resources/application.properties`:
```properties
gemini.api.key=YOUR_GEMINI_API_KEY_HERE
```

Or set as environment variable:
```bash
export GEMINI_API_KEY=your-api-key-here
```

3. **Build the project**
```bash
mvn clean install
```

4. **Run the application**
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

##  Configuration

### application.properties

```properties
# Server
server.port=8080

# Gemini API
gemini.api.key=${GEMINI_API_KEY}

# Database (optional)
spring.datasource.url=jdbc:postgresql://localhost:5432/chatbot_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Logging
logging.level.dev.langchain4j=DEBUG
```

##  API Endpoints

### 1. Send Chat Message

**POST** `/api/gemini/chat`

**Request Body:**
```json
{
  "message": "What is the capital of Morocco?",
  "conversationId": "user123"
}
```

**Response:**
```json
{
  "response": "The capital of Morocco is Rabat.",
  "conversationId": "user123",
  "timestamp": "2026-01-19T10:30:00",
  "activeConversations": 1
}
```

**Notes:**
- `conversationId` is optional (defaults to "default")
- Each conversation maintains its own context/history

### 2. Clear Specific Conversation

**DELETE** `/api/gemini/conversation/{conversationId}`

**Response:**
```json
"Conversation cleared: user123"
```

### 3. Clear All Conversations

**DELETE** `/api/gemini/conversations`

**Response:**
```json
"All conversations cleared"
```

### 4. Get Statistics

**GET** `/api/gemini/stats`

**Response:**
```json
5
```
(Returns number of active conversations)

##  Usage Examples

### Using cURL

**Simple chat:**
```bash
curl -X POST http://localhost:8080/api/gemini/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello, how are you?",
    "conversationId": "user1"
  }'
```

**Follow-up message (with context):**
```bash
curl -X POST http://localhost:8080/api/gemini/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Can you explain more?",
    "conversationId": "user1"
  }'
```

**Clear conversation:**
```bash
curl -X DELETE http://localhost:8080/api/gemini/conversation/user1
```

### Using JavaScript (fetch)

```javascript
async function sendMessage(message, conversationId = 'default') {
  const response = await fetch('http://localhost:8080/api/gemini/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ message, conversationId })
  });
  
  return await response.json();
}

// Usage
const result = await sendMessage('What is AI?', 'user123');
console.log(result.response);
```

### Using Python (requests)

```python
import requests

def send_message(message, conversation_id='default'):
    url = 'http://localhost:8080/api/gemini/chat'
    payload = {
        'message': message,
        'conversationId': conversation_id
    }
    response = requests.post(url, json=payload)
    return response.json()

# Usage
result = send_message('Hello!', 'user123')
print(result['response'])
```

##  How Context Management Works

LangChain4j automatically handles the conversation context:

```
Request 1: "What is the capital of France?"
→ Context sent to AI:
  - System Prompt
  - User: "What is the capital of France?"

Request 2: "What about its population?"
→ Context sent to AI:
  - System Prompt
  - History: [
      User: "What is the capital of France?"
      AI: "The capital of France is Paris."
    ]
  - User: "What about its population?"
```

**Key Points:**
- System prompt is added once per conversation
- History is maintained automatically (last 30 messages)
- You don't need to manually send previous messages
- Each request includes full context internally

##  Architecture

```
┌─────────────────┐
│  Controller     │ (REST API Layer)
│  GeminiController│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Service        │ (Business Logic)
│  MemoryChatBot  │
│  Service        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  LangChain4j    │ (AI Orchestration)
│  - ChatMemory   │
│  - AiServices   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Gemini AI      │ (LLM)
│  Model          │
└─────────────────┘
```

##  Project Structure

```
src/main/java/com/chatboot/chatboot/
├── controllers/
│   └── GeminiController.java       # REST endpoints
├── services/
│   └── MemoryChatBotService.java  # Core chatbot logic
├── dtos/
│   ├── ChatRequestDto.java        # Request model
│   └── ChatResponseDto.java       # Response model
└── ChatbootApplication.java       # Main application

src/main/resources/
└── application.properties         # Configuration
```

##  Key Components

### MemoryChatBotService

- Manages conversation sessions using `ConcurrentHashMap`
- Creates `MessageWindowChatMemory` for each conversation
- Initializes Gemini AI model
- Provides methods for sending messages and clearing history

### ConversationService Interface

```java
public interface ConversationService {
    String chat(String message);
}
```

Auto-implemented by LangChain4j's `AiServices.builder()`

### Memory Window

```java
MessageWindowChatMemory.withMaxMessages(30)
```

Keeps only the last 30 messages to prevent token limit issues.

## ️ Configuration Options

### Gemini Model Settings

In `MemoryChatBotService.java`:

```java
GoogleAiGeminiChatModel.builder()
    .apiKey(geminiApiKey)
    .modelName("gemini-2.0-flash-exp")  // Model version
    .temperature(0.7)                    // Creativity (0-1)
    .maxOutputTokens(1000)               // Max response length
    .build();
```

### Memory Window Size

```java
MessageWindowChatMemory.withMaxMessages(30)  // Adjust as needed
```

### System Prompt

Customize in constructor:
```java
this.systemPrompt = "You are a helpful AI assistant...";
```

##  Testing

### Unit Tests Example

```java
@SpringBootTest
class MemoryChatBotServiceTest {
    
    @Autowired
    private MemoryChatBotService service;
    
    @Test
    void testChatWithContext() {
        String conv = "test123";
        
        String response1 = service.sendMessage("Hello", conv);
        assertNotNull(response1);
        
        String response2 = service.sendMessage("Remember what I said?", conv);
        // Should have context from previous message
        assertNotNull(response2);
    }
}
```

##  Error Handling

The application includes validation:

```java
@NotBlank(message = "Message cannot be empty")
private String message;
```

Errors return appropriate HTTP status codes:
- 400: Bad Request (validation errors)
- 500: Internal Server Error

##  Security Considerations

1. **API Key Protection**: Never commit API keys to version control
2. **CORS**: Configure appropriately for production
3. **Rate Limiting**: Consider adding rate limits for API endpoints
4. **Input Validation**: All inputs are validated using Jakarta Validation

##  Token Management

- System prompt: ~50 tokens (sent once per conversation)
- User message: Variable
- History: Last 30 messages (auto-managed)
- Response: Up to 1000 tokens

Total context per request ≈ System + History + Current message

##  Troubleshooting

### Issue: "API Key not found"
**Solution**: Set `GEMINI_API_KEY` environment variable or in application.properties

### Issue: "Out of memory"
**Solution**: Reduce `maxMessages` in MessageWindowChatMemory

### Issue: "Token limit exceeded"
**Solution**: Decrease `maxOutputTokens` or reduce memory window size

##  Performance Tips

1. **Memory Management**: Periodically clear old conversations
2. **Token Optimization**: Adjust memory window based on use case
3. **Concurrent Users**: The service is thread-safe for multiple users
4. **Caching**: Consider caching for frequently asked questions

##  Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

##  License

This project is licensed under the MIT License.

##  Resources

- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [Google Gemini API](https://ai.google.dev/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

##  Support

For issues and questions, please open a GitHub issue.

---

** Spring Boot, LangChain4j, and Gemini AI**