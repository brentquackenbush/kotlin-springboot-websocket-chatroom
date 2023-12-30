var stompClient = null;
var userId = null;

// State management object to track application state
const state = {
    connected: false,
    username: '',
    currentChatRoomId: null,
    users: [],
    messages: []
};

// Sets the connection status and updates UI accordingly
function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    $("#joinChatRoomForm").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#messages").html("");
    state.connected = connected;
}

// Establishes connection to the WebSocket server
function connect() {
    userId = getOrGenerateUserId();
    console.log(userId)
    var socket = new SockJS('/ws?userId=' + encodeURIComponent(userId));
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
    });
}

// Disconnects from the WebSocket server and resets state
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
    // Reset the state upon disconnection
    state.username = '';
    state.chatroomId = null;
    state.users = [];
    state.messages = [];
    updateChatbox();
}

// Sets up initial UI state and event handlers
$(document).ready(function() {
    // Initially hide chat area
    $("#chatArea").hide();

    // Prevent the default form submission behavior
    $("form").on('submit', function (e) {
        e.preventDefault();
    });

    // Connect and disconnect from the WebSocket
    $("#connect").click(connect);
    $("#disconnect").click(disconnect);

    // Join a chatroom
    $("#joinChatRoom").click(joinChatRoomHandler);

    // Send a message to the chatroom
    $("#sendMessage").click(sendMessage);
});

// Handler for join chat room action
function joinChatRoomHandler() {
    var chatRoomId = $("#chatRoomId").val();
    var name = $("#name").val();
    chatRoomId && name ? joinChatRoom(chatRoomId, name) : alert("Name and Chatroom ID required.");
}

function joinChatRoom(chatRoomId, name) {
    // Disable join button immediately to prevent multiple clicks
    $("#joinChatRoom").prop("disabled", true);

    state.currentChatRoomId = chatRoomId;
    state.username = name;

    // Move the subscription for user list updates here, before the join response
    var usersSubscription = stompClient.subscribe('/topic/chatroomUsers' + chatRoomId, function (userListMessage) {
        updateUserList(JSON.parse(userListMessage.body));
    });

    var joinResponseSubscription = stompClient.subscribe('/user/queue/joinResponse', function (message) {
        var response = JSON.parse(message.body);
        if(response.status === "fail") {
            alert(response.message);
            $("#joinChatRoom").prop("disabled", false);
            usersSubscription.unsubscribe(); // Unsubscribe if join fails
        } else {
            subscribeToChatTopics(chatRoomId);
            $("#chatArea").show();
            $("#joinChatRoomForm").hide();
            $('#websocketConnectionForm').hide();
        }
        joinResponseSubscription.unsubscribe();
    });

    stompClient.send("/app/chatroom", {}, JSON.stringify({
        'userId': userId,
        'chatRoomId': chatRoomId,
        'screenName': name,
        'timestamp': new Date().toISOString()
    }));
}

// Subscribes to different chat room topics (messages, user updates)
function subscribeToChatTopics(chatRoomId) {
    // Subscribe to chat messages
    stompClient.subscribe('/topic/chatroom' + chatRoomId, function (chatMessage) {
        showMessage(chatMessage.body);
    });

    // Subscribe to user leave updates
    stompClient.subscribe('/topic/chatroomUserLeave' + chatRoomId, function (userLeaveMessage) {
        removeUserFromChatroom(JSON.parse(userLeaveMessage.body).screenName);
    });

    // Save the chatRoomId in the state
    state.currentChatRoomId = chatRoomId;
}

// Sends a chat message
function sendMessage() {
    var messageContent = $("#messageContent").val();
    if (messageContent && stompClient && state.connected) {
        var uniqueId = Date.now() + "_" + state.username; // Unique ID using timestamp and username
        var chatMessage = {
            id: uniqueId,
            chatRoomId: state.currentChatRoomId,
            sender: state.username,
            message: messageContent
        };

        // Send the chat message to the server
        stompClient.send("/app/send", {}, JSON.stringify(chatMessage));

        // Clear the message input field after sending
        $("#messageContent").val('');
    }
}

// Displays a chat message in the chatbox
function showMessage(message) {
    var messageObj = JSON.parse(message);

    // Check if the message is sent by the current user and if it's already displayed
    if (messageObj.sender === state.username && messageObj.id && state.messages.find(m => m.id === messageObj.id)) {
        return; // Don't add it to the state; it's a duplicate
    }

    // Push the new message into the state
    state.messages.push({
        id: messageObj.id,
        sender: messageObj.sender,
        content: messageObj.message
    });

    // Update the chatbox with the new message
    updateChatbox();
}

// Updates the chatbox with new messages
function updateChatbox() {
    // Render the messages from the state to the chatbox
    var messagesHtml = state.messages.map(function (message) {
        return `<li><strong>${message.sender}:</strong> ${message.content}</li>`;
    }).join('');

    // Update the messages container with the new HTML
    $("#messages").html(messagesHtml);
    // Auto-scroll to the latest message
    var messagesContainer = $('#messages');
    if (messagesContainer.length) {
        messagesContainer.scrollTop(messagesContainer[0].scrollHeight);
    }
}

// Updates the user list
function updateUserList(userList) {
    state.users = userList;
    updateUsersUI();
}

// Removes a user from the chatroom
function removeUserFromChatroom(leavingUser) {
    state.users = state.users.filter(user => user !== leavingUser);
    updateUsersUI();
}

// Updates the UI with the current user list
function updateUsersUI() {
    var usersHtml = state.users.map(function(user) {
        return `<li>${user}</li>`;
    }).join('');

    $("#users").html(usersHtml);
}

function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

function getOrGenerateUserId() {
    let storedUserId = localStorage.getItem('userId');
    if (!storedUserId) {
        storedUserId = generateUUID();
        localStorage.setItem('userId', storedUserId);
    }
    return storedUserId;
}
