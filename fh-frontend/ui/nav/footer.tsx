'use client';

import * as React from "react";

export default function Footer() {
  return (
      <footer className="bg-transparent rounded-lg shadow dark:bg-gray-900 mb-2">
        <div className="w-full max-w-full mx-auto md:py-2">
          <div className="sm:flex sm:items-center sm:justify-between">
            <span className="block text-sm text-gray-500 sm:text-center dark:text-gray-400">© 2023 <a
                href="/" className="hover:underline">FunctionHub</a>. All Rights Reserved.</span>
            {/*<a href="/" className="flex items-center mb-2 sm:mb-0">*/}
            {/*  <img src="/function_hub_white.png" className="h-8 mr-1"*/}
            {/*       alt="FunctionHub Logo"/>*/}
            {/*  <span*/}
            {/*      className="self-center text-2xl font-semibold whitespace-nowrap dark:text-white">FunctionHub</span>*/}
            {/*</a>*/}
            <ul className="flex flex-wrap items-center mb-6 text-sm font-medium text-gray-500 sm:mb-0 dark:text-gray-400">
              <li>
                <a href="/about" className="mr-4 hover:underline md:mr-6 ">About</a>
              </li>
              <li>
                <a href="/terms" className="mr-4 hover:underline md:mr-6">Terms</a>
              </li>
              <li>
                <a href="/privacy" className="mr-4 hover:underline md:mr-6 ">Privacy</a>
              </li>
            </ul>
          </div>
          {/*<hr className="my-6 border-gray-200 sm:mx-auto dark:border-gray-700 lg:my-8"/>*/}
          {/*<span className="block text-sm text-gray-500 sm:text-center dark:text-gray-400">© 2023 <a*/}
          {/*    href="https://flowbite.com/" className="hover:underline">Flowbite™</a>. All Rights Reserved.</span>*/}
        </div>
      </footer>
  );
}