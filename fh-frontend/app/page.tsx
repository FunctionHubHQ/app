'use client'
import {useAuthContext} from "#/context/AuthContext";
import FHButton from "#/ui/project/fh-button";
import Link from "next/link";

export default function Page() {
  const { authUser } = useAuthContext()

  const onSignIn = () => {

  }

  return (
      <section className=" dark:bg-gray-900">
        <div className="py-8 px-4 mx-auto max-w-screen-xl lg:py-16 lg:px-6">
          <div className="mx-auto max-w-screen-md text-left mb-8 lg:mb-12">
            <h2 className="mb-4 text-5xl font-extrabold text-white dark:text-white">Build powerful AI applications that can call
              <span className="ml-2 italic text-vercel-pink">any</span> API</h2>
            <div className="grid grid-flow-col justify-stretch items-center">
              {!authUser &&
                  <div className="pb-4">
                    <Link href="/login">
                    <FHButton label="Sign In"/>
                    </Link>
                  </div>
              }
              <div className="ml-2">
                <p className="mb-5 font-light text-gray-500 text-md dark:text-gray-400">
                  FunctionHub lets you write, test, and deploy serverless functions and
                  integrate them into GPT-powered applications.</p>
              </div>
            </div>


          </div>
          <div className="space-y-8 lg:grid md:grid-cols-2 sm:gap-2 xl:gap-2 lg:space-y-0">
            <div>
              <img src="/demo/code.png" alt={"FunctionHub code demo"}/>
            </div>
            <div>
              <img src="/demo/code.png" alt={"FunctionHub code demo"}/>
            </div>


            {/*<div*/}
            {/*    className="flex flex-col p-6 mx-auto max-w-lg text-center text-gray-900 bg-white rounded-lg border border-gray-100 shadow dark:border-gray-600 xl:p-8 dark:bg-gray-800 dark:text-white">*/}
            {/*  <h3 className="mb-4 text-2xl font-semibold">Free</h3>*/}
            {/*  <p className="font-light text-gray-500 sm:text-lg dark:text-gray-400">Best option for*/}
            {/*    personal use & for your next project.</p>*/}
            {/*  <div className="flex justify-center items-baseline my-8">*/}
            {/*    <span className="mr-2 text-5xl font-extrabold">$29</span>*/}
            {/*    <span className="text-gray-500 dark:text-gray-400">/month</span>*/}
            {/*  </div>*/}



            {/*  <ul role="list" className="mb-8 space-y-4 text-left">*/}
            {/*    <li className="flex items-center space-x-3">*/}

            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>Individual configuration</span>*/}
            {/*    </li>*/}
            {/*    <li className="flex items-center space-x-3">*/}



            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>No setup, or hidden fees</span>*/}
            {/*    </li>*/}
            {/*    <li className="flex items-center space-x-3">*/}



            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>Team size: <span className="font-semibold">1 developer</span></span>*/}
            {/*    </li>*/}
            {/*    <li className="flex items-center space-x-3">*/}



            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>Premium support: <span className="font-semibold">6 months</span></span>*/}
            {/*    </li>*/}
            {/*    <li className="flex items-center space-x-3">*/}



            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>Free updates: <span className="font-semibold">6 months</span></span>*/}
            {/*    </li>*/}
            {/*  </ul>*/}
            {/*  <a href="#"*/}
            {/*     className="text-white bg-primary-600 hover:bg-primary-700 focus:ring-4 focus:ring-primary-200 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:text-white  dark:focus:ring-primary-900">Get*/}
            {/*    started</a>*/}
            {/*</div>*/}

            {/*<div*/}
            {/*    className="flex flex-col p-6 mx-auto max-w-lg text-center text-gray-900 bg-white rounded-lg border border-gray-100 shadow dark:border-gray-600 xl:p-8 dark:bg-gray-800 dark:text-white">*/}
            {/*  <h3 className="mb-4 text-2xl font-semibold">Premium</h3>*/}
            {/*  <p className="font-light text-gray-500 sm:text-lg dark:text-gray-400">Relevant for*/}
            {/*    multiple users, extended & premium support.</p>*/}
            {/*  <div className="flex justify-center items-baseline my-8">*/}
            {/*    <span className="mr-2 text-5xl font-extrabold">$99</span>*/}
            {/*    <span className="text-gray-500 dark:text-gray-400 dark:text-gray-400">/month</span>*/}
            {/*  </div>*/}



            {/*  <ul role="list" className="mb-8 space-y-4 text-left">*/}
            {/*    <li className="flex items-center space-x-3">*/}


            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>Individual configuration</span>*/}
            {/*    </li>*/}
            {/*    <li className="flex items-center space-x-3">*/}


            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>No setup, or hidden fees</span>*/}
            {/*    </li>*/}
            {/*    <li className="flex items-center space-x-3">*/}


            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>Team size: <span className="font-semibold">10 developers</span></span>*/}
            {/*    </li>*/}
            {/*    <li className="flex items-center space-x-3">*/}

            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>Premium support: <span className="font-semibold">24 months</span></span>*/}
            {/*    </li>*/}
            {/*    <li className="flex items-center space-x-3">*/}

            {/*      <svg className="flex-shrink-0 w-5 h-5 text-green-500 dark:text-green-400"*/}
            {/*           fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">*/}
            {/*        <path fillRule="evenodd"*/}
            {/*              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"*/}
            {/*              clipRule="evenodd"></path>*/}
            {/*      </svg>*/}
            {/*      <span>Free updates: <span className="font-semibold">24 months</span></span>*/}
            {/*    </li>*/}
            {/*  </ul>*/}
            {/*  <a href="#"*/}
            {/*     className="text-white bg-primary-600 hover:bg-primary-700 focus:ring-4 focus:ring-primary-200 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:text-white  dark:focus:ring-primary-900">Get*/}
            {/*    started</a>*/}
            {/*</div>*/}
          </div>


          <div className="mt-4">
            <span className="flex text-xxs text-gray-400 pb-1">1. Wall time is the total </span>
            <span className="flex text-xxs text-gray-400 pb-1">1. Wall time is the total </span>
          </div>



        </div>
      </section>
  );
}
