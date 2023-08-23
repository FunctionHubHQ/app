"use client"
import {FC} from "react";
import {AiOutlinePlus} from "react-icons/ai";
import * as React from "react";

export interface FHButtonProps {
  onClick?: () => void
  label: string,
  disabled?: boolean
}

const FHButton: FC<FHButtonProps> = (props) => {
  return (
      <div>
        <button
            disabled={!!props.disabled}
            onClick={props.onClick}
            type="button"
            className="group flex items-center mb-2 md:mb-0 text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
          {props.label}
        </button>
      </div>
  );
};

export default FHButton;
