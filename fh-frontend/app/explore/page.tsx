import React from "react";
import ExploreView from "#/ui/explore/explore-view";
import {getExploreResult} from "#/lib/server-utils";

export default async function Page() {
  return (
      <ExploreView data={await getExploreResult(0)} prev={1} next={2}/>
  );
}
