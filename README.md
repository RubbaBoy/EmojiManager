# Emoji Manager

Emoji Manager is a desktop app that uses your Discord token to manage all your emojis in the Discord servers you own. Servers not manually enabled will never be affected, and emojis can be backed up/downloaded and restored (Removing all emojis from servers affected by the backup and then setting them to the backup), along with uploaded and removed in bulk. Emoji Manager will also automatically create Discord servers for you when uploading emojis if there is no space in available servers, heavily reducing the time it would normally take to upload emojis.

When starting the program, you can either make a file called `token` with your Discord token in it, or run it and paste in the following JavaScript code in your Discord's console window, which after you click on a new channel/server, it will send the token locally to the program for it to start. This will only need to be done once.

```javascript
XMLHttpRequest.prototype.wrappedSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader; XMLHttpRequest.prototype.setRequestHeader = function (header, value) { this.wrappedSetRequestHeader(header, value); if (header === 'Authorization') { let socket = new WebSocket(`ws://127.0.0.1:6979/token:${value}`); XMLHttpRequest.prototype.setRequestHeader = this.wrappedSetRequestHeader; setTimeout(socket.close, 1000); }};
```

Here's some screenshots of the program:

![Emojis Tab](https://github.com/RubbaBoy/EmojiManager/blob/master/screenshots/emojis.png)

![Emojis Tab](https://github.com/RubbaBoy/EmojiManager/blob/master/screenshots/servers.png)

![Emojis Tab](https://github.com/RubbaBoy/EmojiManager/blob/master/screenshots/backups.png)