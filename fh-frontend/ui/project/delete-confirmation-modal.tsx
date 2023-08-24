"use client";

import { Button, Modal } from 'flowbite-react';
import {FC} from "react";
import * as React from "react";
import {HiOutlineExclamationCircle} from "react-icons/hi";

export interface DeleteConfirmationModalProps {
  isOpen: boolean,
  onClose: () => void,
  resourceName: string,
  onDelete: () => void
}

const DeleteConfirmationModal: FC<DeleteConfirmationModalProps> = (props) => {

  // flex min-h-full items-center justify-center p-4 text-center
  return (
      <>
        <Modal show={props.isOpen} size="md" popup onClose={props.onClose} theme={{
          root: {
            "base": "fixed top-0 right-0 left-0 z-50 h-modal h-screen overflow-y-auto overflow-x-hidden md:inset-0 md:h-full py-20",
            show: {
              "on": "flex bg-gray-900 bg-opacity-50 dark:bg-opacity-80",
              "off": "hidden"
            }
          }}}>
          <Modal.Header />
          <Modal.Body>
            <div className="text-center">
              <HiOutlineExclamationCircle className="mx-auto mb-4 h-14 w-14 text-gray-400 dark:text-gray-200" />
              <h3 className="mb-5 text-lg font-normal text-gray-500 dark:text-gray-400">
                {`Are you sure you want to delete this ${props.resourceName}? This is an irreversible action.`}
              </h3>
              <div className="flex justify-center gap-4">
                <Button color="failure" onClick={props.onDelete}>
                  Yes, I'm sure
                </Button>
                <Button color="gray" onClick={props.onClose}>
                  No, cancel
                </Button>
              </div>
            </div>
          </Modal.Body>
        </Modal>
      </>
  )
}
export default DeleteConfirmationModal;