'use client'

import {FC, useState} from 'react'
import { BsStars } from 'react-icons/bs';
import {DEBUG} from "#/ui/utils/utils";

export interface ToggleProps {
  onClick: (value: boolean) => void
}
const Toggle: FC<ToggleProps> = (props) => {
  const [enabled, setEnabled] = useState(false)

  const onChange = () => {
    setEnabled(!enabled)
    props.onClick(!enabled)
    DEBUG("Toggle onChange: ", !enabled)
  }

  return (
      <label className="relative inline-flex items-center cursor-pointer">
        <input type="checkbox" value="" className="sr-only peer"  onClick={onChange}/>
          <div
              className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
        <div className="ml-2">
          <BsStars />
        </div>
      </label>

)
}
export default Toggle;
