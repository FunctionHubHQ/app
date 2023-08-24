import { useState } from 'react'
import { Switch } from '@headlessui/react'
import {useAuthContext} from "#/context/AuthContext";

function FunctionToggle() {
  const [enabled, setEnabled] = useState(false)
  const [showError, setShowError] = useState(false)
  const {fhUser} = useAuthContext()

  const onChange = () => {
    if (!!fhUser?.username) {
      alert("username: " + fhUser.username)
    }
  }

  return (
      <div>
      <div className="flex items-center">
        <span className="text-xxs text-gray-600 px-2">Private</span>
      <Switch
          checked={enabled}
          onChange={setEnabled}
          className={`${
              enabled ? 'bg-red-500' : 'bg-green-500'
          } relative inline-flex h-4 w-10 items-center rounded-full z-0`}
      >
        <span className="sr-only">Enable notifications</span>
        <span
            className={`${
                enabled ? 'translate-x-6' : 'translate-x-1'
            } inline-block h-3 w-3 transform rounded-full bg-white transition`}
        />
      </Switch>
        <span className="text-xxs text-gray-600 px-2">Public</span>
      </div>
      </div>
  )
}
export default FunctionToggle