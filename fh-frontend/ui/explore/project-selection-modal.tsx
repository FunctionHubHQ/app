"use client"
import {FC, useState} from "react";
import ProjectSelection from "#/ui/explore/project-selection";
import FHForkButton from "#/ui/project/fh-fork-button";
import {Project} from "#/codegen";

import {  Modal } from 'flowbite-react';
import * as React from "react";

export interface ProjectSelectionModalProps {
  isOpen: boolean,
  codeId: string,
  projects: Array<Project>,
  onDoFork: (codeId: string, projectId: string) => void
  onClose: () => void
}

const ProjectSelectionModal: FC<ProjectSelectionModalProps> = (props) => {
  const [projectToForkId, setProjectToForkId] = useState<string>()

  const onSetProjectToForkId = (projectId: string) => {
    setProjectToForkId(projectId)
  }

  return (
      <>
        <Modal show={props.isOpen} size="md" popup onClose={props.onClose} theme={{
          root: {
            "base": "fixed top-0 right-0 left-0 z-50 h-modal h-screen overflow-y-auto overflow-x-hidden md:inset-0 md:h-full py-20",
            show: {
              "on": "flex bg-gray-900 bg-opacity-20 dark:bg-opacity-80",
              "off": "hidden"
            }
          }}}>
          <Modal.Header/>
          <Modal.Body>
           <h3 className="text-black pt-0 pl-4 font-bold text-xl">
                      Please select a project to fork to
           </h3>
            <ProjectSelection projects={props.projects} onSetProjectToForkId={onSetProjectToForkId}/>
            <div className="flex justify-end">
              <FHForkButton onClick={() => props.onDoFork(props.codeId, projectToForkId as string)} showCount={false} label={"Fork"}/>
            </div>
          </Modal.Body>
        </Modal>
      </>
)
}
export default ProjectSelectionModal;