'use client'
import {Boundary} from "#/ui/boundary";
import AddButton from "#/ui/project/add-button";
import {useEffect, useState} from "react";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {ApiKey, ApiKeyResponse, FHFunction, FHFunctions, ProjectApi, UserApi} from "#/codegen";
import {ERROR} from "#/ui/utils/utils";
import {useAuthContext} from "#/context/AuthContext";
import DeleteButton from "#/ui/project/delete-button";

export default function Page() {
  const [vendorKeyValue, setVendorKeyValue] = useState()
  const [apiKeys, setApiKeys] = useState<Array<ApiKey>>([])
  const { user } = useAuthContext()

  useEffect(() => {
    initKeys()
    .catch(e => ERROR(e))
  }, [])

  const onAddVendorKey = async () => {
    const token = await getAuthToken(user)
    if (token) {
      new UserApi(headerConfig(token))
      .upsertApiKey({
        key: vendorKeyValue,
        is_vendor_key: true
      })
      .then(result => {
        const response: ApiKeyResponse = result.data
        setApiKeys(response.keys as Array<FHFunction>)
      }).catch(e => ERROR(e))
    }
  }

  const onDeleteVendorKey = async () => {
    const token = await getAuthToken(user)
    if (token) {
      new UserApi(headerConfig(token))
      .deleteKey({
        is_vendor_key: true
      })
      .then(result => {
        const response: ApiKeyResponse = result.data
        setApiKeys(response.keys as Array<FHFunction>)
      }).catch(e => ERROR(e))
    }
  }

  const initKeys = async () => {
    const token = await getAuthToken(user)
    if (token) {
      new UserApi(headerConfig(token))
      .getApiKeys()
      .then(result => {
        const response: ApiKeyResponse = result.data
        setApiKeys(response.keys as Array<FHFunction>)
      }).catch(e => ERROR(e))
    }
  }

  const getVendorKey = (): string => {
    const key = apiKeys?.find(it => it.is_vendor_key)?.key
    if (key) {
      return key
    }
    return 'KEY NOT FOUND'
  }

  const onChange = (e) => {
    e.preventDefault()
    setVendorKeyValue(e.target.value)
  }

  return (
    <div className="prose prose-sm prose-invert max-w-none">
      <Boundary labels={['openai key']} color={'blue'}>
        {(!apiKeys || !apiKeys.filter(it => it.is_vendor_key).length) ?
          <>
            <span className="flex text-gray-500 w-full">You don't have a key here. Please add one below</span>
            <div className="mb-6 py-4">
              <input type="text" id="default-input"
                     onChange={onChange}
                     className="bg-gray-900 border border-gray-300 text-white text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"/>
              <span className="flex text-gray-500 w-full text-xxs py-2">All your API keys are encrypted at rest</span>
            </div>
            <AddButton onClick={onAddVendorKey} label={"Add Key"}/>
          </> :
            <>
              <div className="mb-6 py-4">
                <span className="flex text-gray-500 w-full text-lg py-2">{getVendorKey()}</span>
              </div>
              <DeleteButton onClick={onDeleteVendorKey} label={"Delete Key"}/>
            </>
        }
      </Boundary>
      {/*<h1 className="text-xl font-bold">Error Handling</h1>*/}

      {/*<ul>*/}
      {/*  <li>*/}
      {/*    <code>error.js</code> defines the error boundary for a route segment*/}
      {/*    and the children below it. It can be used to show specific error*/}
      {/*    information, and functionality to attempt to recover from the error.*/}
      {/*  </li>*/}
      {/*  <li>*/}
      {/*    Trying navigation pages and triggering an error inside nested layouts.*/}
      {/*    Notice how the error is isolated to that segment, while the rest of*/}
      {/*    the app remains interactive.*/}
      {/*  </li>*/}
      {/*</ul>*/}

      {/*<div className="flex gap-2">*/}
      {/*  <BuggyButton />*/}

      {/*  <ExternalLink href="https://nextjs.org/docs/app/building-your-application/routing/error-handling">*/}
      {/*    Docs*/}
      {/*  </ExternalLink>*/}
      {/*  <ExternalLink href="https://github.com/vercel/app-playground/tree/main/app/error-handling">*/}
      {/*    Code*/}
      {/*  </ExternalLink>*/}
      {/*</div>*/}
    </div>
  );
}
