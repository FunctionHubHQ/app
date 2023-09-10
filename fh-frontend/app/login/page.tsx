'use client'
import * as React from "react";
import {
  GoogleAuthProvider,
  GithubAuthProvider,
  indexedDBLocalPersistence,
  setPersistence,
  signInWithPopup
} from "firebase/auth";
import {auth} from "#/ui/utils/firebase-setup";
import firebase from "firebase/compat";
import AuthProvider = firebase.auth.AuthProvider;
import {DEBUG, ERROR} from "#/ui/utils/utils";
import {useEffect} from "react";
import {useAuthContext} from "#/context/AuthContext";
import {useRouter} from "next/navigation";


export default function Page() {
  const {authUser} = useAuthContext()
  const router = useRouter()

  useEffect(() => {
    if (authUser) router.push("/hub")
  }, [authUser])

  /**
   * Sign in user using Google login pop screen. After login, persist the user
   * object in indexDB so that the session is not terminated if the user refreshes
   * the page or closes the browser.
   */
  const firebaseSignIn = (provider: AuthProvider) => {
    setPersistence(auth, indexedDBLocalPersistence)
    .then(() => {
      // In local persistence will be applied to the signed in Google user
      return signInWithPopup(auth, provider)
      .then((result) => {
        if (result.user) {
          DEBUG("Login Success: ", result.user)
        }
      }).catch(e => {
        ERROR(e.message)
      });
    })
    .catch((e) => {
      ERROR(e.message)
    });
  }

  return (
    <div>

      <section >
        <div
            className="flex flex-col items-center justify-center mx-auto md:h-screen">
          <div
              className="relative h-fit w-96 overflow-auto rounded-lg border border-zinc-800 bg-black px-5 py-16">
            <div className="flex justify-center">
              <div className="flex items-center mb-6 text-2xl font-semibold text-white dark:text-white">
                <img className="w-8 h-8 mr-2"
                     src="/function_hub.png" alt="FunctionHub"/>
                FunctionHub
              </div>

            </div>
            <div className="mt-10">
              <button onClick={() => firebaseSignIn(new GoogleAuthProvider())}
                  className="mx-auto flex h-[46px] w-full items-center justify-center space-x-2 rounded-md bg-zinc-900 p-2 text-zinc-400 transition-colors hover:border-gray-400 hover:bg-gray-50 hover:text-gray-600 focus:outline-none focus:ring-4 focus:ring-gray-400 focus:ring-opacity-25 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:border-gray-200 disabled:hover:bg-transparent disabled:hover:text-gray-500">
                <svg width="30" height="30" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48">
                  <path fill="#FFC107"
                        d="M43.611,20.083H42V20H24v8h11.303c-1.649,4.657-6.08,8-11.303,8c-6.627,0-12-5.373-12-12c0-6.627,5.373-12,12-12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C12.955,4,4,12.955,4,24c0,11.045,8.955,20,20,20c11.045,0,20-8.955,20-20C44,22.659,43.862,21.35,43.611,20.083z"></path>
                  <path fill="#FF3D00"
                        d="M6.306,14.691l6.571,4.819C14.655,15.108,18.961,12,24,12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C16.318,4,9.656,8.337,6.306,14.691z"></path>
                  <path fill="#4CAF50"
                        d="M24,44c5.166,0,9.86-1.977,13.409-5.192l-6.19-5.238C29.211,35.091,26.715,36,24,36c-5.202,0-9.619-3.317-11.283-7.946l-6.522,5.025C9.505,39.556,16.227,44,24,44z"></path>
                  <path fill="#1976D2"
                        d="M43.611,20.083H42V20H24v8h11.303c-0.792,2.237-2.231,4.166-4.087,5.571c0.001-0.001,0.002-0.001,0.003-0.002l6.19,5.238C36.971,39.205,44,34,44,24C44,22.659,43.862,21.35,43.611,20.083z"></path>
                </svg>
                <span>Sign in with Google</span></button>
              <button onClick={() => firebaseSignIn(new GithubAuthProvider())}
                  className="mx-auto mt-2 flex h-[46px] w-full items-center justify-center space-x-2 rounded-md bg-zinc-900 p-2 text-zinc-500 transition-colors hover:border-gray-400 hover:bg-zinc-800 hover:text-zinc-300 focus:outline-none focus:ring-4 focus:ring-gray-400 focus:ring-opacity-25 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:border-gray-200 disabled:hover:bg-transparent disabled:hover:text-gray-500">
                <svg xmlns="http://www.w3.org/2000/svg" width="26" height="26" viewBox="0 0 24 24"
                     className="fill-zinc-300">
                  <path
                      d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"></path>
                </svg>
                <span>Sign in with Github</span></button>
              <button
                  className="mx-auto mt-2 flex h-[46px] w-full items-center justify-center space-x-2 rounded-md bg-zinc-900 p-2 text-zinc-500 transition-colors hover:border-gray-400 hover:bg-zinc-800 hover:text-zinc-300 focus:outline-none focus:ring-4 focus:ring-gray-400 focus:ring-opacity-25 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:border-gray-200 disabled:hover:bg-transparent disabled:hover:text-gray-500">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none"
                     xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-zinc-300">
                  <path
                      d="M5 4.63601C5 3.76031 5.24219 3.1054 5.64323 2.67357C6.03934 2.24705 6.64582 1.9783 7.5014 1.9783C8.35745 1.9783 8.96306 2.24652 9.35823 2.67208C9.75838 3.10299 10 3.75708 10 4.63325V5.99999H5V4.63601ZM4 5.99999V4.63601C4 3.58148 4.29339 2.65754 4.91049 1.99307C5.53252 1.32329 6.42675 0.978302 7.5014 0.978302C8.57583 0.978302 9.46952 1.32233 10.091 1.99162C10.7076 2.65557 11 3.57896 11 4.63325V5.99999H12C12.5523 5.99999 13 6.44771 13 6.99999V13C13 13.5523 12.5523 14 12 14H3C2.44772 14 2 13.5523 2 13V6.99999C2 6.44771 2.44772 5.99999 3 5.99999H4ZM3 6.99999H12V13H3V6.99999Z"
                      fill="currentColor" fillRule="evenodd" clipRule="evenodd"></path>
                </svg>
                <span>Sign in with Microsoft</span></button>

              <button
                  className="mx-auto mt-2 flex h-[46px] w-full items-center justify-center space-x-2 rounded-md bg-zinc-900 p-2 text-zinc-500 transition-colors hover:border-gray-400 hover:bg-zinc-800 hover:text-zinc-300 focus:outline-none focus:ring-4 focus:ring-gray-400 focus:ring-opacity-25 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:border-gray-200 disabled:hover:bg-transparent disabled:hover:text-gray-500">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none"
                     xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-zinc-300">
                  <path
                      d="M5 4.63601C5 3.76031 5.24219 3.1054 5.64323 2.67357C6.03934 2.24705 6.64582 1.9783 7.5014 1.9783C8.35745 1.9783 8.96306 2.24652 9.35823 2.67208C9.75838 3.10299 10 3.75708 10 4.63325V5.99999H5V4.63601ZM4 5.99999V4.63601C4 3.58148 4.29339 2.65754 4.91049 1.99307C5.53252 1.32329 6.42675 0.978302 7.5014 0.978302C8.57583 0.978302 9.46952 1.32233 10.091 1.99162C10.7076 2.65557 11 3.57896 11 4.63325V5.99999H12C12.5523 5.99999 13 6.44771 13 6.99999V13C13 13.5523 12.5523 14 12 14H3C2.44772 14 2 13.5523 2 13V6.99999C2 6.44771 2.44772 5.99999 3 5.99999H4ZM3 6.99999H12V13H3V6.99999Z"
                      fill="currentColor" fillRule="evenodd" clipRule="evenodd"></path>
                </svg>
                <span>Sign in with Twitter</span></button>

              <div className="py-8 text-center">
                <span className="text-xs font-light text-zinc-400">
              <a href="/privacy" className="text-primary-700 underline px-1">Privacy Policy</a> and
              <a href="/terms" className="text-primary-700 underline px-1">Terms of Service</a> apply
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

  );
}
