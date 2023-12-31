# Kotlin Spring Boot WebSocket Chatroom Application

## Introduction to WebSocket
> WebSocket is a protocol providing full-duplex communication channels over a single TCP connection. It's designed for real-time data exchange between a client and a server.

## Comparison with REST
WebSocket and REST serve different purposes. REST, a stateless communication method, is used for CRUD operations and requires repeated requests for new data. In contrast, WebSocket maintains a continuous connection, allowing for instant data updates and communication.

## Understanding STOMP

### What is STOMP?
STOMP (Simple/Streaming Text Oriented Messaging Protocol) is a simple, text-based protocol used for messaging across different languages and platforms. It provides an interoperable wire format for client-broker communication.

### STOMP in Spring Boot
In Spring Boot, STOMP over WebSocket facilitates real-time communication. It manages subscriptions and messaging, simplifying the development of applications like chat services.

## Project Structure 
![Screenshot 2023-12-30 at 5.54.17 PM.png](..%2FScreenshot%202023-12-30%20at%205.54.17%E2%80%AFPM.png)

## Configuring WebSocket in Spring Boot

In our Spring Boot application, we configure WebSocket to enable real-time, bi-directional communication between the client and server. This is essential for features like live chat messaging.

### WebSocket Configuration

```kotlin
@Configuration
@EnableWebSocketMessageBroker
class WSConfig: WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // Simple in-memory message broker
        registry.enableSimpleBroker("/topic", "/queue")

        // Application destination prefixes
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // Register WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws").setHandshakeHandler(CustomHandshakeHandler()).withSockJS()
    }
}
```

`@EnableWebSocketMessageBroker`: This annotation enables WebSocket message handling, backed by a message broker.

`WebSocketMessageBrokerConfigurer`: Interface providing methods to configure the WebSocket connection.

`configureMessageBroker`: Configures the message broker for broadcasting to clients (/topic, /queue) and designates the /app prefix for messages bound for methods annotated with @MessageMapping.

`registerStompEndpoints`: Registers the /ws endpoint, which clients use to connect to the WebSocket server. CustomHandshakeHandler manages the initial handshake, and withSockJS ensures compatibility with browsers that don't support WebSocket natively.

## Custom Handshake Handler in WebSocket Configuration

The `CustomHandshakeHandler` in our Spring Boot WebSocket application plays a pivotal role in personalizing the WebSocket connection for each user. It extends the `DefaultHandshakeHandler` to implement custom logic during the WebSocket handshake process.

### Implementation

```kotlin
class CustomHandshakeHandler : DefaultHandshakeHandler() {
    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Principal? {
        val userId = request.uri.query.split("&")
            .map { it.split("=") }
            .firstOrNull { it[0] == "userId" }
            ?.getOrNull(1)

        return userId?.let { UserPrincipal(it) }
    }
}
```

`User Identification`: Extracts the userId parameter from the handshake request's query string.

`Principal Creation`: Creates a UserPrincipal object if userId is present.

`Session Management`: Enables sending messages to a user's private queue and managing their session.

## Creating a Chat Model
This data transfer object (DTO) encapsulates the details of a message sent within a chat room.

```kotlin
data class ChatRoomMessageDto(
    val id: String,
    val chatRoomId: Int,
    val sender: String,
    val message: String,
)
```

This DTO represents a user's request to join a chat room.

```kotlin
data class JoinChatRoomMessageDto(
    val userId: String,
    val chatRoomId: Int,
    val screenName: String,
    var timestamp: Instant? = null,
)
```

### Understanding Data Transfer Objects (DTOs)

DTOs play a crucial role in cleanly separating the format of data sent from controllers to client-side systems from the application's internal domain logic. They are essential in ensuring a well-structured and maintainable codebase.

 > ####  Why Use DTOs?
> - **Decoupling**: DTOs decouple the external interface format from the internal domain model, allowing independent evolution.
> - **Security**: They help in exposing only the necessary data, enhancing security by avoiding accidental exposure of internal details.
> - **Flexibility**: Facilitates easy modification of the data format shared with clients without impacting the domain models.
> - **Performance Optimization**: Tailoring DTOs for specific use cases can reduce payload size and improve performance.

> In summary, DTOs are vital in creating clear boundaries between different layers of an application, providing a more secure, flexible, and maintainable architecture.

## Creating our Chat Controller

The ChatRoomController class manages WebSocket connections and messaging in chat rooms.

```kotlin
@Controller
class ChatRoomController {
//... [Controller properties and constructor]

    // Method to handle user joining a chat room
    @MessageMapping("/chatroom")
    fun chatRoom(joinChatRoomMessageDto: JoinChatRoomMessageDto, headerAccessor: SimpMessageHeaderAccessor) {
        //... [Method implementation]
    }

    // Method to handle sending messages in a chat room
    @MessageMapping("/send")
    fun sendMessage(chatMessage: ChatRoomMessageDto, headerAccessor: SimpMessageHeaderAccessor) {
        //... [Method implementation]
    }

    // Method to handle WebSocket disconnection events
    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        //... [Method implementation]
    }
}
```

`chatRoom`: Manages user requests to join chat rooms.

`sendMessage`: Handles sending messages to chat rooms.

`handleWebSocketDisconnectListener`: Listens for disconnection events to manage user sessions.

## Creating a front-end UI

### Bobby joins chatroom 1
![Screenshot 2023-12-30 at 7.04.55 PM.png](..%2FScreenshot%202023-12-30%20at%207.04.55%E2%80%AFPM.png)
![Screenshot 2023-12-30 at 7.05.05 PM.png](..%2FScreenshot%202023-12-30%20at%207.05.05%E2%80%AFPM.png)
### Timothy joins chatroom 1
![Screenshot 2023-12-30 at 7.06.01 PM.png](..%2FScreenshot%202023-12-30%20at%207.06.01%E2%80%AFPM.png)
### Timothy says `Hello` (Timothy perspective)
![Screenshot 2023-12-30 at 7.06.11 PM.png](..%2FScreenshot%202023-12-30%20at%207.06.11%E2%80%AFPM.png)
### Bobby responds with `Yo` (Bobby perspective)
![Screenshot 2023-12-30 at 7.06.24 PM.png](..%2FScreenshot%202023-12-30%20at%207.06.24%E2%80%AFPM.png)
### John Cena tries to join chatroom 1, but max capacity is 2
![Screenshot 2023-12-30 at 7.06.44 PM.png](..%2FScreenshot%202023-12-30%20at%207.06.44%E2%80%AFPM.png)