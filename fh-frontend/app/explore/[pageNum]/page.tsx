import React from "react";
import { headers } from "next/headers";
import ExploreView from "#/ui/explore/explore-view";
import {getExploreResult, PAGE_LIMIT} from "#/lib/server-utils";

export default async function Page() {
  const headersList = headers();
  const activePath = headersList.get("x-invoke-path");
  const tokens = activePath?.split("/")
  let parsedPage: string
  try {
    if (tokens) {
      parsedPage = tokens[tokens.length - 1]
    } else {
      parsedPage = "1"
    }
  } catch (e) {
    parsedPage = "1"
  }
  const currPage = Number.parseInt(parsedPage)
  const prev = currPage - 1
  const next = currPage + 1

  return (
      <ExploreView data={await getExploreResult(currPage)} prev={prev} next={next}/>
  );
}
