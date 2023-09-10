"use client"
import '#/styles/globals.css';
import '#/styles/editor.css'
import {usePathname} from "next/navigation";
import Footer from "#/ui/nav/footer";
import {AuthContextProvider} from "#/context/AuthContext";
import HeaderNav from "#/ui/nav/header";
import React from "react";


export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const loginLayout = pathname.includes("/login")
  const editorLayout = pathname.includes("edit:")
  const homePageLayout = pathname === '/'
  const exploreLayout = pathname.startsWith("/explore")
  // const viewParentLayout = pathname.startsWith("/view") && pathname.length == "/view".length || pathname.length == "/view/".length
  const viewPublicCodeLayout = pathname.startsWith("/view") && pathname.length > "/view/".length
  const noBgLayout = pathname.includes("/terms") ||
      pathname.includes("/privacy") ||
      pathname.includes("/about") ||
      pathname.includes("/pricing")
  const applyGlobalLayout = !(loginLayout || editorLayout || noBgLayout || homePageLayout || exploreLayout || viewPublicCodeLayout)

  return (
    <html lang="en" className="[color-scheme:dark]">
      <body className="overflow-y-scroll bg-[#1e1e1e] px-8" >
      <AuthContextProvider>
        {!loginLayout && <HeaderNav/>}
        {loginLayout && <div className="min-h-screen">
          <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8">
            <div>
              <div className="rounded-lg p-3.5 lg:p-6">{children}</div>
            </div>
          </div>
        </div>
        }

        {(editorLayout || viewPublicCodeLayout) && <div className="min-h-screen">
          <div className="mx-auto max-w-full lg:px-1 py-16">
            <div className="">
              <div className="rounded-lg bg-[#1e1e1e]">{children}</div>
            </div>

          </div>
        </div>
        }

        {noBgLayout &&
            <div className="min-h-screen pt-20">
              <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8">
                  <div className="p-3.5 lg:p-6">{children}</div>
              </div>
            </div>
        }

        { homePageLayout &&
            <div className="min-h-screen pt-20">
              <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8">
                <div className="p-3.5 lg:p-6">{children}</div>
              </div>
            </div>
        }

        {applyGlobalLayout &&
        <div className="min-h-screen pt-20">
          <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8">

            <div >
              {/*<div className="rounded-lg bg-black">*/}
              {/*  <AddressBar />*/}
              {/*</div>*/}
            </div>
            <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
              <div className="rounded-lg bg-black p-3.5 lg:p-6">{children}</div>
            </div>
          </div>
        </div>
        }

        {exploreLayout &&
            <div className="min-h-screen pt-16">
              <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20  lg:py-8">
                <div >
                </div>
                <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
                  <div className="rounded-lg bg-black  ">{children}</div>
                </div>
              </div>
            </div>
        }

        {/*{viewPublicCodeLayout && children}*/}
        {!loginLayout && <Footer/> }
      </AuthContextProvider>
      </body>
    </html>
  );
}
