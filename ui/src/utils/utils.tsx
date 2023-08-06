'use client'

import moment from "moment";
import {UserApi} from "@/codegen";
import {auth} from "@/utils/firebase-setup";
import {headerConfig} from "@/utils/headerConfig";

export const getFormattedDate = (date: number) => {
  return moment.unix(date).format("llll")
}


// /**
//  * Check if the current user is registered. If not, register them. Once
//  * registration has been verified or completed, disable the progress indicator and
//  * route the user to the dashboard.
//  *
//  */
export const registerUser = () => {
  auth.onAuthStateChanged(user => {
    if (user) {
      user.getIdTokenResult(false)
      .then(tokenResult => {
        new UserApi(headerConfig(tokenResult.token))
        .getUserprofile()
        .then(result => {
          if (result.data.profile) {
            console.log("ACK")
          }
        }).catch(e => console.log(e))
      }).catch(e => console.log(e))
    }
  })
}