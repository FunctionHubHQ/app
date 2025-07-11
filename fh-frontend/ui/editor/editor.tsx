"use client"
import React, {FC, useEffect, useState} from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { historyField } from '@codemirror/commands';
import { javascript } from '@codemirror/lang-javascript';
const stateFields = { history: historyField };
import { vscodeDark } from '@uiw/codemirror-theme-vscode';
import {BiGitCommit} from 'react-icons/bi';
import {BsRocketTakeoff} from 'react-icons/bs';
import UseAnimations from "react-useanimations";
import loading from 'react-useanimations/lib/loading';
import {
  Code,
  RuntimeApi
} from "#/codegen";
import {ANONYMOUS_USER_API_KEY, getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {DEBUG, ERROR} from "#/ui/utils/utils";
import SandboxTabs from "#/ui/interactive-api/sandboxTabs";
import DeployModal from "#/ui/editor/deploy-modal";
import {useAuthContext} from "#/context/AuthContext";

export interface CodeEditorProps {
  userCode?: Code
  showEditButtons?: boolean,
  setUserCode: (userCode: Code) => void
}

const CodeEditor: FC<CodeEditorProps> = (props) => {
  const [serializedStateMain, setSerializedStateMain] = useState<string>();
  const [mainCode, setMainCode] = useState<string>();
  const [codeCommitInProgress, setCodeCommitInProgress] = useState(false)
  const [deployInProgress, setDeployInProgress] = useState(false)
  const [apiKey, setApiKey] = useState<string | undefined>(undefined)
  const [bearerToken, setBearerToken] = useState<string | undefined>(undefined)
  const [functionSlug, setFunctionSlug] = useState<string | undefined>(undefined)
  const [version, setVersion] = useState<string | undefined>(undefined)
  const [deploymentMessage, setDeploymentMessage] = useState<string | undefined>(undefined)
  const [showDeploymentModal, setShowDeploymentModal] = useState(false)
  const [deploymentFailed, setDeploymentFailed] = useState(false)
  const { authUser, fhUser } = useAuthContext()

  const isOwner = () => {
    return authUser && fhUser ? fhUser.user_id === props.userCode?.owner_id : false
  }
  const getCodeKey = () => {
    return  "main_" + props.userCode?.code_id as string
  }

  const getStateKey = () => {
    return 'editorState_' + props.userCode?.code_id as string
  }

  const onCodeChange = (value: string, viewUpdate: any) => {
    const codeKey = getCodeKey()
    const stateKey = getStateKey();
    setMainCode(value)
    localStorage.setItem(codeKey, value);

    const state = viewUpdate.state.toJSON(stateFields);
    localStorage.setItem(stateKey, JSON.stringify(state));
  }

  const onDeploy = () => {
    setDeployInProgress(true)
    getAuthToken(authUser).then(token => {
      if (token && typeof mainCode === "string") {
        new RuntimeApi(headerConfig(token))
        .deploy({
          code_id: props.userCode?.code_id
        })
        .then(result => {
          DEBUG("Deploy Response: ", result.data)
          if (result.data?.status === "ok") {
            setDeploymentMessage("Your function has been deployed!")
            setShowDeploymentModal(true)
            setDeploymentFailed(false)
          } else if (result.data.error) {
            setDeploymentMessage(result.data.error)
            setShowDeploymentModal(true)
            setDeploymentFailed(true)
          } else {
            setDeploymentMessage(undefined)
            setShowDeploymentModal(false)
            setDeploymentFailed(false)
          }
          setDeployInProgress(false)
        }).catch(e => {
          setDeployInProgress(false)
          ERROR(e.message)
        })
      }
    })
  }

  const onCommit =  () => {
    setCodeCommitInProgress(true)
    setFunctionSlug(undefined)
    setVersion(undefined)
    getAuthToken(authUser).then(token => {
      if (token) {
        if (typeof mainCode === "string") {
          new RuntimeApi(headerConfig(token))
          .updateCode({
            code_id: props.userCode?.code_id,
            code: btoa(mainCode),
            fields_to_update: ["code"]
          })
          .then(result => {
            const _code = {...props.userCode}
            DEBUG("Update Response: ", result.data)
            _code["code_id"] = result.data.code_id
            setFunctionSlug(result.data.slug)
            setVersion(result.data.version)
            props.setUserCode(_code)
            setCodeCommitInProgress(false)
          }).catch(e => {
            setCodeCommitInProgress(false)
            ERROR(e.message)
          })
        }
      }
    })
  }

  useEffect(() => {
    initState()
  }, [props.userCode])

  const initState = () => {
    setSerializedStateMain(localStorage.getItem(getStateKey()) || '')
    if (props.userCode) {
      // TODO: could ORing between local storage and server response cause a problem?
      setMainCode(localStorage.getItem(getCodeKey()) || atob(props.userCode?.code || ''));
      setFunctionSlug(props.userCode.function_slug)
      setVersion(props.userCode.version)
    }
    setTokens()
  }

  const setTokens = () => {
    if (authUser) {
      setApiKey(fhUser?.api_key)
      getAuthToken(authUser).then(token => {
        if (token) {
          setBearerToken(token)
        }
      })
    } else {
      setApiKey(ANONYMOUS_USER_API_KEY)
      setBearerToken(ANONYMOUS_USER_API_KEY)
    }
  }

  // @ts-ignore
  return (
      <div className="mx-auto max-w-full m-auto grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1 ">
        <div className="tile col-span-1">
          <div>
                <div className="gf-editor-base gf-editor">

                  <div className="w-full ">
                    {props.showEditButtons && isOwner() ?
                    <div className="flex space-x-1 rounded-xl bg-[#1e1e1e] p-1">
                      <div
                          className='w-full rounded-lg py-2 text-sm font-medium leading-5 text-blue-700'
                      >
                        <div className="w-full flex justify-between items-center p-2">

                          <button 
                              onClick={onCommit}
                              type="button"
                                  className="text-white bg-[#4285F4] hover:bg-[#4285F4]/90 focus:ring-4 focus:outline-none focus:ring-[#4285F4]/50 font-medium rounded-lg text-sm px-5 py-1 text-center inline-flex items-center dark:focus:ring-[#4285F4]/55 mr-2">
                            <div className="flex items-center gap-2">
                              {
                                codeCommitInProgress ? <UseAnimations animation={loading} size={16} strokeColor={"white"}/> :
                                    <BiGitCommit style={{marginRight: "0.5rem"}}/>
                              }
                              Commit
                            </div>
                          </button>

                          
                          <button
                              onClick={onDeploy}
                              type="button"
                              className="text-white bg-red-700 hover:bg-red-800 focus:ring-4 focus:outline-none focus:ring-[#4285F4]/50 font-medium rounded-lg text-sm px-5 py-1 text-center inline-flex items-center dark:focus:ring-[#4285F4]/55 mr-2">
                            <div className="flex items-center gap-2">
                              {
                                deployInProgress ? <UseAnimations animation={loading} size={16} strokeColor={"white"}/> :
                                    <BsRocketTakeoff style={{marginRight: "0.5rem"}}/>
                              }
                              Deploy
                            </div>
                          </button>
                          <DeployModal
                              deploymentFailed={deploymentFailed}
                              message={deploymentMessage}
                              isOpen={showDeploymentModal}
                              onClose={() => setShowDeploymentModal(false)}
                          />
                        </div>
                      </div>
                    </div> :
                        <div className="pt-6"></div>
                    }
                    <CodeMirror
                        className="cm-outer-container"
                        placeholder={"Write your code here..."}
                        theme={vscodeDark}
                        extensions={[javascript({ typescript: true })]}
                        value={mainCode}
                        initialState={
                          serializedStateMain
                              ? {
                                json: JSON.parse(serializedStateMain || ''),
                                fields: stateFields,
                              }
                              : undefined
                        }
                        onChange={(value, valueUpdate) => onCodeChange(value, valueUpdate)}
                    />
                  </div>
                </div>
          </div>
        </div>
        <div className="tile col-span-1">
          <div>
            <SandboxTabs
                functionSlug={functionSlug}
                version={version}
                apiKey={apiKey}
                bearerToken={bearerToken}
                isOwner={isOwner()}
            />
          </div>
        </div>
      </div>
  );
}
export default CodeEditor;