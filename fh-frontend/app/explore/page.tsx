import React from "react";
import ExploreView from "#/ui/explore/explore-view";
import {getExploreResult} from "#/lib/server-utils";
import { headers } from "next/headers";

export default async function Page() {
  const headersList = headers();
  console.log("Headers: ", headersList)
  return (
      <ExploreView data={await getExploreResult(0)} prev={1} next={2}/>
  );
}
