import {ProjectApi} from "#/codegen";
export const PAGE_LIMIT = 10;
export const getExploreResult = async (currPage: number) => {
  try {
    const response = await new ProjectApi().getAllPublicFunctions({
      page_num: currPage,
      limit: PAGE_LIMIT
    })
    return response.data
  } catch (e) {
    console.log("error")
  }
  return {}
}