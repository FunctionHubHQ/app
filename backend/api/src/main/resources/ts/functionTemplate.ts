export interface RequestEntity {
  /**
   * The person being greeted
   */
  name: string
}

export interface ResponseEntity {
  /**
   * A customized greeting
   */
  greeting?: string
}

/**
 * @name custom_greeter
 * @summary A brief summary of what my function does
 * @description An extended descripiton of what my function does. This is shown
 *              to others if you make the function public.
 */
export async function handler(request: RequestEntity): Promise<ResponseEntity> {
  console.log("This is a console log")
  return {
    greeting: `Hello, ${request.name}!`
  }
}