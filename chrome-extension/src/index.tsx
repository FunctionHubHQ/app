import 'react-app-polyfill/ie11';

import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import {MESSAGE_TYPE} from "./types";

async function onProductPageLoad() {
  await chrome?.runtime?.sendMessage({
    type: MESSAGE_TYPE.PRODUCT_PAGE_LOAD,
    html: document.documentElement.outerHTML,
    url: window.location.href
  });
}
export async function init() {
  const href = window.location.href
  if (href.includes("amazon.")) {
    if (href.includes("/dp/")) {
      await onProductPageLoad();
    }

    // Create div wrapper
    const body = document.body;
    const bodyWrapper = document.createElement('div');
    bodyWrapper.id = 'original-body-wrapper';
    bodyWrapper.className = 'h-full w-full overflow-auto relative ease-in-out duration-300';

    // Move the body's children into this wrapper
    while (body.firstChild) {
      bodyWrapper.appendChild(body.firstChild);
    }

    bodyWrapper.style.overflow = 'auto';
    bodyWrapper.style.height = '100vh';

    // Append the wrapper to the body
    body.style.overflow = 'hidden';
    body.style.margin = '0';
    body.appendChild(bodyWrapper);

    // create react app
    const app = document.createElement('div');
    app.id = 'shoppiem-root';

    body.appendChild(app);
    const root = createRoot(app!);
    root.render(<App/>);
  }
}

init()
.catch(e => console.log(e));
