'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var userId = null;
var username = null;
var userList = null;
var status = null;
var isRefreshing = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

const Status = {
    ONLINE: 'online-icon.png',
    AWAY: 'away-icon.png',
    BUSY: 'busy-icon.png',
    OFFLINE: 'offline-icon.png'
};

function getStatusValue(statusName) {
  return Status[statusName] || null;
}

function autoConnect(event) {
    const userIdCookie = document.cookie.split('; ').find(row => row.startsWith('userId='));;
    const userIdValue = userIdCookie ? userIdCookie.split('=')[1] : null;

    if (userIdValue) {
      // User is returning
      console.log("old");
      userId = userIdCookie.split('=')[1];

      usernamePage.classList.add('hidden');
      chatPage.classList.remove('hidden');

      var socket = new SockJS('/ws');
      stompClient = Stomp.over(socket);

      stompClient.connect({}, onOldConnection, onError);
    }

    connectingElement.classList.add('hidden');
}

function connect(event) {
    // User is new
    userId = generateRandomCookie(6);
    document.cookie = setCookie("userId", userId, 1);
    username = document.querySelector('#name').value.trim();

    if (username) {
        status = "ONLINE";

        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onNewConnection, onError);
    }
    event.preventDefault();

    connectingElement.classList.add('hidden');
}

function onOldConnection() {
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/topic/userList', refreshUserList);
    stompClient.subscribe('/topic/oldUser', getOldUser);

    stompClient.send("/app/chat.getUser", {}, userId);
}

function getOldUser(payload) {
    var oldUser = JSON.parse(payload.body);
    status = "ONLINE";
    username = oldUser.username;

    stompClient.send("/app/chat.putUser",
                {},
                JSON.stringify({id: userId, username: username, status: status}));
}

function onNewConnection() {
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/topic/userList', refreshUserList);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({userId: userId, sender: username, type: 'JOIN'})
    )
}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if(messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }
    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);

    textElement.appendChild(messageText);
    messageElement.appendChild(textElement);
    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function refreshUserList (userList) {
    let userListEl = document.querySelector("#userList");

    // Parse the JSON string received from the server
    userList = JSON.parse(userList.body);

    // Clean Users List
    while (userListEl.firstChild) {
        userListEl.removeChild(userListEl.firstChild);
    }

    //add all users
    Object.values(userList).forEach(user => {
        let statusIcon = document.createElement('img');
        let listItem = document.createElement('li');
        let usernameItem = document.createElement('p');
        let statusValue = getStatusValue(user.status);

        statusIcon.src = "./images/" + statusValue;
        statusIcon.classList.add('user-status-icon');
        usernameItem.textContent = user.username;

        usernameItem.appendChild(statusIcon);
        listItem.appendChild(usernameItem);

        if (user.status == "OFFLINE") {
            let lastOnlineItem = document.createElement('p');

            lastOnlineItem.classList.add('last-time-online');
            lastOnlineItem.textContent = "Last Seen: " + parseDate(user.lastTimeOnline);
            listItem.appendChild(lastOnlineItem);
        }
        userListEl.appendChild(listItem)
    });
}

function handleDisconnect(event) {
    console.log("disconnect triggered");
    isRefreshing = true;
    status = "OFFLINE";
    stompClient.send("/app/chat.removeUser",
    {},
    JSON.stringify({userId: userId, sender: username, type: 'LEAVE'}));
}

function handleSwitchTab(event) {
    console.log("switch tab triggered");
    if (!isRefreshing) {
        status = (document.hidden) ? "AWAY" : "ONLINE";
        stompClient.send("/app/chat.putUser",
            {},
            JSON.stringify({id: userId, username: username, status: status}));
    }
}

function handleRefresh(event) {
    console.log("load triggered");
    isRefreshing = false;
}

window.addEventListener('DOMContentLoaded', autoConnect, false);
usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
window.addEventListener('beforeunload', handleDisconnect, false);
window.addEventListener('visibilitychange', handleSwitchTab, false);
window.addEventListener('load', handleRefresh, false);