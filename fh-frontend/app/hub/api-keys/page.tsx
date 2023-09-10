'use client'
import {Boundary} from "#/ui/boundary";
import AddButton from "#/ui/project/add-button";
import React, {useEffect, useState} from "react";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {ApiKey, ApiKeyProvider, ApiKeyResponse, UserApi} from "#/codegen";
import {ERROR, getCreatedAt} from "#/ui/utils/utils";
import {useAuthContext} from "#/context/AuthContext";
import DeleteButton from "#/ui/project/delete-button";

import CodeMirror from '@uiw/react-codemirror';
import { historyField } from '@codemirror/commands';
import { javascript } from '@codemirror/lang-javascript';
const stateFields = { history: historyField };
import { vscodeDark } from '@uiw/codemirror-theme-vscode';
import FHButton from "#/ui/project/fh-button";

export default function Page() {
  const [providerKeyValue, setProviderKey] = useState<string | undefined>()
  const [apiKeys, setApiKeys] = useState<Array<ApiKey>>([])
  const [openAiKey, setOpenAiKey] = useState<ApiKey | undefined>(undefined)
  const [serializedStateMain, setSerializedStateMain] = useState<string>();
  const [envVariableCode, setEnvVariableCode] = useState<string>();
  const [invalidJsonError, setInvalidJsonError] = useState<string | undefined>(undefined)
  const [envVariablesEditable, setEnvVariablesEditable] = useState(true)
  const { authUser } = useAuthContext()

  useEffect(() => {
    initKeys()
    .catch(e => ERROR(e))
  }, [])

  const onGenerateOrInsertKey = async (provider: ApiKeyProvider) => {
    const token = await getAuthToken(authUser)
    let key
    if (provider === ApiKeyProvider.OpenAi && providerKeyValue && providerKeyValue.length > 0) {
      key = providerKeyValue
    }
    if (token) {
      new UserApi(headerConfig(token))
      .upsertApiKey({
        key: key,
        provider: provider
      })
      .then(result => {
        const response: ApiKeyResponse = result.data
        setApiKeys(response.keys as Array<ApiKey>)
      }).catch(e => ERROR(e))
    }
  }

  const onDeleteKey = async (key: string | undefined, provider: ApiKeyProvider) => {
    const token = await getAuthToken(authUser)
    if (token) {
      new UserApi(headerConfig(token))
      .deleteKey({
        key: key,
        provider: provider
      })
      .then(result => {
        const response: ApiKeyResponse = result.data
        setApiKeys(response.keys as Array<ApiKey>)
      }).catch(e => ERROR(e))
    }
  }

  const getCodeKey = () => {
    return  'main_env_variables'
  }

  const getStateKey = () => {
    return 'editorState_env_variables'
  }

  const onEnvVariableJsonChange = (value: string, viewUpdate: any) => {
    setInvalidJsonError(undefined)
    const codeKey = getCodeKey()
    const stateKey = getStateKey();
    setEnvVariableCode(value)
    localStorage.setItem(codeKey, value);

    const state = viewUpdate.state.toJSON(stateFields);
    localStorage.setItem(stateKey, JSON.stringify(state));
  }

  const initKeys = async () => {
    const token = await getAuthToken(authUser)
    if (token) {
      new UserApi(headerConfig(token))
      .getApiKeys()
      .then(result => {
        const response: ApiKeyResponse = result.data
        const apiKeys = response.keys as Array<ApiKey>
        setApiKeys(apiKeys)
      }).catch(e => ERROR(e))

      new UserApi(headerConfig(token))
      .getEnvVariables()
      .then(response => {
        initEnvVariableCode(response.data)
      }).catch(e => ERROR(e))
    }
  }

  const initEnvVariableCode = (encodedVal?: string) => {
    setEnvVariablesEditable(false)
    if (encodedVal) {
      const decodedVal = atob(encodedVal)
      try {
        const json = JSON.parse(decodedVal)
        setEnvVariableCode(JSON.stringify(json, null, 2))
      } catch (e) {
        ERROR(e)
      }
    }
  }

  useEffect(() => {
    if (apiKeys) {
      const openAiKeys = getProviderKeys(ApiKeyProvider.OpenAi)
      if (openAiKeys) {
        setOpenAiKey(openAiKeys[0])
      }
    }
  }, [apiKeys])


  const getProviderKeys = (provider: ApiKeyProvider): Array<ApiKey> => {
    return apiKeys?.filter(it => it.provider === provider)
  }

  const onSaveEnvVariables = () => {
    if (!envVariableCode) return
    try {
      JSON.parse(envVariableCode) // validate json
      getAuthToken(authUser).then(token => {
        if (token) {
          new UserApi(headerConfig(token))
          .upsertEnvVariables(btoa(envVariableCode))
          .then(response => {
            initEnvVariableCode(response.data)
          }).catch(e => ERROR(e))
        }
      })
    } catch (e) {
      setInvalidJsonError(e.message)
    }
  }

  const enableEnvVariableEdit = () => {
    setEnvVariableCode(undefined)
    setEnvVariablesEditable(true)
  }


  const onProviderKeyChange = (e: any) => {
    e.preventDefault()
    // Currently there is only one provider, OpenAI, so we don't need to make further
    // distinctions. 
    setProviderKey(e.target.value)
  }

  return (
    <div className=" max-w-none ">
      <div className="py-8">
        <Boundary labels={['functionhub keys']}>
          <div className="relative overflow-x-auto shadow-md sm:rounded-lg bg-red">
            <table className="w-full text-sm text-left text-gray-500 dark:text-gray-400">
              <tbody>
              {

                getProviderKeys(ApiKeyProvider.FunctionHub)?.map(apiKey => {
                  return (
                      <tr key={apiKey.key}>
                        <th scope="row"
                            className="px-6 py-4 text-xxs text-gray-400 whitespace-nowrap dark:text-white">
                          {apiKey.key}
                        </th>
                        <td>
                          {getCreatedAt(apiKey.created_at as number)}
                        </td>
                        <td className="px-6 py-4">
                          <DeleteButton onClick={() => onDeleteKey(apiKey.key, ApiKeyProvider.FunctionHub)} label={"Delete Key"}
                                        addClass={false}/>
                        </td>
                      </tr>
                  )
                })
              }
              </tbody>
            </table>
          </div>
          <AddButton onClick={() => onGenerateOrInsertKey(ApiKeyProvider.FunctionHub)} label={"Generate Key"}/>
        </Boundary>
      </div>
      <div className="py-8">
        <Boundary labels={['env variables']} color={'pink'}>
          <>
            <span className="flex text-white text-sm w-full">Add your environment variables here as a JSON. You can access them in your code from Hub.env.</span>
            <div className="mb-6 py-4">
              <CodeMirror
                  editable={envVariablesEditable}
                  className="cm-outer-container"
                  placeholder={'{"ENV_VARIABLE": "env variable"}'}
                  theme={vscodeDark}
                  extensions={[javascript({ typescript: true })]}
                  value={envVariableCode}
                  initialState={
                    serializedStateMain
                        ? {
                          json: JSON.parse(serializedStateMain || ''),
                          fields: stateFields,
                        }
                        : undefined
                  }
                  onChange={(value, valueUpdate) => onEnvVariableJsonChange(value, valueUpdate)}
              />
              {invalidJsonError && <span className="flex text-red-500 w-full text-sm py-2">{'Invalid JSON: ' + invalidJsonError}</span> }
            </div>
            {envVariablesEditable ? <FHButton onClick={onSaveEnvVariables} label={"Save"}/> :
                <FHButton onClick={enableEnvVariableEdit} label={"Edit"}/>
            }
          </>
        </Boundary>
      </div>
      <div className="py-8">
      <Boundary labels={['openai key']} color={'blue'}>
        {!openAiKey ?
          <>
            <span className="flex text-white text-sm w-full">Add your OpenAI API key below. We will use your key in development and production functions whenever you make a GPT completion call. For more information, please refer to our documentation on Provider Keys</span>
            <div className="mb-6 py-4">
              <input type="text" id="default-input"
                     onChange={onProviderKeyChange}
                     className="bg-gray-900 border border-gray-300 text-white text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"/>
              {/*<span className="flex text-gray-500 w-full text-xxs py-2"></span>*/}
            </div>
            <AddButton onClick={() => onGenerateOrInsertKey(ApiKeyProvider.OpenAi)} label={"Add Key"}/>
          </> :

            <div className="relative overflow-x-auto shadow-md sm:rounded-lg">
              <table className="w-full text-sm text-left text-gray-500 dark:text-gray-400">
                <tbody>
                <tr>
                  <th scope="row"
                      className="px-6 py-4 text-lg text-gray-400 whitespace-nowrap dark:text-white">
                    {openAiKey.key}
                  </th>
                  <td>
                    {getCreatedAt(openAiKey.created_at as number)}
                  </td>
                  <td className="px-6 py-4">
                    <DeleteButton onClick={() => onDeleteKey(undefined, ApiKeyProvider.OpenAi)} label={"Delete Key"} addClass={false}/>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
        }
      </Boundary>
      </div>
    </div>
  );
}
