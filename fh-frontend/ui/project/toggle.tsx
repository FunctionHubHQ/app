import {FC, useState} from 'react'
import { Switch } from '@headlessui/react'
import {useAuthContext} from "#/context/AuthContext";
import {AiOutlineCheck, AiOutlineClose} from "react-icons/ai";
import * as React from "react";
import Link from "next/link";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {Code, RuntimeApi} from "#/codegen";
import {DEBUG, ERROR} from "#/ui/utils/utils";
import {CodeEditorProps} from "#/ui/editor/editor";

export interface FunctionToggleProps {
  codeId: string,
  isPublic: boolean
}

const FunctionToggle: FC<FunctionToggleProps> = (props) => {
  const [isPublic, setIsPublic] = useState(props.isPublic)
  const [showError, setShowError] = useState(false)
  const {authUser, fhUser} = useAuthContext()

  
  const onChange = () => {
    if (!!fhUser?.username) {
      setShowError(false)
      setIsPublic(!isPublic)
      getAuthToken(authUser).then(token => {
        console.log("about to call update code...: ", props.codeId)
        if (token) {
          new RuntimeApi(headerConfig(token))
          .updateCode({
            code_id: props.codeId,
            is_public: !isPublic,
            fields_to_update: ["is_public"]
          })
          .then(result => {
            const response : Code = result.data
            DEBUG("User code: ", response)
          }).catch(e => {
            ERROR(e.message)
          })
        }
      })
    } else {
      setShowError(true)
    }
  }

  return (
      <div>
      <div className="flex items-center">
        <span className="text-xxs text-gray-600 px-2">Private</span>
      <Switch
          checked={isPublic}
          onChange={onChange}
          className={`${
              isPublic ? 'bg-red-500' : 'bg-green-500'
          } relative inline-flex h-4 w-10 items-center rounded-full z-0`}
      >
        <span
            className={`${
                isPublic ? 'translate-x-6' : 'translate-x-1'
            } inline-block h-3 w-3 transform rounded-full bg-white transition`}
        />
      </Switch>
        <span className="text-xxs text-gray-600 px-2">Public</span>
      </div>
        {showError &&
        <span className="flex text-gray-500 w-full text-xxs py-2 ml-1">
            <AiOutlineClose
                color={'#ff0000'}
                title="Not Available"
                width="32px"
                style={{marginRight: '0.5rem'}}
            />
              <Link href="/hub/profile">
                Please create a username here before you can make a function public
              </Link>

            </span>
        }
      </div>
  )
}
export default FunctionToggle