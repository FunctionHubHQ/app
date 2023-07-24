import React, { ReactElement, useEffect, useState } from 'react';
import { nanoid } from 'nanoid'
import 'rsuite/dist/rsuite.min.css';
import CloseIcon from '@rsuite/icons/Close';
import { Drawer } from 'rsuite';
import {Chat, MESSAGE_TYPE, ProductInfo} from "./types";
import ChatInput from "./components/chat/input";
import ChatMessages from "./components/chat/messages";
import {loadHistory, saveHistory} from "./components/chat/utils";
import FloatingButton from "./components/logo/FloatingButton";
import ProductCard from "./components/product/card";
import FullLogo from "./components/logo/FullLogo";
import Hero from './components/product/hero';
import { Placeholder } from 'rsuite';

export default function App(): ReactElement {
  const [open, setOpen] = useState(false);
  const [chatHistory, setChatHistory] = useState<Chat[]>([])
  const [productMetadata, setProductMetadata] = useState<ProductInfo | undefined>(undefined)
  const [productSku, setProductSku] = useState("")
  const [showBubbleAnimation, setShowBubbleAnimation] = useState(false)
  const [rawMessage, setRawMessage] = useState('')
  const [listenersInitialized, setListenersInitialized] = useState(false)
  const [initialChatHistoryLoaded, setInitialChatHistoryLoaded] = useState(false)
  const boundaryId = nanoid()

  useEffect(() => {
    // Fire a page view event on load
    window.addEventListener('load', () => {
      (async () => {
        // @ts-ignore
        await chrome?.runtime?.sendMessage({
          type: MESSAGE_TYPE.PAGE_VIEW_EVENT,
          title: document.title,
          href: document.location.href
        });
      })();
    });

    // Listen globally for all button events
    document.addEventListener('click', (event) => {
      if (event.target instanceof HTMLButtonElement) {
        (async () => {
          // @ts-ignore
          await chrome?.runtime?.sendMessage({
            type: MESSAGE_TYPE.CLICK_EVENT,
            id: event.target.id
          });
        })();
      }
    });
  }, [])

  useEffect(() => {
    if (productSku) {
      loadHistory(productSku).then(result => {
        setChatHistory(result)
        setInitialChatHistoryLoaded(true)
      }).catch(err => console.log(err))
    }
  }, [productSku])

  useEffect(() => {
    if (initialChatHistoryLoaded && chatHistory.length === 0 && productMetadata?.name) {
      if (!!chatHistory) {
        addToChatHistory(`What would you like to know about <strong>${productMetadata.name}</strong>?`, false)
        .catch(err => console.log(err));
      }
    }
  }, [initialChatHistoryLoaded, productMetadata])

  useEffect(() => {
    if (!productSku) {
     setProductSku(getProductSku())
      if (!productMetadata?.name) {
        parseProductInfo()
      }
    }
  })

  const parseProductInfo = () => {
    const html = document.documentElement.outerHTML
    const parser = new DOMParser()
    try {
      const soup = parser.parseFromString(html, "text/html");
      const title = soup.getElementById("title").textContent.trim();
      const productUrl = window.location.href
      const imageUrl = soup.querySelector("#landingImage").src
      setProductMetadata({
        name: title,
        imageUrl: imageUrl
      })
    } catch (e) {
    }
  }

  const getProductSku = () => {
    const href = window.location.href
    if (href.includes("/dp/")) {
      const url = new URL(window.location.href)
      return url.pathname.split("/dp/")[1].split("/")[0]
    }
  }

  useEffect(() => {
    // Keep messaging the service worker until we have the product metadata
   if (!productMetadata && productSku) {
       // @ts-ignore
       chrome?.runtime?.sendMessage({
         type: MESSAGE_TYPE.PRODUCT_INFO_REQUEST,
         productSku: productSku
       }).catch(e => {})
   }
  })

  useEffect(() => {
    if (!listenersInitialized) {
      setListenersInitialized(true)
      // @ts-ignore
      chrome?.runtime?.onMessage?.addListener(function (request, sender, sendResponse) {
        if (request.type === MESSAGE_TYPE.PRODUCT_INFO_REQUEST && !productMetadata?.name) {
          setProductMetadata({
            name: request.name,
            imageUrl: request.imageUrl
          })
          sendResponse(true)
        } else if (request.type === MESSAGE_TYPE.CHAT) {
          addToChatHistory(request.content, false, request.productSku)
          .catch(err => console.log(err));
          setShowBubbleAnimation(false)
        } else if (request.type === MESSAGE_TYPE.TOOLBAR_BUTTON_CLICK) {
          handleOpen()
        }
      });
    }
  })

  useEffect(() => {
    // @ts-ignore
    chrome?.runtime?.onMessage?.addListener(function (request, sender, sendResponse) {
      if (request.type === MESSAGE_TYPE.TOOLBAR_BUTTON_CLICK) {
        handleOpen()
      }
    });
  }, [])

  const handleRawMessageChange = (value: string) => {
    setRawMessage(value)
  }

  const handleSubmit = () => {
    if (rawMessage && rawMessage.trim().length > 0) {
      (async () => {
        // @ts-ignore
        await chrome?.runtime?.sendMessage({
          type: MESSAGE_TYPE.CHAT,
          query: rawMessage,
          productSku: productSku
        });
      })()
      setShowBubbleAnimation(true)
      addToChatHistory(rawMessage, true)
      .catch(err => console.log(err));
      setRawMessage('')
    }
  }

  const addToChatHistory = async (message: string, fromUser: boolean, sku: string = undefined) => {
    // Chrome listener events have an empty context, i.e. the productSku state is empty
    // so we must use the sku from the event sent by the server, if available
    const _productSku = sku ? sku : productSku
    const prevHistory = await loadHistory(_productSku)
    const newHistory = [...prevHistory,
      {
        message,
        from_user: fromUser,
        id: nanoid()
      } ]
    await saveHistory(_productSku, newHistory)
    setChatHistory(newHistory)
  }

  const handleOpen = () => {
    setOpen(!open);
  };

  // @ts-ignore
  return (<div>
        <Drawer size={"xs"} placement={"right"} open={open} onClose={() => setOpen(false)} className="z-max b">
      <Drawer.Header closeButton={false}>
        <Drawer.Title>
          <FullLogo/>
        </Drawer.Title>
        <Drawer.Actions>
          <CloseIcon onClick={() => setOpen(false)} className="close-btn"/>
        </Drawer.Actions>
      </Drawer.Header>
        {productMetadata?.name && productMetadata?.imageUrl &&
          <Drawer.Header style={{marginTop: "1rem"}}>
            <ProductCard productMetadata={productMetadata}/>
            {/*<Placeholder.Paragraph rows={3} graph="image" active />*/}
          </Drawer.Header>
        }
      <Drawer.Body style={{bottom: 0}}>
        <Hero/>
        {productSku &&
          <ChatMessages
              chatHistory={chatHistory}
              boundaryId={boundaryId}
          />
        }
      </Drawer.Body>
      <Drawer.Footer>
        {productSku && productMetadata?.name && productMetadata?.imageUrl &&
          <ChatInput
              handleSubmit={handleSubmit}
              handleRawMessageChange={handleRawMessageChange}
              showBubbleAnimation={showBubbleAnimation}
              rawMessage={rawMessage}
          />
        }
      </Drawer.Footer>
    </Drawer>
        <FloatingButton
            handleOpen={handleOpen}
            showBadge={productMetadata?.name && productMetadata?.imageUrl}
        />
  </div>
  )
}
