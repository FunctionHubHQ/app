'use client'
import * as React from "react";
import {FC} from "react";
import 'rapidoc';
import {DEBUG} from "#/ui/utils/utils";

export interface SandboxProps {
  specUrl?: string,
  apiKey?: string
  bearerToken?: string
}

const Sandbox: FC<SandboxProps> = (props) => {

  const getBearer = () => {
    const bearer = props.apiKey ? 'Bearer ' + props.apiKey : 'Bearer ' + props.bearerToken
    DEBUG("Bearer: ", bearer)
    return bearer
  }

  // @ts-ignore
  return (
      <rapi-doc
          spec-url = {props.specUrl}
          nav-item-spacing = "compact"
          show-components="true"
          header-color = "#ececec"
          render-style = "view"
          update-route = "false"
          layout = "column"
          show-info = "false"
          show-header = "false"
          allow-search = "false"
          api-key-name = "Authorization"
          api-key-location = "header"
          api-key-value = {getBearer()}
          allow-server-selection = "false"
          allow-authentication = "false"
          allow-spec-url-load = "false"
          allow-spec-file-load = "false"
          allow-advanced-search = "false"
          schema-description-expanded = "true"
          response-area-height="50%"
          persist-auth = "true"
          allow-schema-description-expand-toggle="false"
          css-file = "api-sandbox.css"
          css-classes = "fh-api"
          theme = "dark"
          bg-color = "#1e1e1e"
          style = {{ height: "auto", width: "100%" }}
      >
      </rapi-doc>
  )

}

export default Sandbox;