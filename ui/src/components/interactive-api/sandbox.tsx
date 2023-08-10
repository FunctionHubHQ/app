'use client'
import * as React from "react";
import {FC} from "react";
import 'rapidoc';

export interface SandboxProps {
  specUrl?: string,
  apiAuthToken?: string
}

const Sandbox: FC<SandboxProps> = (props) => {

  // @ts-ignore
  return (
      <rapi-doc
          spec-url = {props.specUrl}
          nav-item-spacing = "compact"
          header-color = "#ececec"
          render-style = "view"
          update-route = "false"
          layout = "column"
          show-info = "false"
          show-header = "false"
          allow-search = "false"
          api-key-name = "Authorization"
          api-key-location = "header"
          api-key-value = {props.apiAuthToken}
          allow-server-selection = "false"
          allow-authentication = "false"
          allow-spec-url-load = "false"
          allow-spec-file-load = "false"
          allow-advanced-search = "false"
          schema-description-expanded = "true"
          persist-auth = "true"
          css-file = "api-sandbox.css"
          css-classes = "fh-api"
          style = {{ height: "auto", width: "100%" }}
      >
      </rapi-doc>
  )

}

export default Sandbox;