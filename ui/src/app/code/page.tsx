'use client'
import * as React from "react";
import CodeEditor from "@/components/code/editor";
import Head from "next/head";

function Index() {

  return  <main>
    <Head>
      <title>Hi</title>
    </Head>
    <section className="bg-gray-50 dark:bg-gray-900">
      <div className='layout relative flex min-h-screen flex-col items-center justify-center py-12'>
        <CodeEditor/>
      </div>
    </section>
  </main>
}
export default Index;