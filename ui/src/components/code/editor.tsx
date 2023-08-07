import React, {useEffect, useState} from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { historyField } from '@codemirror/commands';
import { javascript } from '@codemirror/lang-javascript';
import {githubLight} from "@uiw/codemirror-theme-github";
import Controls from "@/components/code/controls";
const stateFields = { history: historyField };
import Chat from "@/components/code/chat";
import {auth} from "@/utils/firebase-setup";
import {Code, ExecResultAsync, RuntimeApi} from "@/codegen";
import {headerConfig} from "@/utils/headerConfig";
import {customCmTheme, DEBUG, ERROR} from "@/utils/utils";



function CodeEditor() {
  const [serializedStateMain, setSerializedStateMain] = useState<string>();
  const [serializedStateTest, setSerializedStateTest] = useState<string>();
  const [mainCode, setMainCode] = useState<string>();
  const [testPayload, setTestPayload] = useState<string>();
  const [chatMode, setChatMode] = useState(false)
  const [code, setCode] = useState<Code | null>({})
  const [execResult, setExecResult] = useState<ExecResultAsync | null>(null)
  const [codeRunInProgress, setCodeRunInProgress] = useState(false)

  const getCodeKey = (prefix: string) => {
    return  prefix +' _codeValue';
  }

  const getStateKey = (prefix: string) => {
    return prefix + '_editorState'
  }

  const onCodeChange = (value: string, viewUpdate: any, keyPrefix: string) => {
    const codeKey = getCodeKey(keyPrefix)
    const stateKey = getStateKey(keyPrefix);
    if (keyPrefix === "main") {
      setMainCode(value)
      onUpdateCode(value)
    } else {
      setTestPayload(value)
    }

    localStorage.setItem(codeKey, value);

    const state = viewUpdate.state.toJSON(stateFields);
    localStorage.setItem(stateKey, JSON.stringify(state));
  }

  const onUpdateCode = (rawCode: string) => {
    auth.onAuthStateChanged(user => {
      if (user) {
        user.getIdTokenResult(false)
        .then(tokenResult => {
          new RuntimeApi(headerConfig(tokenResult.token))
          .updateCode({
            uid: code?.uid,
            user_id: user.uid,
            code: btoa(rawCode),
            fields_to_update: ["code"]
          })
          .then(result => {
            const _code = {...code}
            _code["uid"] = result.data.uid
            setCode(_code);
          }).catch(e => ERROR(e.message))
        }).catch(e => ERROR(e.message))
      }
    })
  }

  const onRunCode = () => {
    if (codeRunInProgress) {
      return;
    }
    setCodeRunInProgress(true)
    setExecResult(null);
    auth.onAuthStateChanged(user => {
      if (user) {
        user.getIdTokenResult(false)
        .then(tokenResult => {
          new RuntimeApi(headerConfig(tokenResult.token))
          .exec({
            uid: code?.uid,
            payload: testPayload
          })
          .then(result => {
            setExecResult(result.data)
            setCodeRunInProgress(false)
            DEBUG("Exec result: ", result.data)
          }).catch(e => {
            ERROR(e.message)
            setCodeRunInProgress(false)
          })
        }).catch(e => {
          ERROR(e.message)
          setCodeRunInProgress(false)
        })
      }
    })
  }

  useEffect(() => {
    // todo: need to use code uid as key
    initState()
  }, [])

  const initState = () => {
    setSerializedStateMain(localStorage.getItem(getStateKey('main')) || '')
    setMainCode(localStorage.getItem(getCodeKey('main')) || '');
    setSerializedStateTest(localStorage.getItem(getStateKey('test')) || '')
    setTestPayload(localStorage.getItem(getCodeKey('test')) || '');
  }

  const getResult = () => {
    let result = execResult?.result
    if (execResult?.result) {
      try {
        result = JSON.stringify(JSON.parse(JSON.parse(execResult.result)), null, 2)
      } catch (e) {
        ERROR(e)
      }
    }
    return result;
  }

  return (
      <div className="container m-auto grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-4">
        <div className="tile col-span-1">
          <div>
            <div className="gf-editor-base gf-controls">
              <Controls
                  onRun={onRunCode}
                  codeRunInProgress={codeRunInProgress}
              />
            </div>
            {chatMode ? <Chat/> :
                <div className="gf-editor-base gf-editor">
                  <CodeMirror
                      theme={customCmTheme}
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
                      onChange={(value, valueUpdate) => onCodeChange(value, valueUpdate, "main")}
                  />
                </div>
            }
          </div>
        </div>

        <div className="tile col-span-1">
          <div>
            <div className="gf-editor-base gf-controls">
              <div className="flex justify-between pt-4 pb-4 ml-3 mr-3">
                <div>
                  <div>
                  <span
                      className="bg-purple-100 text-purple-800 text-xs font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-700 dark:text-purple-400 border border-purple-400">Request Payload</span>
                  </div>
                </div>
              </div>
              <div className="gf-test-editor">
                <CodeMirror
                    theme={customCmTheme}
                    height={"200px"}
                    extensions={[javascript({ typescript: true })]}
                    value={testPayload}
                    initialState={
                      serializedStateTest
                          ? {
                            json: JSON.parse(serializedStateTest || ''),
                            fields: stateFields,
                          }
                          : undefined
                    }
                    onChange={(value, valueUpdate) => onCodeChange(value, valueUpdate, "test")}
                />
              </div>
            </div>
            {execResult?.error &&
                <div className="gf-editor-base gf-error">
                  <div>
                <span
                    className="bg-red-100 text-red-800 text-md font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-900 dark:text-red-400 border border-red-400">Error</span>
                  </div>
                  <span>{execResult.error}</span>
                </div>
            }
            {execResult?.std_out_str &&
                <div className="gf-editor-base gf-stdout">
                  <div>
                <span
                    className="bg-yellow-100 text-yellow-800 text-md font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-900 dark:text-yellow-400 border border-yellow-300">Console</span>
                  </div>
                  <CodeMirror
                      theme={githubLight}
                      extensions={[javascript({ typescript: true })]}
                      value={execResult.std_out_str}
                  />
                </div>
            }
            {execResult?.result && execResult.result !== "null" &&
                <div className="gf-editor-base gf-result">
                  <div>
                <span
                    className="bg-green-100 text-green-800 text-xs font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-900 dark:text-green-400 border border-green-400">Result</span>
                  </div>
                  <CodeMirror
                      theme={githubLight}
                      value={getResult()}
                  />
                  <span>
              </span>
                </div>
            }
          </div>
        </div>
      </div>
  );
}
export default CodeEditor;