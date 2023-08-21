"use client"
import {FC} from "react";
import {AiOutlinePlus} from "react-icons/ai";
import * as React from "react";

export interface AddButtonProps {
  onClick: () => void
  label: string
}

const AddButton: FC<AddButtonProps> = (props) => {
  return (
      <div>
        <button
            onClick={props.onClick}
            type="button"
            className="group flex w-44 items-center mb-2 md:mb-0 text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
          <AiOutlinePlus
              color={'#fff'}
              title="Add"
              width="32px"
              style={{marginRight: '0.5rem'}}
          />
          {props.label}
        </button>
      </div>
  );
};

export default AddButton;
