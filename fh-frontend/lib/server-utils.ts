import { getCookies } from 'cookies-next';
import { headers } from "next/headers";
import {ProjectApi} from "#/codegen";
import {headerConfig} from "#/ui/utils/headerConfig";
export const PAGE_LIMIT = 10;

export const getExploreResult = async (currPage: number) => {

  try {
    const token = getAuthToken()
    const response = await new ProjectApi(
        headerConfig(token))
    .getAllPublicFunctions({
      page_num: currPage,
      limit: PAGE_LIMIT
    })
    return response.data
  } catch (e) {
    console.log("error")
  }
  return {}
}

function getAuthToken() {
  // @ts-ignore
  let token = parseCookies()["token"]
  if (!token) {
    // Set the public api key
    token = 'fh-anon-YmaGJfzBpgjWbgWiphfF7RfGNirNtQT7UJ2Ig5jB2SffW7'
  }
  return token
}
function parseCookies () {
  const headersList = headers();
  const list = {};
  const cookieHeader = headersList.get("Cookie") || headersList.get("cookie");
  if (!cookieHeader) return list;

  cookieHeader.split(`;`).forEach(function(cookie) {
    let [ name, ...rest] = cookie.split(`=`);
    name = name?.trim();
    if (!name) return;
    const value = rest.join(`=`).trim();
    if (!value) return;
    // @ts-ignore
    list[name] = decodeURIComponent(value);
  });

  return list;
}