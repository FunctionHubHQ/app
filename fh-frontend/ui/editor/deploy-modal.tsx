"use client"
import {FC} from "react";
import { Dialog, Transition } from '@headlessui/react'
import { Fragment } from 'react'

export interface DeployModalProps {
  message: string | undefined
  isOpen: boolean,
  deploymentFailed: boolean,
  onClose: () => void
}

const DeployModal: FC<DeployModalProps> = (props) => {
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
              <div className="flex min-h-full items-center justify-center p-4 text-center">
                <Transition.Child
                    as={Fragment}
                    enter="ease-out duration-300"
                    enterFrom="opacity-0 scale-95"
                    enterTo="opacity-100 scale-100"
                    leave="ease-in duration-200"
                    leaveFrom="opacity-100 scale-100"
                    leaveTo="opacity-0 scale-95"
                >
                  <Dialog.Panel className="w-full max-w-md transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all">
                    <Dialog.Title
                        as="h3"
                        className="text-lg font-medium leading-6 text-gray-900"
                    >
                      {props.deploymentFailed ?
                      'Deployment Failed' : 'Deployment Succeeded'}
                    </Dialog.Title>
                    <div className="mt-2">
                      {props.deploymentFailed ?
                      <div>
                        <div className="text-sm text-gray-500">Functions with unresolved errors cannot be deployed. Please fix the following error and try again:</div>
                        <span className="text-red-500">{props.message}</span>
                      </div> : <div>
                            <p className="text-sm text-gray-500">
                              {props.message}
                            </p>
                          </div>
                      }
                    </div>

                    <div className="mt-4">
                      <button
                          type="button"
                          className="inline-flex justify-center rounded-md border border-transparent bg-blue-100 px-4 py-2 text-sm font-medium text-blue-900 hover:bg-blue-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2"
                          onClick={props.onClose}
                      >
                        Got it, thanks!
                      </button>
                    </div>
                  </Dialog.Panel>
                </Transition.Child>
              </div>
            </div>
          </Dialog>
        </Transition>
      </>)
}
export default DeployModal;