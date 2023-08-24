'use client'
import * as React from "react";
import { AiOutlineFolder } from 'react-icons/ai'
import { BsPersonCircle } from 'react-icons/bs'
import { IoExitOutline } from 'react-icons/io5'
import { BiSolidKey } from 'react-icons/bi'
import {
  AiOutlinePlus,
  AiOutlineMenu,
  AiOutlineDelete,
  AiOutlineEdit
} from 'react-icons/ai';


import { Menu, Transition } from '@headlessui/react'
import { Fragment } from 'react'

export default function ProjectMenu() {

  return (
      <div>
        <Menu as="div" >
          <div className="flex justify-end">
            <Menu.Button className="relative flex rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-200 mb-1 justify-end">
              <AiOutlineMenu
                  color={'#a1a1a1'}
                  title="Menu"
                  width="32px"
                  style={{marginRight: '0.5rem'}}
              />
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
            <Menu.Items className="mt-2 w-40 origin-top-right divide-y divide-gray-100 rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
              <div className="px-1 py-1 ">
                {/*<Menu.Item>*/}
                {/*  {({ active }) => (*/}
                {/*      <button*/}
                {/*          // onClick={}*/}
                {/*          className={`${*/}
                {/*              active ? 'bg-gray-100 text-gray-900' : 'text-gray-900'*/}
                {/*          } group flex w-full items-center rounded-md px-2 py-2 text-sm`}*/}
                {/*      >*/}
                {/*        <AiOutlinePlus*/}
                {/*            color={'#a1a1a1'}*/}
                {/*            title="Create"*/}
                {/*            width="32px"*/}
                {/*            style={{marginRight: '0.5rem'}}*/}
                {/*        />*/}
                {/*        Add Function*/}
                {/*      </button>*/}
                {/*  )}*/}
                {/*</Menu.Item>*/}
                <Menu.Item>
                  {({ active }) => (
                      <button
                          // onClick={}
                          className={`${
                              active ? 'bg-gray-100 text-gray-900' : 'text-gray-900'
                          } group flex w-full items-center rounded-md px-2 py-2 text-sm`}
                      >
                        <AiOutlineEdit
                            color={'#a1a1a1'}
                            title="Edit"
                            width="32px"
                            style={{marginRight: '0.5rem'}}
                        />
                        Edit
                      </button>
                  )}
                </Menu.Item>
                <Menu.Item>
                  {({ active }) => (
                      <button
                          className={`${
                              active ? 'bg-gray-100 text-gray-900' : 'text-gray-900'
                          } group flex w-full items-center rounded-md px-2 py-2 text-sm`}
                      >
                        <AiOutlineDelete
                            color={'#a1a1a1'}
                            title="Delete"
                            width="32px"
                            style={{marginRight: '0.5rem'}}
                        />
                        Delete
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
