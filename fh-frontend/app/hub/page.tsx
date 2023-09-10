'use client'
import { hubPages } from '#/lib/hubPages';
import Link from 'next/link';
import {useAuthContext} from "#/context/AuthContext";
import {Button, Card} from "flowbite-react";
import FHButton from "#/ui/project/fh-button";
import {useRouter} from "next/navigation";
import {createNewFunction} from "#/ui/utils/utils";

export default function Page() {
  const {authUser, fhUser} = useAuthContext()
  const router = useRouter()

  const onCreateFirstFunction = async () => {
    await createNewFunction(router, authUser, undefined)
  }

  return (
    <div className="space-y-8">
      <div className="space-y-10 text-white">
        {fhUser?.projects && fhUser.projects.length === 0 ? <>

          <Card className="max-w-full">
            <h5 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
              <p>
                Create your first function
              </p>
            </h5>
            <p className="font-normal text-gray-700 dark:text-gray-400">
              <p>
                Create and deploy your first GPT function
              </p>
            </p>
            <FHButton label="Create" onClick={onCreateFirstFunction}/>
          </Card>
        </> : null}


        {hubPages.map((section) => {
          return (
            <div key={section.name} className="space-y-5">
              <div className="text-xs font-semibold uppercase tracking-wider text-gray-400">
                {section.name}
              </div>
              <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
                {section.items.map((item) => {
                  return (
                    <Link
                      href={`/${item.slug}`}
                      key={item.name}
                      className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"
                    >
                      <div className="font-medium text-gray-200 group-hover:text-gray-50">
                        {item.name}
                      </div>

                      {item.description ? (
                        <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">
                          {item.description}
                        </div>
                      ) : null}
                    </Link>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
