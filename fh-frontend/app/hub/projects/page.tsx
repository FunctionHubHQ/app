"use client"
import {Boundary} from "#/ui/boundary";
import AddProjectButton from "#/ui/project/add-project-button";
import {useEffect, useState} from "react";
import CreateProjectModal from "#/ui/project/create-project-modal";
import {DEBUG, ERROR} from "#/ui/utils/utils";
import { ProjectApi, Projects} from "#/codegen";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {useAuthContext} from "#/context/AuthContext";
import ProjectMenu from "#/ui/project/project-menu";

import * as React from "react";
import Link from "next/link";

export default function Page() {
  const [openModal, setOpenModal] = useState(false)
  const [creationInProgress, setCreationInProgress] = useState(false)
  const [projects, setProjects] = useState<Projects | undefined>(undefined)
  const { user } = useAuthContext()

  useEffect(() => {
    if (!projects) {
      getAllProjects()
      .catch(e => ERROR(e))
    }
  }, [])

  const onProjectCreate = async (name: string, description: string) => {
    const token = await getAuthToken(user)
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
        setOpenModal(false)
        setCreationInProgress(false)
      }).catch(e => {
        setCreationInProgress(false)
        ERROR(e.message)
      })
    }
  }

  const getAllProjects = async () => {
    const token = await getAuthToken(user)
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

  return (
      <div>
        <CreateProjectModal
            action={"Create"}
            creationInProgress={creationInProgress}
            onProjectCreate={onProjectCreate}
            isOpen={openModal}
            onClose={() => setOpenModal(false)}/>

        {(!projects || !projects.projects?.length) ?
            <Boundary labels={['projects']} color="pink">
              <div className="text-vercel-pink space-y-4">
                <h2 className="text-lg font-bold">Not Found</h2>

                <p className="text-sm">You have no projects. Click below to create one.</p>
                <AddProjectButton onClick={() => setOpenModal(true)}/>
              </div>
            </Boundary> :
            <div className="space-y-10 text-white">
              {projects?.projects?.map((project) => {
                return (
                    <Boundary labels={[project?.name as string]} color="default" key={project?.project_id}>
                      <div className="space-y-5">
                        <div className="grid grid-flow-col justify-stretch ">
                          <Link href={'/hub/projects/' + project.project_id}>
                            <div>
                              {/*<div className="text-xs font-semibold uppercase tracking-wider text-gray-400">*/}
                              {/*  {project?.name}*/}
                              {/*</div>*/}
                              <div className="text-xs font tracking-wider text-gray-400 max-w-[600px]">
                                {project?.description}
                              </div>
                            </div>
                          </Link>
                          <div className="flex justify-end">
                            <ProjectMenu/>
                          </div>
                        </div>
                      </div>
                    </Boundary>
                );
              })}
              <AddProjectButton onClick={() => setOpenModal(true)}/>
            </div>
        }
      </div>
  );
}