import React, {FC, useEffect} from "react";
import {Chat} from "../../types";
import {useRef} from "react/index";

export interface ChatProps {
  chatHistory: Chat[],
  boundaryId: string
}
const ChatMessages: FC<ChatProps> =  (props) => {
  const messageEndRef = useRef()

  useEffect(() => {
    scrollToBottom()
  }, [props.chatHistory])

  const scrollToBottom = () => {
    // @ts-ignore
    messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }

  const historyWithBoundary = () => {
    return [...props.chatHistory, {
      message: undefined,
      from_user: false,
      id: props.boundaryId,
      boundary: true
    }]
  }

  return (
      <>
        <div className="message-container">
          {
            historyWithBoundary().map(item => {
              if (item.boundary) {
                // @ts-ignore
                return <span className="chat-msg right empty" ref={messageEndRef} key={item.id}>BOUNDARY</span>
              } else if (item.from_user) {
                return <span className="chat-msg right" key={item.id}>{item.message}</span>
              } else {
                return <span className="chat-msg left" key={item.id}
                             dangerouslySetInnerHTML={{__html: item.message as string}}></span>
              }
            })
          }
        </div>
      </>
  );
}
export default ChatMessages