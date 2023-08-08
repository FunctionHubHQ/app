import React, {useEffect, useState} from "react";
import {usePathname} from "next/navigation";
import {DEBUG} from "@/utils/utils";

const Route2LabelMap = {
  "/": "Home",
  "/projects": "Projects",
  "/projects/code": "Code"
};

export function BreadCrumbs() {
  const pathname = usePathname()

  const [crumbs, setCrumbs] = useState([]);

  useEffect(() => {

    const segmentsPath = pathname.split("/");
    const segmentsRoute = pathname.split("/");
    const crumbLinks = CombineAccumulatively(segmentsPath);
    const crumbLabels = CombineAccumulatively(segmentsRoute);

    const crumbs = crumbLinks.map((link: any, index: string | number) => {
      const route = crumbLabels[index];
      const crumb = {
        link: link,
        route: route,
        label: Route2LabelMap[route] || route,
      };
      return crumb;
    });
    setCrumbs(crumbs);
    //
    DEBUG({
      segmentsPath,
      segmentsRoute,
      crumbLinks,
      crumbLabels,
      crumbs,
    });
  }, [pathname]);

  return (
      <div className="w-full flex gap-1 mt-10" aria-label="Breadcrumb">
        <ol className="inline-flex items-center space-x-1 md:space-x-3">
          {crumbs.map((c, i) => {
            if (c.label === "Home") {
              return (
                  <li className="inline-flex items-center" key={i}>
                    <a href={c.link}
                       className="inline-flex items-center text-sm font-medium text-gray-700 hover:text-blue-600 dark:text-gray-400 dark:hover:text-white">
                      <svg className="w-3 h-3 mr-2.5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg"
                           fill="currentColor" viewBox="0 0 20 20">
                        <path
                            d="m19.707 9.293-2-2-7-7a1 1 0 0 0-1.414 0l-7 7-2 2a1 1 0 0 0 1.414 1.414L2 10.414V18a2 2 0 0 0 2 2h3a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h3a2 2 0 0 0 2-2v-7.586l.293.293a1 1 0 0 0 1.414-1.414Z"/>
                      </svg>
                      {c.label}
                    </a>
                  </li>
              )
            } else if (i < crumbs.length - 1) {
              return (
                  <li className="inline-flex items-center" key={i}>
                    <div className="flex items-center">
                      <svg className="w-3 h-3 text-gray-400 mx-1" aria-hidden="true"
                           xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 6 10">
                        <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round"
                              strokeWidth="2" d="m1 9 4-4-4-4"/>
                      </svg>
                      <a href={c.link}
                         className="ml-1 text-sm font-medium text-gray-700 hover:text-blue-600 md:ml-2 dark:text-gray-400 dark:hover:text-white">{c.label}</a>
                    </div>
                  </li>
              )
            } else {
              return (
                  <li className="inline-flex items-center" aria-current="page" key={i}>
                    <div className="flex items-center">
                      <svg className="w-3 h-3 text-gray-400 mx-1" aria-hidden="true"
                           xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 6 10">
                        <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round"
                              strokeWidth="2" d="m1 9 4-4-4-4"/>
                      </svg>
                      <span
                          className="ml-1 text-sm font-medium text-gray-500 md:ml-2 dark:text-gray-400">{c.label}</span>
                    </div>
                  </li>
              )
            }
          })
          }
        </ol>
      </div>


  );
}

function CombineAccumulatively(segments) {
  /*
  when segments = ['1','2','3']
  returns ['1','1/2','1/2/3']
  */
  const links = segments.reduce((acc, cur, curIndex) => {
    const last = curIndex > 1 ? acc[curIndex - 1] : "";
    const newPath = last + "/" + cur;
    acc.push(newPath);
    return acc;
  }, []);
  return links;
}