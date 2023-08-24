"use client"
import {FC, useEffect, useState} from "react";
import { Dialog, Transition } from '@headlessui/react'
import { Fragment } from 'react'
import {DEBUG} from "#/ui/utils/utils";

export interface CreateProjectModalProps {
  isOpen: boolean,
  action: string,
  onClose: () => void,
  creationInProgress: boolean,
  name?: string,
  description?: string,
  projectId?: string,
  onProjectCreate?: (name: string, description: string) => void
  onProjectUpdate?: (name: string, description: string) => void
}

const CreateProjectModal: FC<CreateProjectModalProps> = (props) => {
  const [name, setName] = useState<string | undefined>('')
  const [description, setDescription] = useState<string | undefined>('')
  const charLimit = 255;

  useEffect(() => {
    setName(props.name)
    setDescription(props.description)
  }, [props.name, props.description])
  const isValid = (value: string | undefined): boolean => {
    return !!value && value.trim().length > 0
  }

  const onCreate = (e) => {
    e.preventDefault()
    if (isValid(name) && isValid(description)) {
      if (props?.onProjectCreate) {
        props?.onProjectCreate(name as string, description as string)
      }
    }
    setName('')
    setDescription('')
    return false
  }

  const onUpdate = (e) => {
    e.preventDefault()
    if (isValid(name) && isValid(description) && props.projectId) {
      if (props.onProjectUpdate) {
        props.onProjectUpdate(name as string, description as string)
      }
    }
    setName('')
    setDescription('')
    return false
  }

  const getValue = (e) => {
    const value: string = e.target.value
    DEBUG("CreateProjectModal: ", value)
    return value.substring(0, charLimit)
  }

  const onNameChange = (e) => {
    e.preventDefault()
    setName(getValue(e))
  }

  const onDescriptionChange = (e) => {
    e.preventDefault()
    setDescription(getValue(e))
  }

  return (
      <>
        <Transition appear show={props.isOpen} as={Fragment}>
          <Dialog as="div" className="relative z-10" onClose={props.onClose}>
            <Transition.Child
                as={Fragment}
                enter="ease-out duration-300"
                enterFrom="opacity-0"
                enterTo="opacity-100"
                leave="ease-in duration-200"
                leaveFrom="opacity-100"
                leaveTo="opacity-0"
            >
              <div className="fixed inset-0 bg-black bg-opacity-25" />
            </Transition.Child>

            <div className="fixed inset-0 overflow-y-auto">
              <div className="flex min-h-full items-start justify-center p-4 text-center py-20">
                <Transition.Child
                    as={Fragment}
                    enter="ease-out duration-300"
                    enterFrom="opacity-0 scale-95"
                    enterTo="opacity-100 scale-100"
                    leave="ease-in duration-200"
                    leaveFrom="opacity-100 scale-100"
                    leaveTo="opacity-0 scale-95"
                >
                  <Dialog.Panel className="w-full max-w-md transform overflow-hidden rounded-2xl bg-white p-2 text-left align-middle shadow-xl transition-all">
                    <div className="relative w-full max-w-md max-h-full">
                      <div className="relative bg-white rounded-lg">
                        <div className="px-6 py-6 lg:px-8">
                          <h3 className="mb-4 text-xl font-medium text-gray-900 dark:text-white">{props?.projectId ? 'Update a Project' : 'Create a Project'}</h3>
                          <form className="space-y-6" onSubmit={props?.projectId ? onUpdate : onCreate}>
                            <div>
                              <label htmlFor="email"
                                     className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Name</label>
                              <input
                                  onChange={onNameChange}
                                  value={name}
                                  disabled={props.creationInProgress}
                                     className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white" required/>
                            </div>
                            <div>
                              <label
                                     className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Description</label>
                              <textarea
                                  onChange={onDescriptionChange}
                                  value={description}
                                  disabled={props.creationInProgress}
                                     className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white"
                                     required/>
                            </div>

                            <button
                                    type="submit"
                                    disabled={props.creationInProgress}
                                    className="w-full text-white bg-vercel-blue hover:bg-[#0093f3] focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">{props.action}
                            </button>
                          </form>
                        </div>
                      </div>
                    </div>
                  </Dialog.Panel>
                </Transition.Child>
              </div>
            </div>
          </Dialog>
        </Transition>
      </>)
}
export default CreateProjectModal;