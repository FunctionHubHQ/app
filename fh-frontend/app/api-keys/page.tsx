'use client'
import {Boundary} from "#/ui/boundary";
import AddButton from "#/ui/project/add-button";
import {useEffect, useState} from "react";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {ApiKey, ApiKeyResponse, FHFunction, UserApi} from "#/codegen";
import {ERROR, getCreatedAt} from "#/ui/utils/utils";
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

  const onGenerateKey = async (isVendorKey: boolean) => {
    if (isVendorKey && !vendorKeyValue) {
      return
    }
    const token = await getAuthToken(user)
    if (token) {
      new UserApi(headerConfig(token))
      .upsertApiKey({
        key: isVendorKey ? vendorKeyValue : null,
        is_vendor_key: isVendorKey
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

  const onDeleteKey = async (key: string) => {
    const token = await getAuthToken(user)
    if (token) {
      new UserApi(headerConfig(token))
      .deleteKey({
        key: key
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

  const getVendorKey = (): ApiKey => {
    return  apiKeys?.find(it => it.is_vendor_key)
  }


  const onChange = (e) => {
    e.preventDefault()
    setVendorKeyValue(e.target.value)
  }

  return (
    <div className=" max-w-none ">
      <div className="py-8">
        <Boundary labels={['functionhub keys']}>
          <div className="relative overflow-x-auto shadow-md sm:rounded-lg bg-red">
            <table className="w-full text-sm text-left text-gray-500 dark:text-gray-400">
              <tbody>
              {
                apiKeys.filter(it => !it.is_vendor_key).map(apiKey => {
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
                          <DeleteButton onClick={() => onDeleteKey(apiKey.key as string)} label={"Delete Key"}
                                        addClass={false}/>
                        </td>
                      </tr>
                  )
                })
              }
              </tbody>
            </table>
          </div>
          <AddButton onClick={() => onGenerateKey(false)} label={"Generate Key"}/>
        </Boundary>

      </div>
      <div className="py-8">
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
            <AddButton onClick={() => onGenerateKey(true)} label={"Add Key"}/>
          </> :

            <div className="relative overflow-x-auto shadow-md sm:rounded-lg">
              <table className="w-full text-sm text-left text-gray-500 dark:text-gray-400">
                <tbody>
                <tr>
                  <th scope="row"
                      className="px-6 py-4 text-lg text-gray-400 whitespace-nowrap dark:text-white">
                    {getVendorKey().key}
                  </th>
                  <td>
                    {getCreatedAt(getVendorKey().created_at as number)}
                  </td>
                  <td className="px-6 py-4">
                    <DeleteButton onClick={onDeleteVendorKey} label={"Delete Key"} addClass={false}/>
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
