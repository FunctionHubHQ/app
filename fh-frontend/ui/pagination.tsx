
import * as React from "react";
import {FC} from "react";
import Link from "next/link";
export interface PaginationProps {
  prev: number,
  next: number
  currentPageRecordCount?: number
}

const Pagination: FC<PaginationProps> = (props) => {

  const getPrevLink = () => {
    if (props.prev > 1) {
      return "/explore/" + props.prev
    }
    return "/explore"
  }

  const getNextLink = () => {
    if (props.next > 1) {
      const allowNextPage = !!props.currentPageRecordCount
      if (allowNextPage) {
        return "/explore/" + props.next
      }
      if (props.prev > 1) {
        return "/explore/" + props.prev
      }
    }
    return "/explore"
  }

  return (
      <nav aria-label="Page navigation example">
        <ul className="inline-flex -space-x-px text-sm">
          <li>
            <Link href={getPrevLink()}
              className="flex items-center justify-center px-3 h-8 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-l-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">Prev</Link>
          </li>
          {/*<li>*/}
          {/*  <a href="#"*/}
          {/*     className="flex items-center justify-center px-3 h-8 leading-tight text-gray-500 bg-white border border-gray-300 hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">1</a>*/}
          {/*</li>*/}
          {/*<li>*/}
          {/*  <a href="#"*/}
          {/*     className="flex items-center justify-center px-3 h-8 leading-tight text-gray-500 bg-white border border-gray-300 hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">2</a>*/}
          {/*</li>*/}
          {/*<li>*/}
          {/*  <a href="#" aria-current="page"*/}
          {/*     className="flex items-center justify-center px-3 h-8 text-blue-600 border border-gray-300 bg-blue-50 hover:bg-blue-100 hover:text-blue-700 dark:border-gray-700 dark:bg-gray-700 dark:text-white">3</a>*/}
          {/*</li>*/}
          {/*<li>*/}
          {/*  <a href="#"*/}
          {/*     className="flex items-center justify-center px-3 h-8 leading-tight text-gray-500 bg-white border border-gray-300 hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">4</a>*/}
          {/*</li>*/}
          {/*<li>*/}
          {/*  <a href="#"*/}
          {/*     className="flex items-center justify-center px-3 h-8 leading-tight text-gray-500 bg-white border border-gray-300 hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">5</a>*/}
          {/*</li>*/}
          <li>
            <Link href={getNextLink()}
               className="disabled flex items-center justify-center px-3 h-8 leading-tight text-gray-500 bg-white border border-gray-300 rounded-r-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">Next</Link>
          </li>
        </ul>
      </nav>
  );
};

export default Pagination;
