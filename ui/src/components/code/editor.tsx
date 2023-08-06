import React, {useEffect, useState} from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { historyField } from '@codemirror/commands';
import { javascript } from '@codemirror/lang-javascript';
import Controls from "@/components/code/controls";
const stateFields = { history: historyField };
import Chat from "@/components/code/chat";
import {auth} from "@/utils/firebase-setup";
import {Code, RuntimeApi} from "@/codegen";
import {headerConfig} from "@/utils/headerConfig";
import {customCmTheme, DEBUG, ERROR} from "@/utils/utils";



function CodeEditor() {
  const [serializedStateMain, setSerializedStateMain] = useState<string>();
  const [serializedStateTest, setSerializedStateTest] = useState<string>();
  const [mainCode, setMainCode] = useState<string>();
  const [testPayload, setTestPayload] = useState<string>();
  const [chatMode, setChatMode] = useState(false)
  const [code, setCode] = useState<Code | null>({})

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
    DEBUG("onCodeChange: ", codeKey, value);

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
            DEBUG(result.data)
            const _code = {...code}
            _code["uid"] = result.data.uid
            setCode(_code);
          }).catch(e => ERROR(e.message))
        }).catch(e => ERROR(e.message))
      }
    })
  }

  const onRunCode = () => {
    auth.onAuthStateChanged(user => {
      if (user) {
        user.getIdTokenResult(false)
        .then(tokenResult => {
          new RuntimeApi(headerConfig(tokenResult.token))
          .exec({
            uid: code?.uid,
            fcm_token: "hello-world!",
            payload: testPayload
          })
          .then(result => {
            ERROR("Exec Request error: ", result.data.error)
            DEBUG("Exec Request error: ", result.data.status)
          }).catch(e => ERROR(e.message))
        }).catch(e => ERROR(e.message))
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

  return (
      <>
        <div className="gf-editor-base gf-controls">
        <Controls onRun={onRunCode}/>
        </div>
        {chatMode ? <Chat/> :
            <>
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
              <div className="gf-editor-base gf-editor">
                <div className="gf-test-tag">
                <span
                    className="bg-purple-100 text-purple-800 text-xs font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-700 dark:text-purple-400 border border-purple-400">Test Request</span>
                </div>
                <div className="gf-test-editor">
                <CodeMirror
                    theme={customCmTheme}
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
              <div className="gf-editor-base gf-error">
                <div>
                  <span
                      className="bg-red-100 text-red-800 text-md font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-900 dark:text-red-400 border border-red-400">Error</span>
                </div>
                  <span>
                    Uncaught Error: There was an error while hydrating. Because the error happened outside of a Suspense boundary, the entire root will switch to client rendering.
          at updateHostRoot (webpack-internal:///(app-client)/./node_modules/next/dist/compiled/react-dom/cjs/react-dom.development.js:15468:57)
          at beginWork$1 (webpack-internal:///(app-client)/./node_modules/next/dist/compiled/react-dom/cjs/react-dom.development.js:17338:14)
          at beginWork (webpack-internal:///(app-client)/./node_modules/next/dist/compiled/react-dom/cjs/react-dom.development.js:25689:14)
          at performUnitOfWork (webpack-internal:///(app-client)/./node_modules/next/dist/compiled/react-dom/cjs/react-dom.development.js:24540:12)
                  </span>
              </div>
              <div className="gf-editor-base gf-stdout">
                <div>
                  <span
                      className="bg-yellow-100 text-yellow-800 text-md font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-900 dark:text-yellow-400 border border-yellow-300">Console</span>
                </div>
                <span>
                  - ready started server on 0.0.0.0:3000, url: http://localhost:3000
      warning ../package.json: No license field
      - event compiled client and server successfully in 377 ms (20 modules)
      - wait compiling...
      - event compiled client and server successfully in 118 ms (20 modules)
      - wait compiling /code/page (client and server)...
      - event compiled client and server successfully in 4.1s (3295 modules)
      - warn
                </span>
              </div>
              <div className="gf-editor-base gf-result">
                <div>
                  <span
                      className="bg-green-100 text-green-800 text-xs font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-900 dark:text-green-400 border border-green-400">Result</span>
                </div>
                <span>
                  {"{\n" +
                      "  \"model\": \"gpt-3.5-turbo\",\n" +
                      "  \"temperature\": 0.7,\n" +
                      "  \"user\": \"u_N6jrfCkk\",\n" +
                      "  \"messages\": [\n" +
                      "    {\n" +
                      "      \"role\": \"user\",\n" +
                      "      \"content\": \"PROMPT: What is the current time and weather in Boston?\\n\\nANSWER:\"\n" +
                      "    },\n" +
                      "    {\n" +
                      "      \"role\": \"function\",\n" +
                      "      \"name\": \"get_city_time_and_weather\",\n" +
                      "      \"content\": {\n" +
                      "        \"location\": \"Boston, MA\",\n" +
                      "        \"temperature\": \"35\",\n" +
                      "        \"forecast\": [\n" +
                      "          \"rainy\",\n" +
                      "          \"windy\"\n" +
                      "        ],\n" +
                      "        \"current_time\": \"August 1st 2023, 9:58:02 pm\"\n" +
                      "      }\n" +
                      "    }\n" +
                      "  ],\n" +
                      "  \"max_tokens\": 1000\n" +
                      "}"}
                </span>
              </div>
          </>
        }
        </>
  );
}
export default CodeEditor;