import React, {FC, useEffect, useState} from "react";
import {Chat} from "../../types";
import {useRef} from "react/index";
import {nanoid} from "nanoid";
import {Button, Form, Input} from "rsuite";
import SendIcon from "@rsuite/icons/Send";

export interface ChatInputProps {
  handleSubmit: () => void,
  handleRawMessageChange: (e: any) => void,
  rawMessage?: string
  showBubbleAnimation: boolean,
}
const Textarea = React.forwardRef((props, ref) => <Input {...props} as="textarea" ref={ref} />);

const ChatInput: FC<ChatInputProps> =  (props) => {

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      props.handleSubmit()
    }
  }

  return (
      <>
        {
          props.showBubbleAnimation ?
              <div className="typing">
                <span className="circle scaling"></span>
                <span className="circle scaling"></span>
                <span className="circle scaling"></span>
              </div> : <div className="typing-empty"/>
        }
        <div className="chat-form-container">
          <Form className="chat-form">
            <Form.Group controlId="chat-box">
              <Form.Control
                  name="textarea"
                  accepter={Textarea}
                  rows={2}
                  onChange={(e) => props.handleRawMessageChange(e)}
                  value={props.rawMessage}
                  onKeyDown={handleKeyDown}
                  placeholder="Ask a question"/>
            </Form.Group>
          </Form>
          <Button onClick={props.handleSubmit}>
            <SendIcon />
          </Button>
        </div>
      </>
  );
}
export default ChatInput