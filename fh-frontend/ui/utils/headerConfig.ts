import {Configuration} from "#/codegen";
import {User} from "firebase/auth";

export const offlineMode = false

export const getAuthToken = async (user: User): Promise<string> => {
    if (offlineMode && process.env.NODE_ENV !== 'production') {
        return "fh-NP2E0Ax4yui54PbeYILhT5JmU7Q4P1r7WlKpX5hZNdCe7E"
    }
    const tokenResult = await user?.getIdTokenResult(false)
    if (tokenResult?.token) {
        return tokenResult?.token
    }
    return ''
}

export const headerConfig = (token: string) => {
    const config = new Configuration();
    config.baseOptions = {
        headers: { Authorization: 'Bearer ' + token },
    };
    return config
}