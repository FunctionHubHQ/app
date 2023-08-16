'use client'
import * as React from "react";
import CodeEditor from "@/components/code/editor";
import Head from "next/head";
import {BreadCrumbs} from "@/components/breadcrumbs/BreadCrumbs";
import { FolderOpenOutline } from 'react-ionicons'
import FileManager from "@/components/fileManager";

function Page() {

  return  <main>
    <Head>
      <title>Hi</title>
    </Head>
    <section>
      <div className='flex min-h-screen flex-col items-center justify-center py-12'>
        <BreadCrumbs />
        {/*<FileManager/>*/}
        <CodeEditor/>
      </div>
    </section>
  </main>
}
export default Page;