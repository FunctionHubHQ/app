"use client"
import {Boundary} from "#/ui/boundary";
import AddProjectButton from "#/ui/project/add-project-button";
import {useEffect, useState} from "react";
import CreateProjectModal from "#/ui/project/create-project-modal";
import {DEBUG, ERROR} from "#/ui/utils/utils";
import {Project, ProjectApi, Projects} from "#/codegen";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {useAuthContext} from "#/context/AuthContext";

import * as React from "react";
import Link from "next/link";
import DeleteConfirmationModal from "#/ui/project/delete-confirmation-modal";
import {AiOutlineDelete, AiOutlineEdit} from "react-icons/ai";

export default function Page() {
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [currentOpenModalId, setCurrentOpenModalId] = useState<string | undefined>(undefined)
  const [currentSelectedProject, setCurrentSelectedProject] = useState<Project | undefined>(undefined)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [creationInProgress, setCreationInProgress] = useState(false)
  const [projects, setProjects] = useState<Projects | undefined>(undefined)
  const { authUser } = useAuthContext()

  useEffect(() => {
    if (!projects) {
      getAllProjects()
      .catch(e => ERROR(e))
    }
  }, [])

  const onProjectCreate = async (name: string, description: string) => {
    const token = await getAuthToken(authUser)
    if (token) {
      setCreationInProgress(true)
      new ProjectApi(headerConfig(token))
      .createProject({
        name: name,
        description: description
      })
      .then(result => {
        const response : Projects = result.data
        DEBUG("Projects: ", response)
        setProjects(response)
        setShowCreateModal(false)
        setCreationInProgress(false)
      }).catch(e => {
        setCreationInProgress(false)
        ERROR(e.message)
      })
    }
  }

  const onProjectUpdate = async (name: string, description: string) => {
    const token = await getAuthToken(authUser)
    if (token && currentOpenModalId) {
      setCreationInProgress(true)
      setShowEditModal(false)
      new ProjectApi(headerConfig(token))
      .updateProject({
        name: name,
        description: description,
        project_id: currentOpenModalId
      })
      .then(result => {
        const response : Projects = result.data
        DEBUG("Projects: ", response)
        setProjects(response)
        setCreationInProgress(false)
      }).catch(e => {
        setCreationInProgress(false)
        ERROR(e.message)
      })
    }
  }

  const getAllProjects = async () => {
    const token = await getAuthToken(authUser)
    if (token) {
      new ProjectApi(headerConfig(token))
      .getAllProjects()
      .then(result => {
        const _projects : Projects = result.data
        DEBUG("Projects: ", _projects)
        setProjects(_projects)
      }).catch(e => {
        ERROR(e.message)
      })
    }
  }

  const onShowProjectDeleteModal = (projectId: string) => {
    setCurrentOpenModalId(projectId)
    setShowDeleteModal(true)
  }

  const onCloseDeleteProjectModal = () => {
    setCurrentOpenModalId(undefined)
    setShowDeleteModal(false)
  }

  const onShowProjectUpdateModal = (project: Project) => {
    setCurrentOpenModalId(project.project_id)
    setCurrentSelectedProject(project)
    setShowEditModal(true)
  }

  const onCloseProjectUpdateModal = () => {
    setCurrentOpenModalId(undefined)
    setCurrentSelectedProject(undefined)
    setShowEditModal(false)
  }

  const onDeleteProject = async () => {
    if (currentOpenModalId) {
      setShowDeleteModal(false)
      const token = await getAuthToken(authUser)
      if (token) {
        new ProjectApi(headerConfig(token))
        .deleteProject(currentOpenModalId)
        .then(result => {
          const projects: Projects = result.data
          DEBUG("onDeleteProject: ", projects)
          setProjects(projects)
        }).catch(e => ERROR(e))
      }
    }
  }

  return (
      <div>
        <CreateProjectModal
            action={"Create"}
            creationInProgress={creationInProgress}
            onProjectCreate={onProjectCreate}
            isOpen={showCreateModal}
            onClose={() => setShowCreateModal(false)}/>

        <CreateProjectModal
            action={"Save"}
            creationInProgress={creationInProgress}
            onProjectUpdate={onProjectUpdate}
            isOpen={showEditModal}
            projectId={currentOpenModalId}
            name={currentSelectedProject?.name}
            description={currentSelectedProject?.description}
            onClose={onCloseProjectUpdateModal}/>

        {(!projects || !projects.projects?.length) ?
            <Boundary labels={['projects']} color="default" animateRerendering={false}>
              <div className="text-vercel-blue space-y-4">
                <h2 className="text-lg font-bold">Not Found</h2>

                <p className="text-sm">You have no projects. Click below to create one.</p>
                <AddProjectButton onClick={() => setShowCreateModal(true)}/>
              </div>
            </Boundary> :
            <Boundary labels={['projects']} color="none" animateRerendering={false}>
              <div className="space-y-10 text-white">
              {projects?.projects?.map((project) => {
                return (
                    <div
                        key={project?.project_id}
                        className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"
                    >
                      <DeleteConfirmationModal
                          isOpen={showDeleteModal}
                          onClose={onCloseDeleteProjectModal}
                          onDelete={onDeleteProject}
                          resourceName={'project'}
                      />
                      <div className="grid grid-flow-col justify-stretch">
                        <div>
                          <Link href={'/hub/projects/' + project.project_id}>
                            <div className="font-medium text-gray-200 group-hover:text-gray-50">
                              {project?.name}
                            </div>
                          </Link>
                        </div>
                      </div>

                      {project?.description? (
                          <Link href={'/hub/projects/' + project.project_id}>
                            <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">
                              {project?.description}
                            </div>
                          </Link>
                      ) : null}


                      <div className="grid grid-flow-col justify-stretch">
                        <div>
                          <div className="font-medium text-gray-200 group-hover:text-gray-50 mt-4">
                          </div>
                        </div>
                        <div className="flex justify-end mt-4">
                          <button
                              onClick={() => onShowProjectUpdateModal(project)}
                              className='text-gray-900 group  items-center rounded-md px-2 py-2 text-sm'
                          >
                            <AiOutlineEdit
                                color={'#a1a1a1'}
                                title="Edit"
                                width="48px"
                            />
                          </button>
                          <button
                              onClick={() => onShowProjectDeleteModal(project.project_id as string)}
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


                      // <div className="space-y-5" key={project?.project_id}>
                      //   <div className="grid grid-flow-col justify-stretch ">
                      //     <Link href={'/hub/projects/' + project.project_id}>
                      //       <div>
                      //         {/*<div className="text-xs font-semibold uppercase tracking-wider text-gray-400">*/}
                      //         {/*  {project?.name}*/}
                      //         {/*</div>*/}
                      //         <div className="text-xs font tracking-wider text-gray-400 max-w-[600px]">
                      //           {project?.description}
                      //         </div>
                      //       </div>
                      //     </Link>
                      //     <div className="flex justify-end">
                      //       {/*<ProjectMenu/>*/}
                      //     </div>
                      //   </div>
                      // </div>
                );
              })}
              <AddProjectButton onClick={() => setShowCreateModal(true)}/>
            </div>
            </Boundary>
        }
      </div>
  );
}