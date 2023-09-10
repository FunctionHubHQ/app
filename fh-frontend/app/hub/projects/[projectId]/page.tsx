"use client"

import * as React from "react";
import {usePathname, useRouter} from "next/navigation";
import AddButton from "#/ui/project/add-button";
import {Boundary} from "#/ui/boundary";
import {useEffect, useState} from "react";
import {createNewFunction, DEBUG, ERROR, getFunctionPath} from "#/ui/utils/utils";
import {FHFunction, FHFunctions, ProjectApi} from "#/codegen";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {useAuthContext} from "#/context/AuthContext";
import FunctionToggle from "#/ui/project/toggle";
import {AiOutlineDelete} from "react-icons/ai";
import Link from "next/link";
import DeleteConfirmationModal from "#/ui/project/delete-confirmation-modal";



export default function Page() {
  const [currentModalSlug, setCurrentModalSlug] = useState<string | undefined>(undefined)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [fhFunctions, setFhFunctions] = useState<Array<FHFunction>>([])
  const [projectId, setProjectId] = useState('')
  const { authUser } = useAuthContext()
  const router = useRouter()
  const pathname = usePathname();

  useEffect(() => {
    const tokens: string[] = pathname.split("/")
    setProjectId(tokens[tokens.length - 1])
  }, [])

  useEffect(() => {
    if (projectId) {
      getAllFunctions()
      .catch(e => ERROR(e))
    }
  }, [projectId])

  const onAddFunction = async () => {
    await createNewFunction(router, authUser, projectId)
  }

  const getAllFunctions = async () => {
    const token = await getAuthToken(authUser)
    if (token) {
      new ProjectApi(headerConfig(token))
      .getAllFunctions(projectId)
      .then(result => {
        const functions: FHFunctions = result.data
        setFhFunctions(functions.functions as Array<FHFunction>)
      }).catch(e => ERROR(e))
    }
  }

  const onShowFunctionDeleteModal = (functionSlug: string) => {
    setCurrentModalSlug(functionSlug)
    setShowDeleteModal(true)
  }
  
  const onCloseFunctionDeleteModal = () => {
    setCurrentModalSlug(undefined)
    setShowDeleteModal(false)
  }
  
  const onDeleteFunction = async () => {
    if (currentModalSlug) {
      setShowDeleteModal(false)
      const token = await getAuthToken(authUser)
      if (token) {
        new ProjectApi(headerConfig(token))
        .deleteFunction(currentModalSlug)
        .then(result => {
          const functions: FHFunctions = result.data
          DEBUG("onDeleteFunction: ", functions)
          setFhFunctions(functions.functions as Array<FHFunction>)
        }).catch(e => ERROR(e))
      }
    }
  }

  return (
      <div className="space-y-8">
        {(!fhFunctions || !fhFunctions.length) ?
            <Boundary labels={['functions']} color="blue" animateRerendering={false}>
              <div className="text-vercel-blue space-y-4">
                <h2 className="text-lg font-bold">Not Found</h2>

                <p className="text-sm">You have no functions. Click below to create one.</p>
                <AddButton onClick={onAddFunction} label={"Add Function"}/>
              </div>
            </Boundary> :
            <Boundary labels={['functions']} color="none" animateRerendering={false}>
            <div className="space-y-10 text-white">
              <div className="grid grid-cols-1 gap-5 lg:grid-cols-1">
                {fhFunctions?.map((_function: FHFunction) => {
                  return (
                      <div
                          key={_function.slug}
                          className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"
                      >
                        <DeleteConfirmationModal
                            isOpen={showDeleteModal}
                            onClose={onCloseFunctionDeleteModal}
                            onDelete={onDeleteFunction}
                            resourceName={'function'}
                        />
                        <div className="grid grid-flow-col justify-stretch">
                          <div>
                            <Link href={getFunctionPath(_function.code_id as string, _function.project_id as string)}>
                            <div className="font-medium text-gray-200 group-hover:text-gray-50 max-w-[10px]">
                              {_function.name}
                            </div>
                            </Link>
                          </div>
                        </div>

                        {_function.description ? (
                            <Link href={getFunctionPath(_function.code_id as string, _function.project_id as string)}>
                            <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">
                              {_function.description}
                            </div>
                            </Link>
                        ) : null}


                        <div className="grid grid-flow-col justify-stretch">
                          <div>
                            <div className="font-medium text-gray-200 group-hover:text-gray-50 mt-4">
                              <FunctionToggle
                                  codeId={_function.code_id as string}
                                  isPublic={_function.is_public as boolean}/>
                            </div>
                          </div>
                          <div className="flex justify-end mt-4">
                            <button
                                onClick={() => onShowFunctionDeleteModal(_function.slug as string)}
                                className='text-gray-900 group  items-center rounded-md px-2 py-2 text-sm'
                            >
                              <AiOutlineDelete
                                  color={'#a1a1a1'}
                                  title="Delete"
                                  width="48px"
                              />
                            </button>
                          </div>
                        </div>
                      </div>
                  );
                })}
              </div>
              <AddButton onClick={onAddFunction} label={"Add Function"}/>
            </div>
            </Boundary>
        }
      </div>
  );
}