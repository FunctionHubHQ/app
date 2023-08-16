"use client"
import '#/styles/globals.css';
import '#/styles/editor.css'
import { AddressBar } from '#/ui/address-bar';
import Byline from '#/ui/byline';
import { GlobalNav } from '#/ui/global-nav';
import {usePathname} from "next/navigation";


export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const applyGlobalLayout = !pathname.includes("/projects/")

  return (
    <html lang="en" className="[color-scheme:dark]">
      <body className="bg-gray-1100 overflow-y-scroll bg-[url('/grid.svg')] pb-36">
        <GlobalNav />

        {applyGlobalLayout ?
        <div className="lg:pl-72">
          <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8">

            <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
              <div className="rounded-lg bg-black">
                <AddressBar />
              </div>
            </div>
            <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
              <div className="rounded-lg bg-black p-3.5 lg:p-6">{children}</div>
            </div>
            <Byline className="fixed sm:hidden" />
          </div>
        </div> :
            <div className="lg:pl-40">
              <div className="mx-auto max-w-screen-2xl space-y-2 lg:px-2 lg:py-8">
                <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
                  <div className="rounded-lg bg-[#1e1e1e]">{children}</div>
                </div>

              </div>
            </div>
        }
      </body>
    </html>
  );
}
