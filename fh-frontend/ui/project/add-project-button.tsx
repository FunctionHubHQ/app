"use client"
import {FC} from "react";
import {AiOutlinePlus} from "react-icons/ai";
import * as React from "react";

export interface AddProjectButtonProps {
  onClick: () => void
}

const AddProjectButton: FC<AddProjectButtonProps> = (props) => {
  return (
      <button
          onClick={props.onClick}
          type="button"
          className="group flex w-44 items-center mb-2 md:mb-0 text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-red-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-red-800">
        <AiOutlinePlus
            color={'#fff'}
            title="Add Project"
            width="32px"
            style={{marginRight: '0.5rem'}}
        />
        Add Project
      </button>
  );
};

export default AddProjectButton;
