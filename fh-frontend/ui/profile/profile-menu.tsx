'use client'
import * as React from "react";
import { AiOutlineFolder } from 'react-icons/ai'
import { BsPersonCircle } from 'react-icons/bs'
import { IoExitOutline } from 'react-icons/io5'
import { BiSolidKey } from 'react-icons/bi'

import { Menu, Transition } from '@headlessui/react'
import { Fragment } from 'react'
import {useAuthContext} from "#/context/AuthContext";
import {auth} from "#/ui/utils/firebase-setup";
import {useRouter} from "next/navigation";
import {DEBUG, ERROR} from "#/ui/utils/utils";

export default function ProfileDropdown() {
  const {user} = useAuthContext()
  const router = useRouter()

  const logout = () => {
    auth.signOut().then(_ => {
      router.push("/")
    }).catch(e => ERROR(e))
  }

  const getUserName = () => {
    const maxLength = 20;
    let handle = user.displayName; // get handle from server
    if (handle && handle.length > maxLength) {
      handle = handle.substring(0, maxLength - 3) + "..."
    }
    return handle
  }
  return (
      <div>
        <Menu as="div" >
          <div>
            <Menu.Button className="relative flex rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-200 mb-1">
              <img className="w-7 h-7 rounded-full" id="avatarButton" data-dropdown-toggle="userDropdown"  data-dropdown-placement="bottom-start"
                   src={user.photoURL}
                   alt="User dropdown"/>
            </Menu.Button>
          </div>
          <Transition
              as={Fragment}
              enter="transition ease-out duration-100"
              enterFrom="transform opacity-0 scale-95"
              enterTo="transform opacity-100 scale-100"
              leave="transition ease-in duration-75"
              leaveFrom="transform opacity-100 scale-100"
              leaveTo="transform opacity-0 scale-95"
          >
            <Menu.Items className="absolute right-0 mt-2 w-40 origin-top-right divide-y divide-gray-100 rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
              <div className="px-1 py-1 ">
                <Menu.Item>
                  {({ active }) => (
                      <a href="/hub/profile">
                        <button
                            className={`${
                                active ? 'bg-gray-100 text-gray-900' : 'text-gray-900'
                            } group flex w-full items-center rounded-md px-2 py-2 text-sm`}
                        >
                          <BsPersonCircle
                              color={'#a1a1a1'}
                              title="Profile"
                              width="32px"
                              style={{marginRight: '0.5rem'}}
                          />
                          Profile
                        </button>
                      </a>
                  )}
                </Menu.Item>
              </div>
              <div className="px-1 py-1 ">
                <Menu.Item>
                  {({ active }) => (
                      <a href="/hub/projects">
                        <button
                            className={`${
                                active ? 'bg-gray-100 text-gray-900' : 'text-gray-900'
                            } group flex w-full items-center rounded-md px-2 py-2 text-sm`}
                        >
                          <AiOutlineFolder
                              color={'#a1a1a1'}
                              title="Projects"
                              width="32px"
                              style={{marginRight: '0.5rem'}}
                          />
                          Projects
                        </button>
                      </a>
                  )}
                </Menu.Item>
                <Menu.Item>
                  {({ active }) => (
                      <a href="/hub/api-keys">
                        <button
                            className={`${
                                active ? 'bg-gray-100 text-gray-900' : 'text-gray-900'
                            } group flex w-full items-center rounded-md px-2 py-2 text-sm`}
                        >
                          <BiSolidKey
                              color={'#a1a1a1'}
                              title="API Keys"
                              width="32px"
                              style={{marginRight: '0.5rem'}}
                          />
                          API Keys
                        </button>
                      </a>
                  )}
                </Menu.Item>
              </div>
              <div className="px-1 py-1 ">
                <Menu.Item>
                  {({ active }) => (
                      <button
                          onClick = {logout}
                          className={`${
                              active ? 'bg-gray-100 text-gray-900' : 'text-gray-900'
                          } group flex w-full items-center rounded-md px-2 py-2 text-sm`}
                      >
                        <IoExitOutline
                            color={'#a1a1a1'}
                            title="Sign out"
                            width="32px"
                            style={{marginRight: '0.5rem'}}
                        />
                        Sign out
                      </button>
                  )}
                </Menu.Item>
              </div>
            </Menu.Items>
          </Transition>
        </Menu>
      </div>
  )
}
