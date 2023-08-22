"use client"

import * as React from "react";
import {usePathname, useRouter} from "next/navigation";
import AddButton from "#/ui/project/add-button";
import {Boundary} from "#/ui/boundary";
import {useEffect, useState} from "react";
import {DEBUG, ERROR} from "#/ui/utils/utils";
import {CodeUpdateResult, FHFunction, FHFunctions, ProjectApi, Projects} from "#/codegen";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {useAuthContext} from "#/context/AuthContext";
import ProjectMenu from "#/ui/project/project-menu";
import FunctionToggle from "#/ui/project/toggle";
import {AiOutlineDelete} from "react-icons/ai";
import Link from "next/link";
import DeleteFunctionModal from "#/ui/project/delete-function-modal";



export default function Page() {
  const [creationInProgress, setCreationInProgress] = useState(false)
  const [currentModalSlug, setCurrentModalSlug] = useState<string | undefined>(undefined)
  const [showFunctionDeleteModal, setShowFunctionDeleteModal] = useState(false)
  const [projects, setProjects] = useState<Projects | undefined>(undefined)
  const [fhFunctions, setFhFunctions] = useState<Array<FHFunction>>([])
  const [projectId, setProjectId] = useState('')
  const { user } = useAuthContext()
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
    DEBUG("onAddFunction: ", projectId)
    const token = await getAuthToken(user)
    DEBUG("tokenResult: ", token)
    if (token) {
      // setCreationInProgress(true)
      new ProjectApi(headerConfig(token))
      .createFunction(projectId)
      .then(result => {
        const response : CodeUpdateResult = result.data
        DEBUG("CodeUpdateResult: ", response)
        router.push(getFunctionPath(response.uid as string))
      }).catch(e => {
        ERROR(e.message)
      })
    }
  }

  const getFunctionPath = (codeId: string): string => {
    return `/projects/${projectId}/edit:${codeId}`
  }

  const getAllFunctions = async () => {
    const token = await getAuthToken(user)
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
    console.log("Setting current modal slug: ", functionSlug)
    setCurrentModalSlug(functionSlug)
    setShowFunctionDeleteModal(true)
  }
  
  const onCloseFunctionDeleteModal = () => {
    setCurrentModalSlug(undefined)
    setShowFunctionDeleteModal(false)
  }
  
  const onDeleteFunction = async () => {
    console.log("About to delete: ", currentModalSlug)
    if (currentModalSlug) {
      setShowFunctionDeleteModal(false)
      const token = await getAuthToken(user)
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
            <Boundary labels={['functions']} color="blue">
              <div className="text-vercel-blue space-y-4">
                <h2 className="text-lg font-bold">Not Found</h2>

                <p className="text-sm">You have no functions. Click below to create one.</p>
                <AddButton onClick={onAddFunction} label={"Add Function"}/>
              </div>
            </Boundary> :
            <div className="space-y-10 text-white">
              <div className="grid grid-cols-1 gap-5 lg:grid-cols-1">
                {fhFunctions?.map((_function: FHFunction) => {
                  return (
                      <div
                          key={_function.slug}
                          className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"
                      >
                        <DeleteFunctionModal
                            key={_function.slug as string}
                            isOpen={showFunctionDeleteModal}
                            onClose={onCloseFunctionDeleteModal}
                            functionSlug={currentModalSlug}
                            onDelete={onDeleteFunction}
                        />
                        <div className="grid grid-flow-col justify-stretch">
                          <div>
                            <Link href={getFunctionPath(_function.code_id as string)}>
                            <div className="font-medium text-gray-200 group-hover:text-gray-50 max-w-[10px]">
                              {_function.name}
                            </div>
                            </Link>
                          </div>
                        </div>

                        {_function.description ? (
                            <Link href={getFunctionPath(_function.code_id as string)}>
                            <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">
                              {_function.description}
                            </div>
                            </Link>
                        ) : null}


                        <div className="grid grid-flow-col justify-stretch">
                          <div>
                            <div className="font-medium text-gray-200 group-hover:text-gray-50 mt-4">
                              <FunctionToggle/>
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

            // <div className="space-y-10 text-white">
            //   {projects?.projects?.map((project) => {
            //     return (
            //         <div key={project?.project_id} className="space-y-5">
            //           {/*<div className="grid grid-flow-col justify-stretch ">*/}
            //           {/*  <div>*/}
            //           {/*    <div className="text-xs font-semibold uppercase tracking-wider text-gray-400">*/}
            //           {/*      {project?.name}*/}
            //           {/*    </div>*/}
            //           {/*    <div className="text-xs font tracking-wider text-gray-400 max-w-[600px]">*/}
            //           {/*      {project?.description}*/}
            //           {/*    </div>*/}
            //           {/*  </div>*/}
            //           {/*  <div className="flex justify-end">*/}
            //           {/*    <ProjectMenu/>*/}
            //           {/*  </div>*/}
            //           {/*</div>*/}
            //           <AddFunctionButton onClick={onAddFunction}/>
            //           <div className="grid grid-cols-1 gap-5 lg:grid-cols-1">
            //             {fhFunctions?.map((_function: FHFunction) => {
            //               return (
            //                   <div
            //                       key={_function.slug}
            //                       className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"
            //                   >
            //                     {/*<div className="font-medium text-gray-200 group-hover:text-gray-50">*/}
            //                     {/*  {item.name}*/}
            //                     {/*</div>*/}
            //
            //
            //                     <div className="grid grid-flow-col justify-stretch ">
            //                       <div>
            //                         <div className="font-medium text-gray-200 group-hover:text-gray-50 max-w-[10px]">
            //                           {_function.name}
            //                         </div>
            //                       </div>
            //                     </div>
            //
            //                     {_function.description ? (
            //                         <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">
            //                           {_function.description}
            //                         </div>
            //                     ) : null}
            //
            //
            //                     <div className="grid grid-flow-col justify-stretch">
            //                       <div>
            //                         <div className="font-medium text-gray-200 group-hover:text-gray-50 mt-4">
            //                           <FunctionToggle/>
            //                         </div>
            //                       </div>
            //                       <div className="flex justify-end mt-4">
            //                         <button
            //                             className='text-gray-900 group  items-center rounded-md px-2 py-2 text-sm'
            //                         >
            //                           <AiOutlineDelete
            //                               color={'#a1a1a1'}
            //                               title="Delete"
            //                               width="48px"
            //                           />
            //                         </button>
            //                       </div>
            //                     </div>
            //                   </div>
            //               );
            //             })}
            //           </div>
            //         </div>
            //     );
            //   })}
            //   <AddFunctionButton onClick={onAddFunction}/>
            // </div>
        }
      </div>
  );
}