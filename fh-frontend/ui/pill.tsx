import * as React from "react";
import {FC} from "react";
export interface PillProps {
  label: string
}

const Pill: FC<PillProps> = (props) => {
  return (
      <span
            className="mb-2 md:mb-0 text-white bg-blue-500 font-medium rounded-lg text-sm px-2 py-1 cursor-default">
          {props.label}
      </span>
  );
};

export default Pill;
