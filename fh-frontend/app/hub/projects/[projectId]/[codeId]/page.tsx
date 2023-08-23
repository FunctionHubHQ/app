"use client"
import {Boundary} from "#/ui/boundary";
import AddProjectButton from "#/ui/project/add-project-button";
// import {useEffect, useState} from "react";
import CreateProjectModal from "#/ui/project/create-project-modal";
// import {DEBUG, ERROR} from "#/ui/utils/utils";
import { ProjectApi, Projects} from "#/codegen";
// import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
// import {useAuthContext} from "#/context/AuthContext";
import ProjectMenu from "#/ui/project/project-menu";


import CodeEditor from "#/ui/editor/editor";
import {useEffect, useState} from "react";
import {usePathname} from "next/navigation";
import {DEBUG, ERROR} from "#/ui/utils/utils";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {useAuthContext} from "#/context/AuthContext";
import {Code, CodeUpdateResult, RuntimeApi} from "#/codegen";



import * as React from "react";

export default function Page() {
  const [codeId, setCodeId] = useState('')
  const [userCode, setUserCode] = useState<Code | undefined>(undefined)
  const { user } = useAuthContext()
  const pathname = usePathname();
  // alert("Child has loaded...: " + pathname)

  useEffect(() => {
    const tokens: string[] = pathname.replace("edit:", "").split("/")
    setCodeId(tokens[tokens.length - 1])
  }, [])

  useEffect(() => {
    if (codeId) {
      loadUserFunction()
    }
  }, [codeId])

  const loadUserFunction =  () => {
    getAuthToken(user).then(token => {
      if (token) {
        // setCreationInProgress(true)
        new RuntimeApi(headerConfig(token))
        .getCodeDetail(codeId)
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