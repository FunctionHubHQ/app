'use client'
import * as React from "react";
import { Disclosure } from '@headlessui/react'
import { ChevronUpIcon } from '@heroicons/react/20/solid'

function Chat() {
  return (
      <div className="gf-editor-base gf-chatbox">
        <div >
          <div className="flex flex-1">

            <div className="grow rounded-lg p-4">

              <div className="chat-messages mb-4">

                <div className="chat-message">
                  <div className="flex items-center mb-6">
                    <div className="flex-shrink-0">
                      <img className="h-8 w-8 rounded-full"
                           src="https://randomuser.me/api/portraits/men/1.jpg" alt="User Avatar"/>
                    </div>
                    <div className="ml-3">
                      <div className="bg-blue-100 text-blue-900 p-2 rounded-lg">
                        <p className="text-sm">Hello, how can I assist you today?</p>
                      </div>
                      <p className="text-xs text-gray-600 mt-1">12:34 PM</p>
                    </div>
                  </div>
                </div>

                <div className="chat-message">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <img className="h-8 w-8 rounded-full"
                           src="https://randomuser.me/api/portraits/men/1.jpg" alt="User Avatar"/>
                    </div>
                    <div className="ml-3">
                      <div className="bg-blue-100 text-blue-900 p-2 rounded-lg">
                        <p className="text-sm">Hello, how can I assist you today?</p>
                      </div>
                      <p className="text-xs text-gray-600 mt-1">12:34 PM</p>
                    </div>
                  </div>
                </div>

                <div className="chat-message">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <img className="h-8 w-8 rounded-full"
                           src="https://randomuser.me/api/portraits/men/1.jpg" alt="User Avatar"/>
                    </div>
                    <div className="ml-3">
                      <div className="bg-blue-100 text-blue-900 p-2 rounded-lg">
                        <p className="text-sm">Hello, how can I assist you today?</p>
                      </div>
                      <p className="text-xs text-gray-600 mt-1">12:34 PM</p>
                    </div>
                  </div>
                </div>

                <div className="chat-message">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <img className="h-8 w-8 rounded-full"
                           src="https://randomuser.me/api/portraits/men/1.jpg" alt="User Avatar"/>
                    </div>
                    <div className="ml-3">
                      <div className="bg-blue-100 text-blue-900 p-2 rounded-lg">
                        <p className="text-sm">Hello, how can I assist you today?</p>
                      </div>
                      <p className="text-xs text-gray-600 mt-1">12:34 PM</p>
                    </div>
                  </div>
                </div>

                <div className="chat-message">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <img className="h-8 w-8 rounded-full"
                           src="https://randomuser.me/api/portraits/men/1.jpg" alt="User Avatar"/>
                    </div>
                    <div className="ml-3">
                      <div className="bg-blue-100 text-blue-900 p-2 rounded-lg">
                        <p className="text-sm">Hello, how can I assist you today?</p>
                      </div>
                      <p className="text-xs text-gray-600 mt-1">12:34 PM</p>
                    </div>
                  </div>
                </div>

                <div className="chat-message">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <img className="h-8 w-8 rounded-full"
                           src="https://randomuser.me/api/portraits/men/1.jpg" alt="User Avatar"/>
                    </div>
                    <div className="ml-3">
                      <div className="bg-blue-100 text-blue-900 p-2 rounded-lg">
                        <p className="text-sm">Hello, how can I assist you today?</p>
                      </div>
                      <p className="text-xs text-gray-600 mt-1">12:34 PM</p>
                    </div>
                  </div>
                </div>


                <div className="chat-message">
                  <div className="flex items-center justify-end">
                    <div className="mr-3 text-right">
                      <div className="bg-green-100 text-green-900 p-2 rounded-lg">
                        <p className="text-sm">Hi there! I'm ChatGPT, your AI assistant.</p>
                      </div>
                      <p className="text-xs text-gray-600 mt-1">12:35 PM</p>
                    </div>
                    <div className="flex-shrink-0">
                      <img className="h-8 w-8 rounded-full"
                           src="https://via.placeholder.com/150" alt="AI Assistant Avatar"/>
                    </div>
                  </div>
                </div>
              </div>


              <div className="chat-input flex items-center">
                <input type="text" className="flex-grow border rounded-l-lg py-2 px-4" placeholder="Type your prompt..."/>
                  <button
                      className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded-r-lg focus:outline-none">
                    Send
                  </button>
              </div>
            </div>
        </div>
        </div>
      </div>
  )
}
export default Chat;