const constants = {
  DEV: "DEV",
  PRODUCTION: "PRODUCTION"
}
const ENV = constants.DEV
// const ENV = constants.PRODUCTION
import Analytics from './scripts/google-analytics.js';

function firePageViewEvent(title, href) {
  if (ENV === constants.PRODUCTION) {
    Analytics.firePageViewEvent(title, href)
    .catch(e => {});
  }
}

function fireErrorEvent(reason) {
  if (ENV === constants.PRODUCTION) {
    Analytics.fireErrorEvent(reason)
    .catch(e => {});
  }
}

function fireEvent(eventName, params = {}) {
  if (ENV === constants.PRODUCTION) {
    Analytics.fireEvent(eventName, params)
    .catch(e => {});
  }
}

addEventListener('unhandledrejection', async (event) => {
  fireErrorEvent(event.reason);
});

chrome.runtime.onInstalled.addListener(() => {
  fireEvent('install');
});

const MESSAGE_TYPE = {
  CHAT: "CHAT",
  HEART_BEAT: "HEART_BEAT",
  PRODUCT_INIT: "PRODUCT_INIT",
  FCM_TOKEN: "FCM_TOKEN",
  PRODUCT_INFO_REQUEST: "PRODUCT_INFO_REQUEST",
  TOOLBAR_BUTTON_CLICK: "TOOLBAR_BUTTON_CLICK",
  PAGE_VIEW_EVENT: "PAGE_VIEW_EVENT",
  CLICK_EVENT: "CLICK_EVENT",
  PRODUCT_PAGE_LOAD: "PRODUCT_PAGE_LOAD"
}
const PATHS = {
  base: ENV === constants.PRODUCTION ? "https://api.shoppiem.com" : "http://localhost:8080",
  extension: "/extension"
}

function getHtml() {return document.documentElement.outerHTML; }

function getProductInfoKey(productSku) {
  return "productInfo-" + productSku;
}

async function getCurrentTab(productSku) {
  const tabs = await chrome.tabs.query({active: true})
  if (tabs) {
    for (let i = 0; i < tabs.length; i++) {
      const tab = tabs[i];
      if (tab.url?.includes(productSku)) {
        return tab;
      }
    }
  }
  return undefined;
}

function post(host, body) {
  fetch(host, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  })
  .then(response => response.json())
  .then(data => {
    // console.log(data);
  })
  .catch(error => {
    console.error(error);
  });
}

async function initProduct(url, html) {
  const host = `${PATHS.base}${PATHS.extension}`
  const token = await chrome.storage.local.get("rId")
  const requestBody = {
    product_url: url,
    html: html,
    token: token.rId,
    type: MESSAGE_TYPE.PRODUCT_INIT
  };
  post(host, requestBody)
}

async function sendQuery(request) {
  const host = `${PATHS.base}${PATHS.extension}`
  const token = await chrome.storage.local.get("rId")
  const requestBody = {
    token: token.rId,
    type: MESSAGE_TYPE.CHAT,
    query: request.query,
    product_sku: request.productSku
  };
  post(host, requestBody)
}

async function sendProductInfoRequest(productSku) {
  const host = `${PATHS.base}${PATHS.extension}`
  const token = await chrome.storage.local.get("rId")
  const requestBody = {
    token: token.rId,
    type: MESSAGE_TYPE.PRODUCT_INFO_REQUEST,
    product_sku: productSku
  };
  post(host, requestBody)
}

function heartbeatACK() {
  const host = `${PATHS.base}${PATHS.extension}`
  const requestBody = {
    token: chrome.storage.local.get("rId"),
    type: MESSAGE_TYPE.HEART_BEAT
  };
  post(host, requestBody)
}

async function tokenRegistered(registration_id) {
  await chrome.storage.local.set({"rId": registration_id})
  const host = `${PATHS.base}${PATHS.extension}`
  const requestBody = {
    token: registration_id,
    type: MESSAGE_TYPE.FCM_TOKEN
  };
  post(host, requestBody)
}

chrome.gcm.register(["658613891142"], tokenRegistered)

async function sendMessageToClient(message) {
  const tab = await getCurrentTab(message.data.productSku);
  await chrome.tabs.sendMessage(tab.id, {
    type: MESSAGE_TYPE.CHAT,
    content: message.data.content,
    productSku: message.data.productSku
  });
}

chrome.gcm.onMessage.addListener((message) => {
  if (message.data.type === MESSAGE_TYPE.HEART_BEAT) {
    heartbeatACK()
  } else if (message.data.type === MESSAGE_TYPE.CHAT) {
    sendMessageToClient(message)
    .catch(err => console.log(err));
  } else if (message.data.type === MESSAGE_TYPE.PRODUCT_INFO_REQUEST) {
    const obj = {}
    obj[getProductInfoKey(message.data.productSku)] = {
      name: message.data.name,
      imageUrl: message.data.imageUrl
    }
    chrome.storage.local.set(obj)
  }
})

async function sendProductInfo(productSku) {
  const tab = await getCurrentTab(productSku);
  if (tab) {
    const key = getProductInfoKey(productSku);
    const productInfo = await chrome.storage.local.get(key)
    if (productInfo && productInfo[key]) {
      await chrome.tabs.sendMessage(tab.id, {
        type: MESSAGE_TYPE.PRODUCT_INFO_REQUEST,
        name: productInfo[key]["name"],
        imageUrl: productInfo[key]["imageUrl"]
      });
    } else {
      await sendProductInfoRequest(productSku);
    }
  }
}

chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {
  if (request.type === MESSAGE_TYPE.CHAT) {
    sendQuery(request)
    .catch(err => console.log(err));
    sendResponse({ status: "done" });
  } else if (request.type === MESSAGE_TYPE.PRODUCT_INFO_REQUEST) {
    sendProductInfo(request.productSku)
    .catch(e => console.log(e));
    sendResponse(true)
  } else if (request.type === MESSAGE_TYPE.PAGE_VIEW_EVENT) {
    firePageViewEvent(request.title, request.href);
  } else if (request.type === MESSAGE_TYPE.CLICK_EVENT) {
    fireEvent('click_button', { id: request.id });
  } else if (request.type === MESSAGE_TYPE.PRODUCT_PAGE_LOAD) {
    initProduct(request.url, request.html)
    .catch(e => {});
  }
});

chrome.action.onClicked.addListener( async function(tab) {
  try {
    await chrome.tabs.sendMessage(tab.id, {
      type: MESSAGE_TYPE.TOOLBAR_BUTTON_CLICK
    })
  } catch (e) {
    //
  }
});

