'use client'
import * as React from "react";
import CodeEditor from "@/components/code/editor";
import Head from "next/head";
import {BreadCrumbs} from "@/components/breadcrumbs/BreadCrumbs";
import { FolderOpenOutline } from 'react-ionicons'

function Index() {

  return  <main>
    <Head>
      <title>This is dynamic</title>
    </Head>
    <section className="bg-gray-50 dark:bg-gray-900">
      <div className='layout relative flex min-h-screen flex-col items-center justify-center py-12'>
        {/*<BreadCrumbs />*/}
        <CodeEditor/>
      </div>
    </section>
  </main>
}
export default Index;