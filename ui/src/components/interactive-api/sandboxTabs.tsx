'use client'
import React, {FC, useState} from 'react'
import { Tab } from '@headlessui/react'
import Toggle from "@/components/interactive-api/toggle";
import Sandbox, {SandboxProps} from "@/components/interactive-api/sandbox";

function classNames(...classes) {
  return classes.filter(Boolean).join(' ')
}

export interface SandboxTabProps {
  specUrl?: string,
  apiAuthToken?: string
}

const SandboxTabs: FC<SandboxProps> = (props) => {
  let [categories] = useState({
    Dev: [
      {
        id: 1,
        title: 'Does drinking coffee make you smarter?',
        date: '5h ago',
        commentCount: 5,
        shareCount: 2,
      },
      {
        id: 2,
        title: "So you've bought coffee... now what?",
        date: '2h ago',
        commentCount: 3,
        shareCount: 2,
      },
    ],
    Prod: [
      {
        id: 1,
        title: 'Is tech making coffee better or worse?',
        date: 'Jan 7',
        commentCount: 29,
        shareCount: 16,
      },
      {
        id: 2,
        title: 'The most innovative things happening in coffee',
        date: 'Mar 19',
        commentCount: 24,
        shareCount: 12,
      },
    ]
  })

  return (
      <div className="w-full ">
        <Tab.Group>
          <Tab.List className="flex space-x-1 rounded-xl bg-blue-900/20 p-1">
            {Object.keys(categories).map((category) => (
                <Tab
                    key={category}
                    className={({ selected }) =>
                        classNames(
                            'w-full rounded-lg py-2.5 text-sm font-medium leading-5 text-blue-700',
                            'ring-white ring-opacity-60 ring-offset-2 ring-offset-blue-400 focus:outline-none focus:ring-2',
                            selected
                                ? 'bg-white shadow'
                                : 'text-blue-100 hover:bg-white/[0.12] hover:text-white'
                        )
                    }
                >
                  <div className="w-full flex justify-center items-center">
                    <div className="w-full">{category}</div>
                    <div className="mr-2">
                      <Toggle/>
                    </div>
                  </div>
                </Tab>
            ))}
          </Tab.List>
          <Tab.Panels className="mt-2">
            {Object.values(categories).map((posts, idx) => (
                <Tab.Panel
                    key={idx}
                    className={classNames(
                        'rounded-xl p-3',
                        'ring-white ring-opacity-60 ring-offset-2 ring-offset-blue-400 focus:outline-none focus:ring-2'
                    )}
                >
                  <Sandbox specUrl={props.specUrl} apiAuthToken={props.apiAuthToken}/>
                  {/*<ul>*/}
                  {/*  {posts.map((post) => (*/}
                  {/*      <li*/}
                  {/*          key={post.id}*/}
                  {/*          className="relative rounded-md p-3 hover:bg-gray-100"*/}
                  {/*      >*/}
                  {/*        <h3 className="text-sm font-medium leading-5">*/}
                  {/*          {post.title}*/}
                  {/*        </h3>*/}

                  {/*        <ul className="mt-1 flex space-x-1 text-xs font-normal leading-4 text-gray-500">*/}
                  {/*          <li>{post.date}</li>*/}
                  {/*          <li>&middot;</li>*/}
                  {/*          <li>{post.commentCount} comments</li>*/}
                  {/*          <li>&middot;</li>*/}
                  {/*          <li>{post.shareCount} shares</li>*/}
                  {/*        </ul>*/}

                  {/*        <a*/}
                  {/*            href="#"*/}
                  {/*            className={classNames(*/}
                  {/*                'absolute inset-0 rounded-md',*/}
                  {/*                'ring-blue-400 focus:z-10 focus:outline-none focus:ring-2'*/}
                  {/*            )}*/}
                  {/*        />*/}
                  {/*      </li>*/}
                  {/*  ))}*/}
                  {/*</ul>*/}
                </Tab.Panel>
            ))}
          </Tab.Panels>
        </Tab.Group>
      </div>
  )
}
export default SandboxTabs;
