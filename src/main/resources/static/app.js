var stompClient = null;
var userId = null;

// State management
const state = {
    connected: false,
    username: '',
    currentChatRoomId: null,
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
    userId = getOrGenerateUserId();
    console.log(userId)
    var socket = new SockJS('/ws?userId=' + encodeURIComponent(userId));
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
    $("#joinChatRoom").click(function() {
        var chatRoomId = $("#chatRoomId").val();
        var name = $("#name").val();
        if(chatRoomId && name) {
            joinChatRoom(chatRoomId, name);
        } else {
            alert("Name and Chatroom ID are required to join a chatroom.");
        }
    });

    // Send a message to the chatroom
    $("#sendMessage").click(sendMessage);
});

function joinChatRoom(chatRoomId, name) {
    // Disable join button immediately to prevent multiple clicks
    $("#joinChatRoom").prop("disabled", true);

    state.currentChatRoomId = chatRoomId;
    state.username = name;

    var joinResponseSubscription = stompClient.subscribe('/user/queue/joinResponse', function (message) {
        var response = JSON.parse(message.body);
        if(response.status === "fail") {
            alert(response.message);
            $("#joinChatRoom").prop("disabled", false);
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


function subscribeToChatTopics(chatRoomId) {
    stompClient.subscribe('/topic/chatroom' + chatRoomId, function (chatMessage) {
        showMessage(chatMessage.body);
    });

    var usersSubscription = stompClient.subscribe('/topic/chatroomUsers' + chatRoomId, function (userListMessage) {
        updateUserList(JSON.parse(userListMessage.body));
    });

    stompClient.subscribe('/topic/chatroomUserLeave' + chatRoomId, function (userLeaveMessage) {
        removeUserFromChatroom(JSON.parse(userLeaveMessage.body).screenName);
    });

    // Save the chatRoomId in the state
    state.currentChatRoomId = chatRoomId;
}

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