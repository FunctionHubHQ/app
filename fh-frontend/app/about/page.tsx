import * as React from "react";

export default function Page() {

  return (
      <section className=" dark:bg-gray-900">
        <div className="py-8 px-1 mx-auto max-w-screen-xl lg:py-8 lg:px-1">
          <div className="mx-auto max-w-screen-md text-center mb-8 lg:mb-12">
            <h2 className="mb-4 text-4xl tracking-tight font-extrabold text-white dark:text-white">About FunctionHub</h2>
            {/*<p className="mb-5 font-light text-gray-500 sm:text-xl dark:text-gray-400">Last updated: August 25, 2023 </p>*/}
          </div>
          <div className="mb-5 font-light text-gray-400 sm:text-xl dark:text-gray-400">
            <p>Large language models like Open AI’s GPT-3 and GPT-4 have opened up a Pandora’s box of possibilities. We can ingest and analyze complex pieces of information quickly, build personalized chat bots, and assist with coding. Despite their flexibility, these AI models are limited in what they can do because of business concerns and legal issues. According to Postman, 15% of all APIs out there are public, whereas the remaining 85% are private or protected in some way. AI models cannot interface with the vast majority of APIs out there. FunctionHub aims to solve this problem.</p>

            <p className="py-4">
              FunctionHub allows you to write any piece of code in a TypeScript environment and let GPT-3.5 and GPT-4 call them directly. That means these models can now query your database or call any external or internal API. Imagine an ecommerce or customer service chat bot that has real-time visibility into customer information and allows them to perform business transactions in a fully automated and deterministic fashion.
            </p>

            <p className="py-4">
              FunctionHub also allows for your code to be used anywhere with instant REST endpoints. Think of AWS Lambda but much faster.
            </p>

            <p className="py-4">
              There are so many things you can build with FunctionHub and GPT. We will be constantly adding to our public functions so you can fork them and use them in your own applications. If you have any questions or feedback, please shoot us an email at
              <img src="/support_email_gray.png" alt="FunctionHub Support" width={250} className="inline-flex -ml-2"/>.
            </p>
            <h6 className="mb-1 tracking-tight font-extrabold text-white dark:text-white pt-4">Cost</h6>
            <p>FunctionHub has free and paid plans. The free plan has a limited number of projects and functions, whereas the paid plan has unlimited projects and a higher number of functions per project. The paid plan is coming soon.</p>

            <h6 className="mb-1 tracking-tight font-extrabold text-white dark:text-white pt-4">About the Team</h6>
            <p>FunctionHub is based out of Los Angeles and has two team members: a data scientist/generalist and a full stack engineer.</p>
          </div>
        </div>
      </section>
  );
}
