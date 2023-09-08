"use client"

import CodeEditor from "#/ui/editor/editor";
import {useEffect, useState} from "react";
import {usePathname} from "next/navigation";
import {DEBUG, ERROR} from "#/ui/utils/utils";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {useAuthContext} from "#/context/AuthContext";
import {Code, RuntimeApi} from "#/codegen";


import * as React from "react";

export default function Page() {
  const [codeId, setCodeId] = useState('')
  const [userCode, setUserCode] = useState<Code | undefined>(undefined)
  const { authUser } = useAuthContext()
  const pathname = usePathname();


  useEffect(() => {
    const tokens: string[] = pathname.split("/view/")
    setCodeId(tokens[tokens.length - 1])
  }, [])

  useEffect(() => {
    if (codeId) {
      loadUserFunction()
    }
  }, [codeId])

  const loadUserFunction =  () => {
    getAuthToken(authUser).then(token => {
      if (token) {
        // setCreationInProgress(true)
        new RuntimeApi(headerConfig(token))
        .getCodeDetail(codeId, true)
        .then(result => {
          const response : Code = result.data
          DEBUG("User code: ", response)
          setUserCode(response)
        }).catch(e => {
          ERROR(e.message)
        })
      }
    })
  }

  return (
      <div className="w-full">
        <CodeEditor userCode={userCode} setUserCode={setUserCode}/>
      </div>
  );
}