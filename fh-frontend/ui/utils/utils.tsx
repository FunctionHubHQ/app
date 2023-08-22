'use client'

import moment from "moment";
import {createTheme} from "@uiw/codemirror-themes";
import {tags as t} from "@lezer/highlight";
import { auth } from "./firebase-setup";
import {UserApi} from "#/codegen";
import {headerConfig} from "#/ui/utils/headerConfig";

export const getFormattedDate = (date: number) => {
  return moment.unix(date).format("llll")
}

export const getCreatedAt = (timestamp: number) => {
  if (timestamp) {
    return moment.unix(timestamp).format("YYYY-MM-DD HH:mm:ss")
  }
  return ''
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

export const customCmTheme = createTheme({
  theme: 'light',
  settings: {
    background: '#f3f3f3',
    foreground: '#75baff',
    caret: '#5d00ff',
    selection: '#036dd626',
    selectionMatch: '#036dd626',
    lineHighlight: '#8a91991a',
    gutterBackground: '#f3f3f3',
    gutterForeground: '#8a919966',
  },
  styles: [
    { tag: [t.standard(t.tagName), t.tagName], color: '#116329' },
    { tag: [t.comment, t.bracket], color: '#6a737d' },
    { tag: [t.className, t.propertyName], color: '#6f42c1' },
    { tag: [t.variableName, t.attributeName, t.number, t.operator], color: '#005cc5' },
    { tag: [t.keyword, t.typeName, t.typeOperator, t.typeName], color: '#d73a49' },
    { tag: [t.string, t.meta, t.regexp], color: '#032f62' },
    { tag: [t.name, t.quote], color: '#22863a' },
    { tag: [t.heading], color: '#24292e', fontWeight: 'bold' },
    { tag: [t.emphasis], color: '#24292e', fontStyle: 'italic' },
    { tag: [t.deleted], color: '#b31d28', backgroundColor: 'ffeef0' },
    { tag: [t.atom, t.bool, t.special(t.variableName)], color: '#e36209' },
    { tag: [t.url, t.escape, t.regexp, t.link], color: '#032f62' },
    { tag: t.link, textDecoration: 'underline' },
    { tag: t.strikethrough, textDecoration: 'line-through' },
    { tag: t.invalid, color: '#cb2431' },
  ],
});

export const DEBUG = (...args: any[]) => {
  if (process.env.NODE_ENV !== "production") {
    console.debug(args)
  }
}

export const ERROR = (...args: any[]) => {
  if (process.env.NODE_ENV !== "production") {
    console.error(args)
  }
}