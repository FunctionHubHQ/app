import {Chat} from "../../types";

export const loadHistory = async (productSku: string) => {
  try {
    // @ts-ignore
    const result = await chrome.storage.local.get(productSku)
    if (result && result[productSku]) {
      const history = JSON.parse(result[productSku])
      return history ? history : []
    }
  } catch (e) {
    console.log(e)
  }
  return []
}

export const saveHistory = async function (productSku: string, newHistory: Chat[]) {
  try {
    const obj = {}
    // @ts-ignore
    obj[productSku] = JSON.stringify(newHistory)

    // @ts-ignore
    await chrome?.storage?.local?.set(obj)

  } catch (e) {
    console.log(e)
  }
}