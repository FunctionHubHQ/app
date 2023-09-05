import axios from 'npm:axios';

export interface Request {
  /**
   * The person being greeted
   */
  name: string
}

export interface Response {
  /**
   * A customized greeting
   */
  greeting?: string
}

/**
 * @name custom_greeter
 * @summary A brief summary of what my function does
 * @description An extended description of what my function does. This is shown
 *              to others if you make the function public.
 */
export async function handler(request: Request): Promise<Response> {
  console.log("This is a console log")
  return {
    greeting: `Hello, ${request.name}!`
  }
}