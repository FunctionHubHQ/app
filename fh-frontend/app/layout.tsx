"use client"
import '#/styles/globals.css';
import '#/styles/editor.css'
import { AddressBar } from '#/ui/address-bar';
import Byline from '#/ui/byline';
import { GlobalNav } from '#/ui/global-nav';
import {usePathname} from "next/navigation";
import Footer from "#/ui/nav/footer";
import {AuthContextProvider} from "#/context/AuthContext";
import HeaderNav from "#/ui/nav/header";


export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const loginLayout = pathname.includes("/login")
  const projectsLayout = pathname.includes("/projects/")
  const applyGlobalLayout = !(loginLayout || projectsLayout)

  return (
    <html lang="en" className="[color-scheme:dark]">
      <body className="overflow-y-scroll bg-[#1e1e1e] px-8" >
      <AuthContextProvider>
        <HeaderNav/>
        {/*<GlobalNav />*/}

        {loginLayout && <div className="min-h-screen">
          <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8">
            <div>
              <div className="rounded-lg p-3.5 lg:p-6">{children}</div>
            </div>
          </div>
        </div>
        }

        {projectsLayout && <div className="lg:pl-40">
          <div className="mx-auto max-w-screen-2xl space-y-2 lg:px-2 lg:py-8">
            <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
              <div className="rounded-lg bg-[#1e1e1e]">{children}</div>
            </div>

          </div>
        </div>
        }

        {applyGlobalLayout &&
        <div className="lg:pl-0">
          <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8">

            <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
              <div className="rounded-lg bg-black">
                <AddressBar />
              </div>
            </div>
            <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
              <div className="rounded-lg bg-black p-3.5 lg:p-6">{children}</div>
            </div>
            {/*<Byline className="fixed sm:hidden" />*/}
          </div>
        </div>
        }
        <Footer/>
      </AuthContextProvider>
      </body>
    </html>
  );
}
