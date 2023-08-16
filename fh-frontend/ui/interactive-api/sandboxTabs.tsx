'use client'
import React, {FC, useEffect, useState} from 'react'
import { Tab } from '@headlessui/react'
import Toggle from "#/ui/interactive-api/toggle";
import Sandbox from "#/ui/interactive-api/sandbox";
import {auth} from "#/ui/utils/firebase-setup";
import {SpecApi} from "#/codegen";
import {headerConfig} from "#/ui/utils/headerConfig";
import {BASE_PATH} from "#/codegen/base";
import {DEBUG, ERROR} from "#/ui/utils/utils";
import UseAnimations from "react-useanimations";
import loading from "react-useanimations/lib/loading";
// import Toggle from "@/components/interactive-api/toggle";
// import Sandbox, {SandboxProps} from "@/components/interactive-api/sandbox";

function classNames(...classes) {
  return classes.filter(Boolean).join(' ')
}

export interface SandboxTabProps {
  functionSlug?: string,
  version?: string,
  apiKey?: string
  bearerToken?: string
}

const SandboxTabs: FC<SandboxTabProps> = (props) => {
  const [devBaseSpecUrl, setDevBaseSpecUrl] = useState<string | undefined>(undefined)
  const [prodBaseSpecUrl, setProdBaseSpecUrl] = useState<string | undefined>(undefined)
  const [devGptEnabled, setDevGptEnabled] = useState(false)
  const [prodGptEnabled, setProdGptEnabled] = useState(false)
  const [envs]= useState({
    dev: {
      name: 'Dev',
      id: 'fhd'
    },
    prod: {
      name: 'Prod',
      id: 'fhp'
    },
    devGpt: {
      name: 'Dev',
      id: 'gd'
    },
    prodGpt: {
      name: 'Prod',
      id: 'gp'
    }
  })

  useEffect(() => {
    initBaseSpecUrls()
  }, [props.functionSlug, props.version])


  const initBaseSpecUrls = () => {
    if (props.functionSlug && props.version) {
      if (!devBaseSpecUrl) {
        onSetBaseSpecUrl(props.functionSlug, props.version, false)
        .catch(e => ERROR(e.message))
      }
      if (!prodBaseSpecUrl) {
        onSetBaseSpecUrl(props.functionSlug, props.version, true)
        .catch(e => ERROR(e.message))
      }
    } else {
      setDevBaseSpecUrl(undefined)
      setProdBaseSpecUrl(undefined)
    }
  }

  const onSetBaseSpecUrl = async (functionSlug: string | undefined, version: string | undefined, deployed: boolean) => {
    DEBUG("onSetBaseSpecUrl")
    const tokenResult = await auth.currentUser?.getIdTokenResult(false)
    const api = new SpecApi(headerConfig(tokenResult.token))
    const response = await api
    .getSpecStatus({
      slug: functionSlug,
      version: version,
      deployed: deployed
    })
    if (response?.data?.is_ready) {
        const _specUrl = BASE_PATH + `/spec/${functionSlug}/${version}`
        DEBUG("Spec URL: ", _specUrl)
      if (deployed) {
        setProdBaseSpecUrl(_specUrl)
      } else {
        setDevBaseSpecUrl(_specUrl)
      }
    } else {
      await onSetBaseSpecUrl(functionSlug, version, deployed)
    }
  }

  const buildFullSpecUrl = (env) => {
    if (env === envs.dev.name) {
      let id = envs.dev.id
      if (devGptEnabled) {
        id = envs.devGpt.id
      }
      return `${devBaseSpecUrl}/${props.bearerToken}/${id}`
    } else if (env === envs.prod.name) {
      let id = envs.prod.id
      if (prodGptEnabled) {
        id = envs.prodGpt.id
      }
      return `${devBaseSpecUrl}/${props.apiKey}/${id}`
    }
  }

  const onDevToggleChange = (enabled: boolean) => {
    setDevGptEnabled(enabled)
  }

  const onProdToggleChange = (enabled: boolean) => {
    setProdGptEnabled(enabled)
  }

  return (
      <div className="w-full ">
        <Tab.Group>
          <Tab.List className="flex space-x-1 rounded-xl bg-blue-900/20 p-1">
            <Tab
                onClick={initBaseSpecUrls}
                key={envs.dev.id}
                className={({ selected }) =>
                    classNames(
                        'w-full rounded-lg py-2.5 text-sm font-medium leading-5 text-blue-700',
                        'ring-white ring-opacity-60 ring-offset-2 ring-offset-blue-400 focus:outline-none focus:ring-2',
                        selected
                            ? 'bg-white shadow'
                            : 'text-blue-100 hover:bg-white/[0.12] hover:text-white'
                    )
                }
            >
              <div className="w-full flex justify-center items-center">
                <div className="w-full">{envs.dev.name}</div>
                <div className="mr-2">
                  <Toggle onClick={onDevToggleChange}/>
                </div>
              </div>
            </Tab>
            <Tab
                onClick={initBaseSpecUrls}
                key={envs.prod.id}
                className={({ selected }) =>
                    classNames(
                        'w-full rounded-lg py-2.5 text-sm font-medium leading-5 text-blue-700',
                        'ring-white ring-opacity-60 ring-offset-2 ring-offset-blue-400 focus:outline-none focus:ring-2',
                        selected
                            ? 'bg-white shadow'
                            : 'text-blue-100 hover:bg-white/[0.12] hover:text-white'
                    )
                }
            >
              <div className="w-full flex justify-center items-center">
                <div className="w-full">{envs.prod.name}</div>
                <div className="mr-2">
                  <Toggle onClick={onProdToggleChange}/>
                </div>
              </div>
            </Tab>
          </Tab.List>
          <Tab.Panels className="mt-2">
            <Tab.Panel
                key={envs.dev.name}
                className={classNames(
                    'rounded-xl p-3',
                    'ring-white ring-opacity-60 ring-offset-2 ring-offset-blue-400 focus:outline-none focus:ring-2'
                )}
            >
              {
                devBaseSpecUrl ?  <Sandbox
                  specUrl={buildFullSpecUrl(envs.dev.name)}
                  bearerToken={props.bearerToken}
              /> : <div className="flex h-screen">
                      <div className="m-auto">
                        <UseAnimations animation={loading} size={16} strokeColor={"white"}/>
                      </div>
                    </div>
              }
            </Tab.Panel>
            <Tab.Panel
                key={envs.prod.name}
                className={classNames(
                    'rounded-xl p-3',
                    'ring-white ring-opacity-60 ring-offset-2 ring-offset-blue-400 focus:outline-none focus:ring-2'
                )}
            >
              {
                prodBaseSpecUrl ? <Sandbox
                    specUrl={buildFullSpecUrl(envs.prod.name)}
                    apiKey={props.apiKey}
                /> : <div className="flex h-screen">
                      <div className="m-auto">
                        <UseAnimations animation={loading} size={16} strokeColor={"white"}/>
                      </div>
                    </div>
              }

            </Tab.Panel>
          </Tab.Panels>
        </Tab.Group>
      </div>
  )
}
export default SandboxTabs;
