'use client'
import * as React from "react";
import {
  GoogleAuthProvider,
  GithubAuthProvider,
  indexedDBLocalPersistence,
  setPersistence,
  signInWithPopup
} from "firebase/auth";
import {auth} from "@/utils/firebase-setup";
import firebase from "firebase/compat";
import AuthProvider = firebase.auth.AuthProvider;
import {registerUser} from "@/utils/utils";

function Login() {

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
          registerUser()
        }
      }).catch(e => {
        console.log(e.message)
      });
    })
    .catch((e) => {
      console.log(e)
    });
  }

  return (
      <section className="bg-gray-50 dark:bg-gray-900">
        <div className="flex flex-col items-center justify-center px-6 py-8\2 mx-auto md:h-screen lg:py-0">
          <div className="relative container m-auto px-60 text-gray-500 md:px-12 xl:px-40">
            <div className="m-auto md:w-8/12 lg:w-6/12 xl:w-4/12">
              <div className="rounded-xl bg-white shadow-xl">
                <div className="p-6 sm:p-16">
                  <div className="space-y-4">
                    <img src="https://tailus.io/sources/blocks/social/preview/images/icon.svg"
                         loading="lazy" className="w-10" alt="tailus logo"/>
                      <h2 className="mb-8 text-2xl text-cyan-900 font-bold">Sign in to FunctionHub</h2>
                  </div>
                  <div className="mt-16 grid space-y-4">
                    <button onClick={() => firebaseSignIn(new GoogleAuthProvider())}
                        className="group h-12 px-6 border-2 border-gray-300 rounded-full transition duration-300
 hover:border-blue-400 focus:bg-blue-50 active:bg-blue-100">
                      <div className="relative flex items-center space-x-4 justify-center">
                        <img src="https://tailus.io/sources/blocks/social/preview/images/google.svg"
                             className="absolute left-0 w-5" alt="google logo"/>
                          <span
                              className="block w-max font-semibold tracking-wide text-gray-700 text-sm transition duration-300 group-hover:text-blue-600 sm:text-base">Continue with Google</span>
                      </div>
                    </button>
                    <button
                        onClick={() => firebaseSignIn(new GithubAuthProvider())}
                        className="group h-12 px-6 border-2 border-gray-300 rounded-full transition duration-300
 hover:border-blue-400 focus:bg-blue-50 active:bg-blue-100">
                      <div className="relative flex items-center space-x-4 justify-center">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="currentColor"
                             className="absolute left-0 w-5 text-gray-700" viewBox="0 0 16 16">
                          <path
                              d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.012 8.012 0 0 0 16 8c0-4.42-3.58-8-8-8z"/>
                        </svg>
                        <span
                            className="block w-max font-semibold tracking-wide text-gray-700 text-sm transition duration-300 group-hover:text-blue-600 sm:text-base">Continue with Github</span>
                      </div>
                    </button>
                    {/*<button className="group h-12 px-6 border-2 border-gray-300 rounded-full transition duration-300*/}
                    {/*                 hover:border-blue-400 focus:bg-blue-50 active:bg-blue-100">*/}
                    {/*  <div className="relative flex items-center space-x-4 justify-center">*/}
                    {/*    <img*/}
                    {/*        src="https://upload.wikimedia.org/wikipedia/en/0/04/Facebook_f_logo_%282021%29.svg"*/}
                    {/*        className="absolute left-0 w-5" alt="Facebook logo"/>*/}
                    {/*      <span*/}
                    {/*          className="block w-max font-semibold tracking-wide text-gray-700 text-sm transition duration-300 group-hover:text-blue-600 sm:text-base">Continue with Facebook</span>*/}
                    {/*  </div>*/}
                    {/*</button>*/}
                  </div>

                  {/*<div className="mt-32 space-y-4 text-gray-600 text-center sm:-mb-8">*/}
                  {/*  <p className="text-xs">By proceeding, you agree to our <a href="#"*/}
                  {/*                                                            className="underline">Terms*/}
                  {/*    of Use</a> and confirm you have read our <a href="#" className="underline">Privacy*/}
                  {/*    and Cookie Statement</a>.</p>*/}
                  {/*  <p className="text-xs">This site is protected by reCAPTCHA and the <a href="#"*/}
                  {/*                                                                        className="underline">Google*/}
                  {/*    Privacy Policy</a> and <a href="#" className="underline">Terms of*/}
                  {/*    Service</a> apply.</p>*/}
                  {/*</div>*/}
                </div>
              </div>
            </div>
          </div>
        </div>
        {/*<div*/}
        {/*    className="flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0">*/}
        {/*  <a href="#"*/}
        {/*     className="flex items-center mb-6 text-2xl font-semibold text-gray-900 dark:text-white">*/}
        {/*    <img className="w-8 h-8 mr-2"*/}
        {/*         src="https://flowbite.s3.amazonaws.com/blocks/marketing-ui/logo.svg" alt="logo"/>*/}
        {/*      Flowbite*/}
        {/*  </a>*/}
        {/*  <div*/}
        {/*      className="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0 dark:bg-gray-800 dark:border-gray-700">*/}
        {/*    <div className="p-6 space-y-4 md:space-y-6 sm:p-8">*/}
        {/*      <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl dark:text-white">*/}
        {/*        Sign in to your account*/}
        {/*      </h1>*/}
        {/*      <form className="space-y-4 md:space-y-6" action="#">*/}
        {/*        <div>*/}
        {/*          <label htmlFor="email"*/}
        {/*                 className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Your*/}
        {/*            email</label>*/}
        {/*          <input type="email" name="email" id="email"*/}
        {/*                 className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"*/}
        {/*                 placeholder="name@company.com" required=""/>*/}
        {/*        </div>*/}
        {/*        <div>*/}
        {/*          <label htmlFor="password"*/}
        {/*                 className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Password</label>*/}
        {/*          <input type="password" name="password" id="password" placeholder="••••••••"*/}
        {/*                 className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"*/}
        {/*                 required=""/>*/}
        {/*        </div>*/}
        {/*        <div className="flex items-center justify-between">*/}
        {/*          <div className="flex items-start">*/}
        {/*            <div className="flex items-center h-5">*/}
        {/*              <input id="remember" aria-describedby="remember" type="checkbox"*/}
        {/*                     className="w-4 h-4 border border-gray-300 rounded bg-gray-50 focus:ring-3 focus:ring-primary-300 dark:bg-gray-700 dark:border-gray-600 dark:focus:ring-primary-600 dark:ring-offset-gray-800"*/}
        {/*                     required=""/>*/}
        {/*            </div>*/}
        {/*            <div className="ml-3 text-sm">*/}
        {/*              <label htmlFor="remember" className="text-gray-500 dark:text-gray-300">Remember*/}
        {/*                me</label>*/}
        {/*            </div>*/}
        {/*          </div>*/}
        {/*          <a href="#"*/}
        {/*             className="text-sm font-medium text-primary-600 hover:underline dark:text-primary-500">Forgot*/}
        {/*            password?</a>*/}
        {/*        </div>*/}
        {/*        <button type="submit"*/}
        {/*                className="w-full text-white bg-primary-600 hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800">Sign*/}
        {/*          in*/}
        {/*        </button>*/}
        {/*        <p className="text-sm font-light text-gray-500 dark:text-gray-400">*/}
        {/*          Don’t have an account yet? <a href="#"*/}
        {/*                                        className="font-medium text-primary-600 hover:underline dark:text-primary-500">Sign*/}
        {/*          up</a>*/}
        {/*        </p>*/}
        {/*      </form>*/}
        {/*    </div>*/}
        {/*  </div>*/}
        {/*</div>*/}
      </section>
  )
}
export default Login