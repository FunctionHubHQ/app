'use client'
import * as React from "react";
import ProfileDropdown from "#/ui/profile/profile-menu";
import {useAuthContext} from "#/context/AuthContext";
import FHButton from "#/ui/project/fh-button";
// import SearchBar from "#/ui/search-bar";


const HeaderNav = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = React.useState(false);
  const {authUser} = useAuthContext()
  return (
      <>
        <nav className="bg-[#1e1e1e] dark:bg-gray-900 fixed w-full top-0 left-0 z-50">
          <div className="mx-auto max-w-8xl px-2 sm:px-6 lg:px-8">
            <div className="relative flex h-16 items-center justify-between">
              <div className="absolute inset-y-0 left-0 flex items-center sm:hidden">

                <button type="button" onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                        className="relative inline-flex items-center justify-center rounded-md p-2 text-gray-400 hover:bg-gray-700 hover:text-white focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white">
                  <span className="absolute -inset-0.5"></span>
                  <span className="sr-only">Open main menu</span>

                  <svg className="block h-6 w-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5"
                       stroke="currentColor" aria-hidden="true">
                    <path strokeLinecap="round" strokeLinejoin="round"
                          d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"/>
                  </svg>
                  <svg className="hidden h-6 w-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5"
                       stroke="currentColor" aria-hidden="true">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12"/>
                  </svg>
                </button>
              </div>
              <div
                  className="flex flex-1 items-center justify-center sm:items-stretch sm:justify-start">
                <div className="flex flex-shrink-0 items-center">
                  <a href="/" className="flex items-center mb-2 sm:mb-0">
                    <img src="/function_hub_white.png" className="h-6 mr-1"
                         alt="FunctionHub Logo"/>
                    <span
                        className="self-center text-2xl font-semibold whitespace-nowrap dark:text-white">FunctionHub</span>
                  </a>

                </div>
                <div className="hidden sm:ml-6 sm:block">
                  <div className="flex space-x-4 items-center">
                    {authUser &&
                        <a href="/hub"
                           className="text-gray-300 hover:bg-gray-700 hover:text-white rounded-md px-3 py-2 text-sm font-medium">Dashboard</a>
                    }
                    <a href="/docs"
                       className="text-gray-300 hover:bg-gray-700 hover:text-white rounded-md px-3 py-2 text-sm font-medium">Docs</a>
                    {/*<a href="/examples"*/}
                    {/*   className="text-gray-300 hover:bg-gray-700 hover:text-white rounded-md px-3 py-2 text-sm font-medium">Examples</a>*/}
                    <a href="/explore"
                       className="text-gray-300 hover:bg-gray-700 hover:text-white rounded-md px-3 py-2 text-sm font-medium">Explore</a>
                    <a href="/pricing"
                       className="text-gray-300 hover:bg-gray-700 hover:text-white rounded-md px-3 py-2 text-sm font-medium">Pricing</a>
                    <div>
                    </div>
                  </div>
                </div>
              </div>
              <div
                  className="absolute inset-y-0 right-0 flex items-center pr-2 sm:static sm:inset-auto sm:ml-6 sm:pr-0">

                {authUser ? <div className="relative ml-3">
                      <ProfileDropdown/>
                    </div> :
                    <a href="/login">
                      <FHButton label={'Sign in'}/>
                    </a>
                }
              </div>
            </div>
          </div>

          {mobileMenuOpen &&

              <div className="sm:hidden" id="mobile-menu">
                <div className="space-y-1 px-2 pb-3 pt-2">

                  {authUser &&
                      <a href="/hub"
                         className="text-gray-300 hover:bg-gray-700 hover:text-white block rounded-md px-3 py-2 text-base font-medium">Dashboard</a>
                  }
                  <a href="/docs"
                     className="text-gray-300 hover:bg-gray-700 hover:text-white block rounded-md px-3 py-2 text-base font-medium">Docs</a>
                  {/*<a href="/examples"*/}
                  {/*   className="text-gray-300 hover:bg-gray-700 hover:text-white block rounded-md px-3 py-2 text-base font-medium">Examples</a>*/}
                  <a href="/explore"
                     className="text-gray-300 hover:bg-gray-700 hover:text-white block rounded-md px-3 py-2 text-base font-medium">Explore</a>
                  <a href="/pricing"
                     className="text-gray-300 hover:bg-gray-700 hover:text-white block rounded-md px-3 py-2 text-base font-medium">Pricing</a>
                </div>
              </div>
          }
        </nav>

      </>
  );
};

export default HeaderNav;