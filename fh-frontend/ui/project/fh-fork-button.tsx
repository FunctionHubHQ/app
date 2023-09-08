"use client"
import {Component, FC} from "react";
import {BiGitRepoForked} from "react-icons/bi";
import * as React from "react";

export interface FHForkButtonProps {
  onClick: () => void
  disabled?: boolean,
  forkCount?: number,
  showCount?: boolean,
  label: string
}

const FHForkButton: FC<FHForkButtonProps> = (props) => {
  return (
      <div>
        <button
            disabled={!!props.disabled}
            onClick={props.onClick}
            type="button"
            className="group flex items-center mb-2 md:mb-0 text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
          <BiGitRepoForked
              color={'#fff'}
              title="Add"
              width="32px"
              style={{marginRight: '0.5rem'}}
          />
          {props.label}
          {
            props.showCount && <span className={"ml-2 p-1 px-2 rounded bg-blue-600"}>{props.forkCount}</span>
          }

        </button>
      </div>
  );
};

export default FHForkButton;
