'use client'
import * as React from "react";
import {FC, useState} from 'react'
import { RadioGroup } from '@headlessui/react'
import {Project} from "#/codegen";

export interface ProjectSelectionProps {
  projects: Array<Project>,
  onSetProjectToForkId: (projectId: string) => void
}

const ProjectSelection: FC<ProjectSelectionProps> = (props) => {
  const [selected, setSelected] = useState(props.projects[2])

  const onChange = (selectedProject: any) => {
    setSelected(selectedProject)
    props.onSetProjectToForkId(selectedProject)
  }

  const truncate = (value?: string) => {
    const maxLength = 48
    if (value) {
      if (value.length < maxLength) {
        return value
      }
      return value.substring(0, maxLength) + "..."
    }
  }

  return (
      <div className="w-full px-2 py-8">
        <div className="mx-auto w-full max-w-md max-h-75 overflow-y-auto">
          <RadioGroup value={selected} onChange={onChange}>
            <RadioGroup.Label className="sr-only">Projects</RadioGroup.Label>
            <div className="space-y-2">
              {props.projects.map((project) => (
                  <RadioGroup.Option
                      disabled={project.full}
                      key={project.project_id}
                      value={project.project_id}
                      className={({ active, checked }) =>
                          `${
                              active
                                  ? 'ring-2 ring-white ring-opacity-60 ring-offset-2 ring-offset-sky-300'
                                  : ''
                          }
                  ${
                              checked ? ' bg-blue-700 text-white' : 'bg-white'
                          }
                    relative flex cursor-pointer rounded-lg px-5 py-4 shadow-md focus:outline-none`
                      }
                  >
                    {({ active, checked }) => (
                        <>
                          <div className="flex w-full items-center justify-between">
                            <div className="flex items-center">
                              <div className="text-sm">
                                <RadioGroup.Label
                                    as="p"
                                    className={`font-medium  ${
                                        checked ? 'text-white' : 'text-gray-900'
                                    }`}
                                >
                                  {truncate(project.name)}
                                </RadioGroup.Label>
                                <RadioGroup.Description
                                    as="span"
                                    className={`inline ${
                                        checked ? 'text-sky-100' : 'text-gray-500'
                                    }`}
                                >
                                <span>
                                  {truncate(project.description)}
                                </span>
                                  {project.full &&
                                  <div className="flex">
                                    <div className="w-16 bg-gray-400 rounded flex mt-2">
                                      <span className="text-white px-2  text-center w-full">Full</span>
                                    </div>
                                  </div>
                                  }
                                </RadioGroup.Description>
                              </div>
                            </div>
                            {checked && (
                                <div className="shrink-0 text-white">
                                  <CheckIcon className="h-6 w-6" />
                                </div>
                            )}
                          </div>
                        </>
                    )}
                  </RadioGroup.Option>
              ))}
            </div>
          </RadioGroup>
        </div>
      </div>
  )
}

export default ProjectSelection;

function CheckIcon(props: any) {
  return (
      <svg viewBox="0 0 24 24" fill="none" {...props}>
        <circle cx={12} cy={12} r={12} fill="#fff" opacity="0.2" />
        <path
            d="M7 13l3 3 7-7"
            stroke="#fff"
            strokeWidth={1.5}
            strokeLinecap="round"
            strokeLinejoin="round"
        />
      </svg>
  )
}
