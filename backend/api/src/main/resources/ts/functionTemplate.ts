export interface RequestEntity {
  name?: string
}

export interface ResponseEntity {
  greeting?: string
}

/**
 * @name untitled
 * @summary A brief description of what my function does
 */
export async function handler(request: RequestEntity): Promise<ResponseEntity> {
  console.log("This is a console log")
  return {
    greeting: `Hello, ${request.name}!`
  }
}