"use client"
import {FC} from "react";
import {AiOutlinePlus} from "react-icons/ai";
import * as React from "react";
import {MdDelete} from 'react-icons/md'

export interface DeleteButtonProps {
  onClick: () => void
  label: string
}

const DeleteButton: FC<DeleteButtonProps> = (props) => {
  return (
      <div>
        <button
            onClick={props.onClick}
            type="button"
            className="group flex w-44 items-center mb-2 md:mb-0 text-white bg-red-700 hover:bg-red-800 focus:ring-4 focus:outline-none focus:ring-red-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-red-600 dark:hover:bg-red-700 dark:focus:ring-red-800">
          <MdDelete
              color={'#fff'}
              title="Delete"
              width="32px"
              style={{marginRight: '0.5rem'}}
          />
          {props.label}
        </button>
      </div>
  );
};

export default DeleteButton;
