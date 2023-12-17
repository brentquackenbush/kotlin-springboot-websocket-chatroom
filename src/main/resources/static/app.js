var stompClient = null;
var currentChatRoomId = null;

// State management
const state = {
    connected: false,
    username: '',
    chatroomId: null,
    users: [],
    messages: []
};

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

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
    });
}

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
    $("#joinChatRoom").click(joinChatRoom);

    // Send a message to the chatroom
    $("#sendMessage").click(sendMessage);
});

function joinChatRoom() {
    console.log("Join chatroom button clicked.");
    var chatRoomId = $("#chatRoomId").val();
    var name = $("#name").val();
    if (chatRoomId && name) {
        currentChatRoomId = chatRoomId;
        state.username = name;
        state.chatroomId = chatRoomId;

        // Existing message subscription
        stompClient.subscribe('/topic/chatroom' + chatRoomId, function (chatMessage) {
            showMessage(chatMessage.body);
        });

        stompClient.subscribe('/topic/chatroomUsers' + chatRoomId, function (userListMessage) {
            console.log("Subscription to /topic/chatroomUsers" + chatRoomId + " received a message.");
            var users = JSON.parse(userListMessage.body);
            console.log("Received user list update for Chatroom: " + users);
            updateUserList(users);
        });

        // Subscribe to user leave updates
        stompClient.subscribe('/topic/chatroomUserLeave' + chatRoomId, function (userLeaveMessage) {
            var leavingUser = JSON.parse(userLeaveMessage.body).screenName;
            removeUserFromChatroom(leavingUser);
        });

        // Inform the server about the new user joining
        stompClient.send("/app/chatroom", {}, JSON.stringify({'chatRoomId': chatRoomId, 'screenName': name, 'timestamp': undefined}));

        // UI updates
        $("#chatArea").show();
        $("#joinChatRoomForm").hide();
    } else {
        alert("Name and Chatroom ID are required to join a chatroom.");
    }
}

function sendMessage() {
    var messageContent = $("#messageContent").val();
    if (messageContent && stompClient && state.connected) {
        var uniqueId = Date.now() + "_" + state.username; // Unique ID using timestamp and username
        var chatMessage = {
            id: uniqueId,
            chatRoomId: currentChatRoomId,
            sender: state.username,
            message: messageContent
        };

        // Send the chat message to the server
        stompClient.send("/app/send", {}, JSON.stringify(chatMessage));

        // Clear the message input field after sending
        $("#messageContent").val('');

        // Do not immediately add to state; wait for it to come through subscription
    }
}

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

function updateUserList(userList) {
    // This replaces the entire list of users with the new list
    console.log(userList)
    state.users = userList;
    updateUsersUI();
}

function removeUserFromChatroom(leavingUser) {
    state.users = state.users.filter(user => user !== leavingUser);
    updateUsersUI();
}

function updateUsersUI() {
    var usersHtml = state.users.map(function(user) {
        return `<li>${user}</li>`;
    }).join('');

    $("#users").html(usersHtml);
}
