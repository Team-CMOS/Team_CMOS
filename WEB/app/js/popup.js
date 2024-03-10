// window.onload = function () {
//   chrome.tabs.query({ currentWindow: true, active: true }, function (tabs) {
//     chrome.tabs.sendMessage(tabs[0].id, { message: "popup_open" });
//   });

//   document.getElementsByClassName("analyze-button")[0].onclick = function () {
//     chrome.tabs.query({ currentWindow: true, active: true }, function (tabs) {
//       chrome.tabs.sendMessage(tabs[0].id, { message: "analyze_site" });
//     });
//   };

//   document.getElementsByClassName("link")[0].onclick = function () {
//     chrome.tabs.create({
//       url: document.getElementsByClassName("link")[0].getAttribute("href"),
//     });
//   };
// };

// chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {
//   if (request.message === "update_current_count") {
//     document.getElementsByClassName("number")[0].textContent = request.count;
//   }
// });

window.onload = function () {
  chrome.tabs.query({ currentWindow: true, active: true }, function (tabs) {
    chrome.tabs.sendMessage(tabs[0].id, { message: "popup_open" });
  });

  document.getElementsByClassName("analyze-button")[0].onclick = function () {
    chrome.tabs.query({ currentWindow: true, active: true }, function (tabs) {
      chrome.tabs.sendMessage(tabs[0].id, { message: "analyze_site" });
    });
  };

  // Add event listener to the "Chat bot" button
  document.querySelector('.fb a').onclick = function (event) {
    event.preventDefault(); // Prevent default action of the anchor tag
    var chatUrl = this.getAttribute('href'); // Get the href attribute
    chrome.tabs.create({ url: chatUrl }); // Open chat.html in a new tab
  };
};

chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {
  if (request.message === "update_current_count") {
    document.getElementsByClassName("number")[0].textContent = request.count;
  }
});

