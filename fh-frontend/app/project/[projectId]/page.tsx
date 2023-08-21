"use client"
import {demos} from "#/lib/demos";
import Link from "next/link";
import {Boundary} from "#/ui/boundary";
import AddProjectButton from "#/ui/project/add-project-button";
import {useEffect, useState} from "react";
import CreateProjectModal from "#/ui/project/create-project-modal";
import {DEBUG, ERROR} from "#/ui/utils/utils";
import {CodeUpdateResult, FHFunction, FHFunctions, ProjectApi, Projects} from "#/codegen";
import {headerConfig} from "#/ui/utils/headerConfig";
import {useAuthContext} from "#/context/AuthContext";
import ProjectMenu from "#/ui/project/project-menu";
import FunctionToggle from "#/ui/project/toggle";
import {AiOutlineDelete, AiOutlinePlus} from "react-icons/ai";


import * as React from "react";
import {usePathname} from "next/navigation";
import AddFunctionButton from "#/ui/project/add-function-button";

export default function Page() {
  const [openModal, setOpenModal] = useState(false)
  const [creationInProgress, setCreationInProgress] = useState(false)
  const [projects, setProjects] = useState<Projects | undefined>(undefined)
  const [allFunctions, setAllFunctions] = useState<Map<string, Array<FHFunction>>>(new Map())
  const [functionMap, setFunctionMap] = useState<Map<string, Array<FHFunction>>>(new Map())
  const [functionsList, setFunctionsList] = useState([])
  const [projectId, setProjectId] = useState('')
  const { user } = useAuthContext()

  const pathname = usePathname();

  console.log("pathname: ", pathname)

  const updateAllFunctions = (k,v) => {
    setAllFunctions(allFunctions.set(k,v));
  }


  useEffect(() => {
    if (!projects) {
      getAllProjects()
      .catch(e => ERROR(e))
    }
  }, [])

  useEffect(() => {
    const tokens: string[] = pathname.split("/")
    setProjectId(tokens[tokens.length - 1])
  }, [])

  const onProjectCreate = async (name: string, description: string) => {
    DEBUG("OnProjectCreate: ", name, description)
    const tokenResult = await user?.getIdTokenResult(false)
    DEBUG("tokenResult: ", tokenResult)
    if (tokenResult?.token) {
      setCreationInProgress(true)
      new ProjectApi(headerConfig(tokenResult.token))
      .createProject({
        name: name,
        description: description
      })
      .then(result => {
        const response : Projects = result.data
        DEBUG("Projects: ", response)
        setProjects(response)
        setOpenModal(false)
        setCreationInProgress(false)
      }).catch(e => {
        setCreationInProgress(false)
        ERROR(e.message)
      })
    }
  }

  const onAddFunction = async (projectId: string) => {
    DEBUG("onAddFunction: ", projectId)
    const tokenResult = await user?.getIdTokenResult(false)
    DEBUG("tokenResult: ", tokenResult)
    if (tokenResult?.token) {
      // setCreationInProgress(true)
      new ProjectApi(headerConfig(tokenResult.token))
      .createFunction(projectId)
      .then(result => {
        const response : CodeUpdateResult = result.data
        DEBUG("CodeUpdateResult: ", response)
      }).catch(e => {
        ERROR(e.message)
      })
    }
  }

  const getAllProjects = async () => {
    const tokenResult = await user?.getIdTokenResult(false)
    if (tokenResult?.token) {
      new ProjectApi(headerConfig(tokenResult.token))
      .getAllProjects()
      .then(result => {
        const _projects : Projects = result.data
        DEBUG("Projects: ", _projects)
        setProjects(_projects)
        const _functionMap = new Map<string, Array<FHFunction>>()

        for (let i = 0; i < _projects?.projects?.length; i++) {
          if (_projects?.projects) {
            const projectId = _projects?.projects[i].project_id as string
            console.log("Looping projects: ",projectId)
            _functionMap.set(projectId, new Array<FHFunction>())
          }
        }
        setFunctionMap(_functionMap)
        // for (let i = 0; i < _projects.projects?.length; i++) {
        //
        //   console.log("response.projects[i]: ", _projects.projects?.at[i])
        //   // _functionMap.set(response.projects.)
        // }
        // getAllFunctions(_projects)
        // .catch(e => ERROR(e))
      }).catch(e => {
        ERROR(e.message)
      })
    }
  }

  const getAllFunctions = async () => {
    const tokenResult = await user?.getIdTokenResult(false)
    if (tokenResult?.token && projects?.projects?.length) {
      for (let i = 0; i < projects.projects.length; i++) {
        const projectId = projects.projects[i].project_id as string
        new ProjectApi(headerConfig(tokenResult.token))
        .getAllFunctions(projectId)
        .then(result => {
          const functions: FHFunctions = result.data
          // console.log("function map: ", functionMap)
          console.log("Before: ", functionMap.size)
          const temp = new Map(functionMap)
          if (functions.project_id != null && functions.functions?.length) {
            console.log("setting temp: ", functions.project_id, functions.functions)
            temp.set(functions.project_id, functions.functions)
          }
          console.log("Temp: ", temp)

          setFunctionMap(temp)
          console.log("After: ", temp.size)
          // setFunctionsList([...functionsList, functions.functions])

          // if (allFunctions) {
          //   const _copy = {...allFunctions}
          //   _copy[functions.project_id] = functions.functions
          //   setAllFunctions(_copy)
          //   // DEBUG("Set all functions to: ", _copy)
          // }
        }).catch(e => ERROR(e))
      }
    }
  }

  const getFunctionsByProjectId = (projectId: string | undefined): Array<FHFunction> => {
    if (projectId != null) {
      const functionsFound = functionMap.get(projectId)
      if (functionsFound) {
        DEBUG("Function by project: ", functionsFound)
        return functionsFound
      }
    }
    // console.log("functionMap: ", functionMap)
    // console.log("functionsList", functionsList)

    // if (allFunctions.has(projectId)) {
    //   DEBUG("Function by project: ", allFunctions.get(projectId))
    //   return allFunctions.get(projectId)
    // }
    DEBUG("Function by project not found: ", projectId)
    // DEBUG("Not found but all functions: ", allFunctions)
    return new Array<FHFunction>()
  }


  return (
      <div className="space-y-8">
        <CreateProjectModal
            action={"Create"}
            creationInProgress={creationInProgress}
            onProjectCreate={onProjectCreate}
            isOpen={openModal}
            onClose={() => setOpenModal(false)}/>

        {(!projects || !projects.projects?.length) ?
            <Boundary labels={['functions']} color="blue">
              <div className="text-vercel-blue space-y-4">
                <h2 className="text-lg font-bold">Not Found</h2>

                <p className="text-sm">You have no functions. Click below to create one.</p>
                <AddFunctionButton onClick={() => onAddFunction(projectId)}/>
              </div>
            </Boundary> :
            <div className="space-y-10 text-white">
              {projects?.projects?.map((project) => {
                return (
                    <div key={project?.project_id} className="space-y-5">
                      <div className="grid grid-flow-col justify-stretch ">
                        <div>
                          <div className="text-xs font-semibold uppercase tracking-wider text-gray-400">
                            {project?.name}
                          </div>
                          <div className="text-xs font tracking-wider text-gray-400 max-w-[600px]">
                            {project?.description}
                          </div>
                        </div>
                        <div className="flex justify-end">
                          <ProjectMenu/>
                        </div>
                      </div>
                      <AddFunctionButton onClick={() => onAddFunction(projectId)}/>
                      <div className="grid grid-cols-1 gap-5 lg:grid-cols-1">
                        {getFunctionsByProjectId(project.project_id)?.map((_function: FHFunction) => {
                          return (
                              <div
                                  key={_function.slug}
                                  className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"
                              >
                                {/*<div className="font-medium text-gray-200 group-hover:text-gray-50">*/}
                                {/*  {item.name}*/}
                                {/*</div>*/}


                                <div className="grid grid-flow-col justify-stretch ">
                                  <div>
                                    <div className="font-medium text-gray-200 group-hover:text-gray-50 max-w-[10px]">
                                      {_function.name}
                                    </div>
                                  </div>
                                </div>

                                {_function.description ? (
                                    <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">
                                      {_function.description}
                                    </div>
                                ) : null}


                                <div className="grid grid-flow-col justify-stretch">
                                  <div>
                                    <div className="font-medium text-gray-200 group-hover:text-gray-50 mt-4">
                                      <FunctionToggle/>
                                    </div>
                                  </div>
                                  <div className="flex justify-end mt-4">
                                    <button
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
                    </div>
                );
              })}
              <AddFunctionButton onClick={() => onAddFunction(projectId)}/>
            </div>
        }
      </div>
  );
}