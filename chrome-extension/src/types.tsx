export interface Chat {
  message?: string,
  from_user: boolean,
  id: string,
  boundary?: boolean
}

export interface ProductInfo {
  name: string,
  imageUrl?: string,
  productUrl?: string
}

export const MESSAGE_TYPE = {
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