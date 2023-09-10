"use client"
import * as React from "react";
import {Boundary} from "#/ui/boundary";

export default function Page() {
  return (
      <div className="w-full">
        <div className="rounded-lg bg-black p-3.5 lg:p-6">
          <Boundary labels={['explore']} color="pink" animateRerendering={false}>
            <div className="text-vercel-pink space-y-4">
              <h2 className="text-lg text-vercel-pink font-bold">{"Sorry, we couldn't find what you were looking for"}</h2>
              <p className="text-sm">You can go back to find more functions to view</p>
            </div>
          </Boundary>
        </div>
      </div>
  );
}