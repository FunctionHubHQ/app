'use client'
import {Boundary} from "#/ui/boundary";
import AddButton from "#/ui/project/add-button";
import {useEffect, useState} from "react";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {
  UserApi,
  UsernameResponse, UserProfileResponse
} from "#/codegen";
import {ERROR} from "#/ui/utils/utils";
import {useAuthContext} from "#/context/AuthContext";
import * as React from "react";
import {AiOutlineCheck, AiOutlineClose} from 'react-icons/ai'

export default function Page() {
  const [usernameAvailable, setUsernameAvailable] = useState(false)
  const [username, setUsername] = useState('')
  const { authUser, fhUser } = useAuthContext()

  useEffect(() => {
    initProfile()
  }, [fhUser])

  const initProfile = () => {
    if (fhUser?.username) {
      setUsername(fhUser?.username)
    }
  }

  const onUpdateProfile = async () => {
    const token = await getAuthToken(authUser)
    if (token && usernameAvailable && username) {
      new UserApi(headerConfig(token))
      .updateUsername({
        username: username
      })
      .then(result => {
        const response: UserProfileResponse = result.data
        setUsername(response.profile?.username ? response.profile?.username : '')
      }).catch(e => ERROR(e))
    }
  }

  const onChange = async (e: any) => {
    e.preventDefault()
    setUsername(e.target.value)
    const token = await getAuthToken(authUser)
    if (token && e.target.value) {
      new UserApi(headerConfig(token))
      .usernameExists({
        username: e.target.value
      })
      .then(result => {
        const response: UsernameResponse = result.data
        setUsernameAvailable(response.is_available as boolean)
      }).catch(e => ERROR(e))
    }
  }

  return (
    <div className="grid grid-cols-1 gap-5 lg:grid-cols-1">
      <div className="flex justify-center items-center pt-8">
        <img className="w-20 h-20 rounded-full"
             src={authUser?.photoURL}
             alt={authUser?.displayName}/>
      </div>
      <div>
        <span className="flex justify-center items-center">{authUser?.displayName}</span>
      </div>

      <div className="py-8">
        {authUser && <Boundary labels={['']} color={'blue'}>
        <>
          <span className="flex text-gray-800 w-full mb-4 text-center justify-start">Create a username below</span>
          <span className="flex text-gray-500 w-full text-sm">Username</span>
          <div className="mb-6 py-2">
            <input type="text" id="default-input"
                   onChange={onChange}
                   value={username}
                   className="bg-gray-900 border border-gray-300 text-white text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"/>
            <span className="flex text-gray-500 w-full text-xxs py-2 ml-1">
              {
                usernameAvailable &&  <>
                  <AiOutlineCheck
                      color={'#3ec434'}
                      title="Available"
                      width="32px"
                      style={{marginRight: '0.5rem'}}
                  />
                Available
                  </>
              }
              {
                  username && !usernameAvailable && username != fhUser?.username &&
                  <>
                    <AiOutlineClose
                        color={'#ff0000'}
                        title="Not Available"
                        width="32px"
                        style={{marginRight: '0.5rem'}}
                    />
                    Not available
                  </>
              }

            </span>
          </div>
          <AddButton label={"Update Profile"} onClick={onUpdateProfile} hideIcon={true} disabled={!usernameAvailable}/>
        </>
      </Boundary>
        }
      </div>
    </div>
  );
}
